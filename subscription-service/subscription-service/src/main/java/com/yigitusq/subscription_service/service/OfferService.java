package com.yigitusq.subscription_service.service;

import com.yigitusq.subscription_service.model.Offer;
import com.yigitusq.subscription_service.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;

    @Cacheable(value = "offers", key = "#id")
    public Offer findById(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found - id: " + id));
    }

    @Cacheable(value = "offers", key = "'all'")
    public List<Offer> findAll() {
        return offerRepository.findAll();
    }

    @CacheEvict(value = "offers", allEntries = true)
    public Offer save(Offer offer) {
        return offerRepository.save(offer);
    }

    @CacheEvict(value = "offers", allEntries = true)
    public void deleteById(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Offer not found - id: " + id);
        }
        offerRepository.deleteById(id);
    }
}


