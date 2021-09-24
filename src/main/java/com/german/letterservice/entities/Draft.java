package com.german.letterservice.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "draft_letters")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="uniqueCode")
@ToString
public class Draft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, nullable = false, updatable = false,length = 50)
    private String uniqueCode;

    @Column(nullable = false)
    private String senderUsername;

    private String receiverUsername;


    @Column(length = 100)
    private String subject;


    @Column(length = 10000)
    private String text;

    @Column(nullable = false)
    private LocalDateTime leftAsDraftAt;



    // In case if letter that was drafted is reply to another letter
    @Column(nullable = false)
    private boolean isReply;
    private String mainLetterUniqueCode;





}
