package com.german.letterservice.dto;


import com.german.letterservice.entities.Draft;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "uniqueCode")
@ToString
public class ShowDraftDto {

    private String uniqueCode;
    private String senderUsername;
    private String receiverUsername;
    private String subject;
    private String text;
    private LocalDateTime leftAsDraftAt;
    private boolean isReply;
    private String mainLetterUniqueCode;



    public ShowDraftDto(Draft originalDraft) {
        this.setUniqueCode(originalDraft.getUniqueCode());
        this.setSenderUsername(originalDraft.getSenderUsername());
        this.setReceiverUsername(originalDraft.getReceiverUsername());
        this.setSubject(originalDraft.getSubject());
        this.setText(originalDraft.getText());
        this.setLeftAsDraftAt(originalDraft.getLeftAsDraftAt());
        this.setReply(originalDraft.isReply());
        this.setMainLetterUniqueCode(originalDraft.getMainLetterUniqueCode());
    }



}
