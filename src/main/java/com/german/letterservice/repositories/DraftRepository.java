package com.german.letterservice.repositories;

import com.german.letterservice.entities.Draft;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DraftRepository extends JpaRepository<Draft,Long> {

    Slice<Draft> findBySenderUsername(String senderUsername, Pageable pageable);
}
