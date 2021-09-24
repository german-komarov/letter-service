package com.german.letterservice.services;


import com.german.letterservice.dto.RetrieveAttachmentDto;
import com.german.letterservice.entities.Attachment;
import com.german.letterservice.entities.ByteContent;
import com.german.letterservice.entities.Letter;
import com.german.letterservice.entities.User;
import com.german.letterservice.exceptions.AttachmentNotFoundException;
import com.german.letterservice.exceptions.LetterNotFoundException;
import com.german.letterservice.exceptions.RetrievingAttachmentException;
import com.german.letterservice.repositories.AttachmentRepository;
import com.german.letterservice.util.constants.ContentDispositionType;
import com.german.letterservice.util.constants.ExceptionMessageTemplates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final LetterService letterService;


    @Autowired
    public AttachmentService(AttachmentRepository attachmentRepository, LetterService letterService) {
        this.attachmentRepository = attachmentRepository;
        this.letterService = letterService;
    }


    public Attachment retrieveByUniqueCode(String attachmentUniqueCode) throws AttachmentNotFoundException {
        Attachment attachment=
                attachmentRepository
                        .findByUniqueCode(attachmentUniqueCode)
                        .orElseThrow(()-> new AttachmentNotFoundException("There is no attachment with such unique code"));

        return attachment;
    }



    public ResponseEntity<ByteArrayResource> retrieveResponseEntity(User principal, RetrieveAttachmentDto retrieveAttachmentDto) throws RetrievingAttachmentException {

        String attachmentUniqueCode= retrieveAttachmentDto.getAttachmentUniqueCode();



        Attachment attachmentToWatch;

        try {
            attachmentToWatch=this.retrieveByUniqueCode(attachmentUniqueCode);
        } catch (AttachmentNotFoundException e) {
            throw new RetrievingAttachmentException(e.getMessage());
        }



        String letterUniqueCode= retrieveAttachmentDto.getLetterUniqueCode();

        Letter letterOfAttachment;

        try {
            letterOfAttachment=this.letterService.retrieveByUniqueCode(letterUniqueCode);
        } catch (LetterNotFoundException e) {
            throw new RetrievingAttachmentException(e.getMessage());
        }


        boolean isPrincipalSender=letterOfAttachment.getSenderUsername().equals(principal.getUsername());
        boolean isPrincipalReceiver=letterOfAttachment.getReceiverUsername().equals(principal.getUsername());

        if ( ! ( isPrincipalSender || isPrincipalReceiver ) ) {
            throw new RetrievingAttachmentException("You are neither sender, nor receiver of the letter to which this file is attached");
        }


        String noAccess= ExceptionMessageTemplates.ACTION_NO_ACCESS_AS;

        if ( letterOfAttachment.isDeletedByReceiverFinally() || letterOfAttachment.isDeletedBySenderFinally() ) {
            throw new RetrievingAttachmentException(String.format("%s this file is attached to letter that is finally deleted", noAccess));
        }



        ContentDispositionType contentDispositionType= retrieveAttachmentDto.getContentDispositionType();

        String contentDispositionTypeString=contentDispositionType.name().toLowerCase();

        String originalFileName=attachmentToWatch.getOriginalFileName();

        String contentDispositionHeaderValue=String.format("%s;filename=%s",contentDispositionTypeString,originalFileName);


        ByteContent byteContentOfAttachment=attachmentToWatch.getByteContent().get(0);
        byte[] bytes=byteContentOfAttachment.getContent();

        ByteArrayResource byteArrayResource=new ByteArrayResource(bytes);





        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(attachmentToWatch.getFileContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionHeaderValue)
                .body(byteArrayResource);

    }















}
