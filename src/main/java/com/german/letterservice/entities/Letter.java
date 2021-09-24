package com.german.letterservice.entities;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "letters")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Letter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false,updatable = false,length = 50)
    private String uniqueCode;


    @Column(nullable = false,updatable = false)
    private String senderUsername;
    @Column(nullable = false,updatable = false)
    private String receiverUsername;


    @Column(length = 100)
    private String subject;

    @Column(length = 10000)
    private String text;


    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinTable(joinColumns = @JoinColumn(name = "letter_id"),inverseJoinColumns = @JoinColumn(name = "reply_id"))
    private List<Letter> replies=new ArrayList<>();


    @OneToMany(fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Attachment> attachments=new ArrayList<>(10);



    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private LocalDateTime receivedAt;




    private boolean isChecked;



    private boolean isReply;
    private String mainLetterUniqueCode;



    private boolean isChosenBySender;
    private boolean isChosenByReceiver;

    private LocalDateTime chosenBySenderAt;
    private LocalDateTime chosenByReceiverAt;



    private boolean isSpam;
    private LocalDateTime markedAsSpamAt;




    private boolean isArchivedBySender;
    private boolean isArchivedByReceiver;

    private LocalDateTime archivedBySenderAt;
    private LocalDateTime archivedByReceiverAt;





    private boolean isDeletedBySenderTemporarily;
    private boolean isDeletedByReceiverTemporarily;

    private LocalDateTime deletedBySenderTemporarilyAt;
    private LocalDateTime deletedByReceiverTemporarilyAt;





    private boolean isDeletedBySenderFinally;
    private boolean isDeletedByReceiverFinally;

    private LocalDateTime deletedBySenderFinallyAt;
    private LocalDateTime deletedByReceiverFinallyAt;


    


    @PrePersist
    private void onPersist(){
        this.receivedAt=LocalDateTime.now();
    }


    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
    }
}
