package com.yigitusq.subscription_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String[] TRACE_HEADERS = new String[] {
            "traceparent",
            "tracestate",
            "b3",
            "X-B3-TraceId",
            "X-B3-SpanId",
            "X-B3-ParentSpanId",
            "X-B3-Sampled",
            "X-Request-Id"
    };

    /**
     * Bu metot, (Feign Client ile) dışarıya atılan HER İSTEKTEN hemen önce çalışır.
     */
    @Override
    public void apply(RequestTemplate template) {
        // O anki isteğin (request) bilgilerini al
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            // Ana isteğin "Authorization" başlığını (token'ı) al
            String authorizationHeader = attributes.getRequest().getHeader(AUTHORIZATION_HEADER);
            if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                // Ve bu token'ı, dışarı giden yeni isteğe (template) ekle
                template.header(AUTHORIZATION_HEADER, authorizationHeader);
            }

            // Aktif trace header'larını da ilet (W3C ve B3)
            for (String headerName : TRACE_HEADERS) {
                String headerValue = attributes.getRequest().getHeader(headerName);
                if (headerValue != null && !headerValue.isEmpty()) {
                    template.header(headerName, headerValue);
                }
            }
        }
    }
}