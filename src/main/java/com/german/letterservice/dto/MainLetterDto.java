package com.german.letterservice.dto;


import com.german.letterservice.util.Generators;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "letterUniqueCode")
@ToString
public class MainLetterDto {

    private final String letterUniqueCode;


    @NotBlank(message = "Receiver username cannot be blank")
    @Size(min = 10,max = 85, message = "Receiver username must be of length in [10,85] range")
    private String receiverUsername;



    @Size(max = 100, message = "Subject can be maximum 100 characters")
    private String subject="";

    @Size(max = 10000, message = "Text can be maximum 10000 characters")
    private String text="";

    private List<MultipartFile> attachments=new ArrayList<>();

    private final LocalDateTime sentAt;


    public MainLetterDto() {
        this.letterUniqueCode=Generators.generateUniqueCode();
        this.sentAt=LocalDateTime.now();
    }


    public MainLetterDto(ExternalLetterDto externalLetterDto) {
        this.letterUniqueCode = externalLetterDto.getLetterUniqueCode();
        this.receiverUsername=externalLetterDto.getReceiverUsername();
        this.subject=externalLetterDto.getSubject();
        this.text=externalLetterDto.getText();
        this.attachments=externalLetterDto.getAttachments();
        this.sentAt=externalLetterDto.getSentAt();
    }
}
