package com.german.letterservice.services;


import com.german.letterservice.dto.ChangePasswordDto;
import com.german.letterservice.dto.RestorePasswordDto;
import com.german.letterservice.dto.UserInformationDto;
import com.german.letterservice.entities.PhoneConfirmation;
import com.german.letterservice.entities.User;
import com.german.letterservice.exceptions.*;
import com.german.letterservice.repositories.UserRepository;
import com.german.letterservice.util.constants.Responses;
import com.german.letterservice.util.holders.WebSocketSessionHolder;
import com.german.letterservice.util.validators.ChangePasswordValidator;
import com.german.letterservice.util.validators.RestorePasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    private final UserRepository userRepository;
    private final ChangePasswordValidator changePasswordValidator;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final PhoneConfirmationService phoneConfirmationService;
    private final RestorePasswordValidator restorePasswordValidator;


    @Autowired
    public UserService(UserRepository userRepository, ChangePasswordValidator changePasswordValidator, BCryptPasswordEncoder passwordEncoder, SessionService sessionService, PhoneConfirmationService phoneConfirmationService, RestorePasswordValidator restorePasswordValidator) {
        this.userRepository = userRepository;
        this.changePasswordValidator = changePasswordValidator;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.phoneConfirmationService = phoneConfirmationService;
        this.restorePasswordValidator = restorePasswordValidator;
    }





    // implementation of this method almost the same as "loadUserByUsername",
    // but it is better to not use that method manually, as it is related to Spring Security and coupled with Security Context
    public User retrieveByUsername(String username) throws UserNotFoundException {

        User user=
                this.userRepository
                        .findByUsername(username)
                        .orElseThrow(()->new UserNotFoundException("There is no user with username"));

        return user;
    }



    public UserInformationDto retrieveUserInformation(User principal) {
        User user=this.userRepository.getOne(principal.getId());

        UserInformationDto userInformationDto=new UserInformationDto(user);

        return userInformationDto;
    }




    public User checkCredentialsAndReturnUser(String username, String rawPassword) throws WrongCredentialsException {

        User user;

        try {
            user=this.retrieveByUsername(username);
        } catch (UserNotFoundException e) {
            throw new WrongCredentialsException(e.getMessage());
        }


        String encodedPassword=user.getPassword();


        if ( ! this.passwordEncoder.matches(rawPassword, encodedPassword) ) {
            throw new WrongCredentialsException("Wrong password for this user");
        }


        return user;

    }





    public List<String> retrieveSpammersUsername(User principal) {

        List<String> spammersUsernames=this.userRepository.findSpammersUsernames(principal.getId());

        return spammersUsernames;

    }


    public List<String> retrieveBlockedContactsUsernames(User principal) {

        List<String> blockedContactsUsernames=this.userRepository.findBlockedContactsUsernames(principal.getId());

        return blockedContactsUsernames;

    }




    public boolean doesExist(String username) {

        if ( ! username.endsWith("#lets") ) {
            username = username.concat("#lets");
        }

        boolean doesExist=this.userRepository.countByUsername(username)==1;
        return doesExist;
    }

    public boolean isPhoneNumberTaken(String phoneNumber) {
        boolean isTaken=this.userRepository.countByPhoneNumber(phoneNumber)==1;

        return isTaken;
    }


    public boolean isContactBlockedByUser(Long userId, Long contactId) {
        boolean isBlocked=this.userRepository.isContactBlockedByUser(userId,contactId)==1;
        return isBlocked;
    }

    public boolean isContactBlockedByUser(String userUsername,String contactUsername) {
        boolean isBlocked=this.userRepository.isContactBlockedByUser(userUsername,contactUsername)==1;
        return isBlocked;
    }


    public Integer howManySpammingCases(Long userId, Long contactId){


        Integer spammingCasesNumber = this.userRepository.findContactSpammingCasesNumber(userId, contactId).orElse(0);

        return spammingCasesNumber;


    }






    public User save(User userToSave){
        User savedUser=this.userRepository.save(userToSave);
        return savedUser;
    }


    public void insertContactToBLock(Long userId, Long contactId) {
        this.userRepository.insertContactToBlock(userId,contactId);
    }




    public String createPhoneApprovement(User principal, String phoneNumber) throws PhoneApprovementException {
        if ( this.isPhoneNumberTaken(phoneNumber) ) {
            throw new PhoneApprovementException("There is already an account with such phone number");
        }


        PhoneConfirmation phoneConfirmation=this.phoneConfirmationService.create(principal, phoneNumber);

        String responseString;

        try {
            responseString=this.phoneConfirmationService.sendConfirmationCode(phoneNumber, phoneConfirmation.getConfirmationCode());
        } catch (PhoneConfirmationException e) {
            throw new PhoneApprovementException(e.getMessage());
        }

        return responseString;

    }



    @Transactional(noRollbackFor = PhoneApprovementException.class)
    public String completePhoneApprovement(User principal, String inputConfirmationCode) throws PhoneApprovementException {

        PhoneConfirmation phoneConfirmation;

        try {
            phoneConfirmation=this.phoneConfirmationService.retrieveByUsername(principal.getUsername());
        } catch (PhoneConfirmationNotFoundException e) {
            throw new PhoneApprovementException(e.getMessage());
        }

        boolean isNotExpired=this.phoneConfirmationService.isNotExpired(phoneConfirmation);

        if ( ! isNotExpired ) {
            this.phoneConfirmationService.delete(phoneConfirmation);
            throw new PhoneApprovementException("You phone confirmation has been expired, request for approvement again");
        }

        if ( phoneConfirmation.getConfirmationCode().equals(inputConfirmationCode) ) {
            User user=this.userRepository.getOne(principal.getId());

            user.setPhoneNumber(phoneConfirmation.getPhoneNumber());
            user.setPhoneNumberApproved(true);

            this.save(user);

            this.phoneConfirmationService.delete(phoneConfirmation);

            return Responses.PHONE_NUMBER_APPROVED.name();
        }


        else {

            if( phoneConfirmation.getAttempts()==2 ) {
                this.phoneConfirmationService.delete(phoneConfirmation);
                throw new PhoneApprovementException("You have reached 3rd fail attempt and your phone confirmation was deleted, request for approvement again");
            }

            else {
                phoneConfirmation.setAttempts(phoneConfirmation.getAttempts()+1);
                this.phoneConfirmationService.save(phoneConfirmation);

                throw new PhoneApprovementException("Confirmation code is wrong, try again");
            }

        }

    }




    public String changePassword(User principal, ChangePasswordDto changePasswordDto) throws PasswordChangingException{

        User user=this.userRepository.getOne(principal.getId());

        this.changePasswordValidator.validate(user.getPassword(),changePasswordDto);

        String rawNewPassword=changePasswordDto.getNewPassword();

        String encodedNewPassword=this.passwordEncoder.encode(rawNewPassword);


        user.setPassword(encodedNewPassword);

        user=this.save(user);


        try {
            WebSocketSessionHolder.closeSessions(user.getUsername());
        } catch (IOException e) {
            e.printStackTrace();
            throw new PasswordChangingException("Something went wrong in the server");
        }

        this.sessionService.expireAllUserSessions(user);



        return Responses.PASSWORD_CHANGED.name();

    }



    public String createPasswordRestoring(String username, String phoneNumber) throws PasswordRestoringException {

        User user;

        try {
            user=this.retrieveByUsername(username);
        } catch (UserNotFoundException e) {
            throw new PasswordRestoringException(e.getMessage());
        }



        if ( ! phoneNumber.equals(user.getPhoneNumber()) ) {
            throw new PasswordRestoringException("Account with such username is not bind to this phone number");
        }


        PhoneConfirmation phoneConfirmation=this.phoneConfirmationService.create(user, user.getPhoneNumber());


        String responseString;


        try {
            responseString=this.phoneConfirmationService.sendConfirmationCode(phoneNumber, phoneConfirmation.getConfirmationCode());
        } catch (PhoneConfirmationException e) {
            throw new PasswordRestoringException(e.getMessage());
        }


        return responseString;

    }





    @Transactional(noRollbackFor = PasswordRestoringException.class)
    public String completePasswordRestoring(RestorePasswordDto restorePasswordDto) throws PasswordRestoringException {

        PhoneConfirmation phoneConfirmation;

        try {
            phoneConfirmation=this.phoneConfirmationService.retrieveByUsername(restorePasswordDto.getUsername());
        } catch (PhoneConfirmationNotFoundException e) {
            throw new PasswordRestoringException(e.getMessage());
        }



        boolean isNotExpired=this.phoneConfirmationService.isNotExpired(phoneConfirmation);

        if ( ! isNotExpired ) {
            throw new PasswordRestoringException("You phone confirmation has been expired, request for approvement again");
        }


        if (  phoneConfirmation.getConfirmationCode().equals(restorePasswordDto.getConfirmationCode()) ) {

            this.restorePasswordValidator.validate(restorePasswordDto.getNewPassword(), restorePasswordDto.getConfirmNewPassword());

            String rawNewPassword=restorePasswordDto.getNewPassword();

            String encodedNewPassword=this.passwordEncoder.encode(rawNewPassword);

            User user;

            try {
                user=this.retrieveByUsername(restorePasswordDto.getUsername());
            } catch (UserNotFoundException e) {
                throw new PasswordRestoringException(e.getMessage());
            }

            user.setPassword(encodedNewPassword);

            user=this.save(user);


            try {
                WebSocketSessionHolder.closeSessions(user.getUsername());
            } catch (IOException e) {
                e.printStackTrace();
                throw new PasswordRestoringException("Something went wrong in the server");
            }

            this.sessionService.expireAllUserSessions(user);

            this.phoneConfirmationService.delete(phoneConfirmation);


            return Responses.PASSWORD_CHANGED.name();
        }

        else {

            if( phoneConfirmation.getAttempts()==2 ) {
                this.phoneConfirmationService.delete(phoneConfirmation);
                throw new PasswordRestoringException("You have reached 3rd fail attempt and your phone confirmation was deleted, request for approvement again");
            }

            else {
                phoneConfirmation.setAttempts(phoneConfirmation.getAttempts()+1);
                this.phoneConfirmationService.save(phoneConfirmation);

                throw new PasswordRestoringException("Confirmation code is wrong, try again");
            }

        }




    }




    public Integer changeMaxAllowedSpammingCasesNumber(User principal, final Integer number) throws IllegalValueException {
        if ( number<=0 ) {
            throw new IllegalValueException("You cannot change max allowed spamming cases number to zero or negative value");
        }

        this.userRepository.changeMaximumAllowedSpammingCasesNumber(principal.getId(), number);


        return number;


    }



    public String cancelSpammingCases(User principal, String spammerUsername) {


        spammerUsername=spammerUsername.endsWith("#lets") ? spammerUsername : spammerUsername.concat("#lets");

        this.userRepository.cancelSpammingCases(principal.getId(), spammerUsername);

        return Responses.SPAMMING_CASES_CANCELED.name();



    }



    public String blockContact(User principal, String contactUsername) throws BlockingContactException {

        User user=this.userRepository.getOne(principal.getId());

        User contact;


        contactUsername = contactUsername.endsWith("#lets") ? contactUsername : contactUsername.concat("#lets");

        try {
            contact=this.retrieveByUsername(contactUsername);
        } catch (UserNotFoundException e) {
            throw new BlockingContactException(e.getMessage());
        }


        if ( user.equals(contact) ) {
            throw new BlockingContactException("You cannot block yourself");
        }


        if ( this.isContactBlockedByUser(user.getId(),contact.getId()) ) {
            throw new BlockingContactException("This contact is already blocked");
        }


        this.insertContactToBLock(user.getId(),contact.getId());


        return String.format("%s was successfully blocked",contactUsername);

    }


    public String unblockContact(User principal, String contactUsername) throws UnblockingContactException {

        User user=this.userRepository.getOne(principal.getId());

        User contact;

        contactUsername = contactUsername.endsWith("#lets") ? contactUsername : contactUsername.concat("#lets");

        try {
            contact=this.retrieveByUsername(contactUsername);
        } catch (UserNotFoundException e) {
            throw new UnblockingContactException(e.getMessage());
        }


        if ( user.equals(contact) ) {
            throw new UnblockingContactException("You cannot block/unblock yourself");
        }

        if ( ! this.isContactBlockedByUser(user.getId(),contact.getId()) ) {
            throw new UnblockingContactException("This contact is not blocked by you");
        }


        this.deleteContactFromBlock(user.getId(), contact.getId());


        return String.format("%s was successfully unblocked", contactUsername);

    }




    public Integer addSpammingCase(User user, String contactUsername) throws UserNotFoundException {

        // We retrieve user as in case if user is principal, it may consist its session credentials that is out of date
        user=this.retrieveByUsername(user.getUsername());

        User spammer=this.retrieveByUsername(contactUsername);


        Integer spammingCasesNumber=user.addSpammingCase(spammer);

        this.save(user);

        return spammingCasesNumber;

    }


    public Integer removeSpammingCase(User user, String contactUsername) throws UserNotFoundException {

        // We retrieve user as in case if user is principal, it may consist its session credentials that is out of date
        user=this.retrieveByUsername(user.getUsername());

        User notSpammer=this.retrieveByUsername(contactUsername);

        Integer spammingCasesNumber=user.removeSpammingCase(notSpammer);

        this.save(user);


        return spammingCasesNumber;

    }






    public Integer deleteContactFromBlock(Long userId, Long contactId) {


        Integer deletedObjectsNumber=this.userRepository.deleteContactFromBlock(userId,contactId);

        return deletedObjectsNumber;
    }


}



