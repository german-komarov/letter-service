package com.german.letterservice.repositories;

import com.german.letterservice.entities.Letter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LetterRepository extends JpaRepository<Letter,Long> {




    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "insert into letters_replies (letter_id, reply_id) values (:paramLetterId,:paramReplyId)",nativeQuery = true)
    void insertReply(@Param("paramLetterId") Long letterId,@Param("paramReplyId") Long replyId);







    Optional<Letter> findByUniqueCode(String letterUniqueCode);



    @Query("select letter from Letter letter " +
            "where " +
            "letter.receiverUsername=:paramReceiverUsername " +
            "and " +
            "not (letter.isSpam=true or letter.isReply=true or letter.isArchivedByReceiver=true or letter.isDeletedByReceiverTemporarily=true or letter.isDeletedByReceiverFinally=true)")
    Slice<Letter> findIncoming(@Param("paramReceiverUsername") String receiverUsername, Pageable pageable);




    @Query("select letter from Letter letter " +
            "where " +
            "letter.senderUsername=:paramSenderUsername " +
            "and " +
            "not (letter.isReply=true or letter.isArchivedBySender=true or letter.isDeletedBySenderTemporarily=true or letter.isDeletedBySenderFinally=true)")
    Slice<Letter> findSent(@Param("paramSenderUsername") String senderUsername, Pageable pageable);




    @Query("select letter from Letter letter " +
            "where " +
            "letter.receiverUsername=:paramReceiverUsername " +
            "and " +
            "letter.isChosenByReceiver=true " +
            "and " +
            "not (letter.isSpam=true or letter.isArchivedByReceiver=true or letter.isDeletedByReceiverTemporarily=true or letter.isDeletedByReceiverFinally=true)")
    Slice<Letter> findChosenIncoming(@Param("paramReceiverUsername") String receiverUsername, Pageable pageable);




    @Query("select letter from Letter letter " +
            "where " +
            "letter.senderUsername=:paramSenderUsername " +
            "and " +
            "letter.isChosenBySender=true " +
            "and " +
            "not (letter.isArchivedBySender=true or letter.isDeletedBySenderTemporarily=true or letter.isDeletedBySenderFinally=true)")
    Slice<Letter> findChosenSent(@Param("paramSenderUsername") String senderUsername, Pageable pageable);




    @Query("select letter from Letter letter " +
            "where " +
            "letter.receiverUsername=:paramReceiverUsername " +
            "and " +
            "letter.isSpam=true " +
            "and not (letter.isDeletedByReceiverFinally=true or letter.isReply=true)")
    Slice<Letter> findSpam(@Param("paramReceiverUsername") String receiverUsername,Pageable pageable);



    @Query("select letter from Letter letter " +
            "where " +
            "letter.receiverUsername=:paramReceiverUsername " +
            "and " +
            "letter.isArchivedByReceiver=true " +
            "and " +
            "not (letter.isDeletedByReceiverTemporarily=true or letter.isDeletedByReceiverFinally=true)")
    Slice<Letter> findArchivedIncoming(@Param("paramReceiverUsername") String receiverUsername,Pageable pageable);




    @Query("select letter from Letter letter " +
            "where " +
            "letter.senderUsername=:paramSenderUsername " +
            "and " +
            "letter.isArchivedBySender=true " +
            "and " +
            "not (letter.isDeletedBySenderTemporarily=true or letter.isDeletedBySenderFinally=true)")
    Slice<Letter> findArchivedSent(@Param("paramSenderUsername") String senderUsername,Pageable pageable);






    @Query("select letter from Letter letter " +
            "where " +
            "letter.receiverUsername=:paramReceiverUsername " +
            "and " +
            "letter.isDeletedByReceiverTemporarily=true " +
            "and " +
            "letter.isDeletedByReceiverFinally=false")
    Slice<Letter> findDeletedTemporarilyIncoming(@Param("paramReceiverUsername") String receiverUsername, Pageable pageable);





    @Query("select letter from Letter letter " +
            "where " +
            "letter.senderUsername=:paramSenderUsername " +
            "and " +
            "letter.isDeletedBySenderTemporarily=true " +
            "and " +
            "letter.isDeletedBySenderFinally=false")
    Slice<Letter> findDeletedTemporarilySent(@Param("paramSenderUsername") String senderUsername, Pageable pageable);





}
