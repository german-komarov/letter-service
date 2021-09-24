package com.german.letterservice.dto;


import com.german.letterservice.util.Generators;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "letterUniqueCode")
@ToString
public class ReplyLetterDto {


    private final String letterUniqueCode= Generators.generateUniqueCode();



    @NotBlank(message = "Main letter unique code cannot be blank")
    private String mainLetterUniqueCode;

    @Size(max = 10000, message = "Text can be maximum 10000 characters")
    private String text="";

    private List<MultipartFile> attachments=new ArrayList<>();

    private final LocalDateTime sentAt=LocalDateTime.now();


    public void setMainLetterUC(String mainLetterUniqueCode) {
        this.mainLetterUniqueCode=mainLetterUniqueCode;
    }



}
