package com.german.letterservice.repositories;

import com.german.letterservice.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment,Long> {



    Optional<Attachment> findByUniqueCode(String uniqueCode);


}
