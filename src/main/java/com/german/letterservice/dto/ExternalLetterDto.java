package com.german.letterservice.dto;


import com.german.letterservice.util.Generators;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "letterUniqueCode")
public class ExternalLetterDto {


    private final String letterUniqueCode = Generators.generateUniqueCode();



    @NotBlank(message = "Sender username cannot be blank")
    @Size(min = 10,max = 85, message = "Sender username must be of length in [10,85] range")
    private String senderUsername;


    @NotNull(message = "Sender password cannot be null")
    @Size(min = 8, max = 255,message = "Sender password must be of length in [8,255] range")
    private String senderPassword;


    @NotBlank(message = "Receiver username cannot be blank")
    @Size(min = 10,max = 85, message = "Receiver username must be of length in [10,85] range")
    private String receiverUsername;


    @Size(max = 100, message = "Subject can be maximum 100 characters")
    private String subject="";

    @Size(max = 10000, message = "Text can be maximum 10000 characters")
    private String text="";

    private List<MultipartFile> attachments=new ArrayList<>();

    private final LocalDateTime sentAt=LocalDateTime.now();






}
