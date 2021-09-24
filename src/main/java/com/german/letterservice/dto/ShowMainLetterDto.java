package com.german.letterservice.dto;


import com.german.letterservice.entities.Attachment;
import com.german.letterservice.entities.Letter;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "uniqueCode")
@ToString
public class ShowMainLetterDto {
    private String uniqueCode;
    private String senderUsername;
    private String receiverUsername;
    private String subject;
    private String text;
    private List<Attachment> attachments;
    private LocalDateTime sentAt;
    private LocalDateTime receivedAt;
    private final String type="main";


    public ShowMainLetterDto(Letter originalLetter) {
        this.setUniqueCode(originalLetter.getUniqueCode());
        this.setSenderUsername(originalLetter.getSenderUsername());
        this.setReceiverUsername(originalLetter.getReceiverUsername());
        this.setSubject(originalLetter.getSubject());
        this.setText(originalLetter.getText());
        this.setAttachments(originalLetter.getAttachments());
        this.setSentAt(originalLetter.getSentAt());
        this.setReceivedAt(originalLetter.getReceivedAt());
    }
}
