package com.yigitusq.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

public class SubscriptionE2ETest {

    // API Gateway ve PostgREST adresleri
    private static final String API_GATEWAY_URL = "http://localhost:8080";
    private static final String PG_CUSTOMER_URL = "http://localhost:3001"; // customerDB PostgREST
    private static final String PG_SUBS_URL = "http://localhost:3002";     // subscriptionDB PostgREST

    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("Yeni Müşteri Oluşturma, Giriş ve Başarılı Abonelik E2E Testi")
    void testFullSubscriptionFlow() throws Exception {

        // --- 1. Müşteri Oluştur (customer-service) ---
        String userEmail = "test.kullanici." + System.currentTimeMillis() + "@example.com";

        Map<String, String> newUser = Map.of(
                "name", "Test",
                "surname", "Kullanici",
                "email", userEmail,
                "password", "password123",
                "status", "ACTIVE",
                "mobile", "555" + (System.currentTimeMillis() % 10000000)
        );

        // API Gateway üzerinden customer-servicei çağır
        Integer customerId = given()
                .baseUri(API_GATEWAY_URL)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(newUser))
                .when()
                .post("/api/customers")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        System.out.println("Adım 1/5: Müşteri oluşturuldu. ID: " + customerId);


        // --- 2. Giriş Yap ve Token Al (customer-service) ---
        Map<String, String> loginRequest = Map.of("email", userEmail, "password", "password123");

        String jwtToken = given()
                .baseUri(API_GATEWAY_URL)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(loginRequest))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .body().asString(); // Dönen token'ı al

        System.out.println("Adım 2/5: JWT Token alındı.");


        // --- 3. Abonelik Oluştur (subscription-service) ---
        // (Bu servis Offer ID'si 1 olan bir teklifin DB'de olduğunu varsayar)
        Map<String, Long> subscriptionRequest = Map.of(
                "customerId", customerId.longValue(),
                "offerId", 1L
        );

        given()
                .baseUri(API_GATEWAY_URL)
                .header("Authorization", "Bearer " + jwtToken) // Token'ı ekle
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(subscriptionRequest))
                .when()
                .post("/api/subscriptions")
                .then()
                .statusCode(201) // HttpStatus.CREATED
                .body("status", equalTo("WAITINGFORPAYMENT")); // API'nin ilk cevabını doğrula

        System.out.println("Adım 3/5: Abonelik isteği gönderildi.");


        // --- 4. PostgREST ile DB Doğrulaması (Ödeme Bekleniyor) ---
        // subscriptionDB'ye (Port 3002) bağlanıp kaydı kontrol et
        given()
                .baseUri(PG_SUBS_URL)
                .get("/subscriptions?customerId=eq." + customerId)
                .then()
                .statusCode(200)
                .body("$", hasSize(1)) // Bir kayıt bulunduğunu doğrula
                .body("[0].status", equalTo("WAITINGFORPAYMENT")); // Statünün 'ödeme bekleniyor' olduğunu doğrula

        System.out.println("Adım 4/5: PostgREST (DB) doğrulaması başarılı: Statü WAITINGFORPAYMENT.");


        // --- 5. Asenkron Bekleme (Kafka -> Payment -> Kafka -> Subscription) ---
        // Awaitility kütüphanesi burada devreye girer.
        // 15 saniye boyunca, her 2 saniyede bir PostgREST'e tekrar sor.
        // payment-service'in işini bitirip subscription-service'i güncellemesini bekle.

        System.out.println("Adım 5/5: Asenkron ödeme sonucu bekleniyor (Max 15 saniye)...");

        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    // PostgREST'e tekrar GET isteği at
                    String status = given()
                            .baseUri(PG_SUBS_URL)
                            .get("/subscriptions?customerId=eq." + customerId)
                            .then()
                            .statusCode(200)
                            .extract()
                            .path("[0].status"); // Statüyü çek

                    // Statü "ACTIVE" olunca bekleme biter
                    return "ACTIVE".equals(status);
                });

        System.out.println("TEST BAŞARILI: Abonelik statüsü 'ACTIVE' olarak güncellendi!");

        // İsteğe bağlı: Temizlik (Teardown)
        // PostgREST ile oluşturduğun verileri silebilirsin
        // given().baseUri(PG_CUSTOMER_URL).delete("/customers?id=eq." + customerId).then().statusCode(204);
        // given().baseUri(PG_SUBS_URL).delete("/subscriptions?customerId=eq." + customerId).then().statusCode(204);
    }
}