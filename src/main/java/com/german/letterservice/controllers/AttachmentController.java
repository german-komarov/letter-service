package com.german.letterservice.controllers;


import com.german.letterservice.dto.RetrieveAttachmentDto;
import com.german.letterservice.entities.User;
import com.german.letterservice.exceptions.RetrievingAttachmentException;
import com.german.letterservice.services.AttachmentService;
import com.german.letterservice.util.constants.ContentDispositionType;
import com.german.letterservice.util.validators.AttachedFilesValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {


    private final AttachmentService attachmentService;
    private final AttachedFilesValidator attachedFilesValidator;


    @Autowired
    public AttachmentController(AttachmentService attachmentService, AttachedFilesValidator attachedFilesValidator) {
        this.attachmentService = attachmentService;
        this.attachedFilesValidator = attachedFilesValidator;
    }


    @GetMapping("/watch")
    public ResponseEntity<ByteArrayResource> attachmentsWatch(
            @AuthenticationPrincipal User principal, RetrieveAttachmentDto retrieveAttachmentDto) throws RetrievingAttachmentException
    {
        retrieveAttachmentDto.setContentDispositionType(ContentDispositionType.INLINE);

        ResponseEntity<ByteArrayResource> responseEntity = this.attachmentService.retrieveResponseEntity(principal, retrieveAttachmentDto);

        return responseEntity;
    }




    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> attachmentsDownload(
            @AuthenticationPrincipal User principal,
            RetrieveAttachmentDto retrieveAttachmentDto) throws RetrievingAttachmentException
    {
        retrieveAttachmentDto.setContentDispositionType(ContentDispositionType.ATTACHMENT);

        ResponseEntity<ByteArrayResource> responseEntity = this.attachmentService.retrieveResponseEntity(principal, retrieveAttachmentDto);

        return responseEntity;
    }


    @GetMapping("/forbidden-file-extensions")
    public List<String> attachmentsForbiddenFileExtensionsGet(){
        List<String> dangerousFileExtensions=this.attachedFilesValidator.getDangerousFileExtensions();

        return dangerousFileExtensions;
    }

}
