package com.german.letterservice.repositories;

import com.german.letterservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {




    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "insert into users_blocked_contacts (user_id, blocked_contact_id) values (:paramUserId,:paramContactId)",nativeQuery = true)
    void insertContactToBlock(@Param("paramUserId") Long userId, @Param("paramContactId") Long contactId);



    Optional<User> findByUsername(String username);

    Integer countByUsername(String username);

    Integer countByPhoneNumber(String phoneNumber);




    @Query(value =
            "select username from users " +
            "where " +
            "id in (select contact_id from users_spamming_cases_numbers_contacts " +
            "where " +
            "user_id=:paramUserId " +
            "and " +
            "spamming_cases_number>=(select maximum_allowed_spamming_cases from users where id=:paramUserId))",
            nativeQuery = true)
    List<String> findSpammersUsernames(@Param("paramUserId") Long userId);



    @Query(value =
            "select username from users " +
            "where " +
            "id " +
            "in (select blocked_contact_id from users_blocked_contacts where user_id=:paramUserId)",
            nativeQuery = true)
    List<String> findBlockedContactsUsernames(@Param("paramUserId") Long userId);


    @Query(value =
            "select count(*) from users_blocked_contacts " +
            "where user_id=:paramUserId and blocked_contact_id=:paramContactId",
            nativeQuery = true)
    Integer isContactBlockedByUser(@Param("paramUserId") Long userId, @Param("paramContactId") Long contactId);



    @Query(value =
            "select count(*) from users_blocked_contacts " +
            "where " +
            "user_id=(select id from users where username=:paramUserUsername) " +
            "and " +
            "blocked_contact_id=(select id from users where username=:paramContactUsername)",
            nativeQuery = true)
    Integer isContactBlockedByUser(@Param("paramUserUsername") String userUsername,@Param("paramContactUsername") String contactUsername);


    @Query(value =
            "select spamming_cases_number from users_spamming_cases_numbers_contacts " +
                    "where " +
                    "user_id=:paramUserId " +
                    "and " +
                    "contact_id=:paramContactId",
            nativeQuery = true)
    Optional<Integer> findContactSpammingCasesNumber(@Param("paramUserId") Long userId,@Param("paramContactId") Long contactId);




    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update users set maximum_allowed_spamming_cases=:paramValueToSet where id=:paramUserId", nativeQuery = true)
    Integer changeMaximumAllowedSpammingCasesNumber(@Param("paramUserId") Long userId, @Param("paramValueToSet") Integer valueToSet);




    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update users_spamming_cases_numbers_contacts set spamming_cases_number=0 where user_id=:paramUserId and contact_id=(select id from users where username=:paramSpammerUsername)",
            nativeQuery = true)
    Integer cancelSpammingCases(@Param("paramUserId") Long userId, @Param("paramSpammerUsername") String spammerUsername);





    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from users_blocked_contacts where user_id=:paramUserId and blocked_contact_id=:paramContactId", nativeQuery = true)
    Integer deleteContactFromBlock(@Param("paramUserId") Long userId,@Param("paramContactId") Long contactId);



}
