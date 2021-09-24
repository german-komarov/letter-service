package com.german.letterservice.dto;


import com.german.letterservice.util.constants.ContentDispositionType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class RetrieveAttachmentDto {

    private String attachmentUniqueCode;
    private String letterUniqueCode;
    private ContentDispositionType contentDispositionType;


    public void setAttachmentUC(String attachmentUniqueCode) {
        this.attachmentUniqueCode = attachmentUniqueCode;
    }

    public void setLetterUC(String letterUniqueCode) {
        this.letterUniqueCode = letterUniqueCode;
    }
}
