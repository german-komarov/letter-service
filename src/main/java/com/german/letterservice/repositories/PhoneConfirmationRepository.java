package com.german.letterservice.repositories;

import com.german.letterservice.entities.PhoneConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneConfirmationRepository extends JpaRepository<PhoneConfirmation, Long> {
    Optional<PhoneConfirmation> findByUsername(String username);
}
