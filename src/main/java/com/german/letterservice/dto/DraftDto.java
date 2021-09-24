package com.german.letterservice.dto;


import com.german.letterservice.util.Generators;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "draftUniqueCode")
@ToString
public class DraftDto {

    private final String draftUniqueCode= Generators.generateUniqueCode();

    private String receiverUsername="";
    private String subject="";
    private String text="";

    private String mainLetterUniqueCode="";

    private final LocalDateTime leftAsDraftAt=LocalDateTime.now();

    public void setMainLetterUC(String mainLetterUniqueCode) {
        this.mainLetterUniqueCode = mainLetterUniqueCode;
    }
}
