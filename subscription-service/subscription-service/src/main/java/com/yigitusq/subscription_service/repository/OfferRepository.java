package com.yigitusq.subscription_service.repository;

import com.yigitusq.subscription_service.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {

}
