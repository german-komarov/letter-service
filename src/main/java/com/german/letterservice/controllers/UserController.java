package com.german.letterservice.controllers;

import com.german.letterservice.dto.ChangePasswordDto;
import com.german.letterservice.dto.RegistrationDto;
import com.german.letterservice.dto.RestorePasswordDto;
import com.german.letterservice.dto.UserInformationDto;
import com.german.letterservice.entities.User;
import com.german.letterservice.exceptions.*;
import com.german.letterservice.services.RegistrationService;
import com.german.letterservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {


    private final UserService userService;
    private final RegistrationService registrationService;


    @Autowired
    public UserController(UserService userService, RegistrationService registrationService) {
        this.userService = userService;
        this.registrationService = registrationService;
    }


    @GetMapping("/exists")
    public ResponseEntity<Boolean> usersExistsGet(@RequestParam String username) {
        boolean exists = this.userService.doesExist(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/user-information")
    public UserInformationDto usersUserInformation(@AuthenticationPrincipal User principal) {
        UserInformationDto userInformation = this.userService.retrieveUserInformation(principal);

        return userInformation;
    }


    @GetMapping("/spammers")
    public List<String> usersSpammersGet(@AuthenticationPrincipal User principal) {
        List<String> spammersUsername=this.userService.retrieveSpammersUsername(principal);

        return spammersUsername;
    }



    @GetMapping("/blocked-contacts")
    public List<String> usersBlockedContactsGet(@AuthenticationPrincipal User principal) {
        List<String> blockedContactsUsernames=this.userService.retrieveBlockedContactsUsernames(principal);

        return blockedContactsUsernames;

    }


    @PostMapping("/registration")
    public String registrationPost(@Valid RegistrationDto registrationDto) throws RegistrationException {
        String responseString=this.registrationService.createUser(registrationDto);
        return responseString;
    }



    @PostMapping("/create-phone-approvement")
    public String usersRequestConfirmationPost(@AuthenticationPrincipal User principal, @RequestParam String phoneNumber) throws PhoneApprovementException {

        String responseString=this.userService.createPhoneApprovement(principal, phoneNumber);

        return responseString;

    }

    @PostMapping("/complete-phone-approvement")
    public String usersConfirmPhoneNumberPost(@AuthenticationPrincipal User principal, @RequestParam String confirmationCode) throws PhoneApprovementException {

        String responseString=this.userService.completePhoneApprovement(principal, confirmationCode);

        return responseString;
    }



    @PostMapping("/change-password")
    public String usersChangePasswordPost(@AuthenticationPrincipal User principal, @Valid ChangePasswordDto changePasswordDto) throws PasswordChangingException {
        String responseString=this.userService.changePassword(principal, changePasswordDto);

        return responseString;
    }


    @PostMapping("/permit-all/create-password-restoring")
    public String usersCreatePasswordRestoringPost(@RequestParam String username, @RequestParam String phoneNumber) throws PasswordRestoringException {
        String responseString=this.userService.createPasswordRestoring(username, phoneNumber);

        return responseString;
    }


    @PostMapping("/permit-all/complete-password-restoring")
    public String usersCompletePasswordRestoringPost(@Valid RestorePasswordDto restorePasswordDto) throws PasswordRestoringException {
        String responseString=this.userService.completePasswordRestoring(restorePasswordDto);

        return responseString;
    }


    @PutMapping("/block-contact")
    public String usersBlockContactPut(@AuthenticationPrincipal User principal, String contactUsername) throws BlockingContactException {
        String responseString=this.userService.blockContact(principal, contactUsername);

        return responseString;
    }



    @PutMapping("/unblock-contact")
    public String usersUnblockContactPut(@AuthenticationPrincipal User principal, String contactUsername) throws UnblockingContactException {
        String responseString=this.userService.unblockContact(principal, contactUsername);

        return responseString;
    }



    @PutMapping("/change-max-allowed-spamming-cases-number")
    public Integer usersChangeMaxAllowedSpammingCasesNumberPut(@AuthenticationPrincipal User principal, Integer number) throws IllegalValueException {
        Integer currentNumber=this.userService.changeMaxAllowedSpammingCasesNumber(principal, number);

        return currentNumber;
    }


    @PutMapping("/cancel-spamming-cases")
    public String usersCancelSpammingCasesDelete(@AuthenticationPrincipal User principal, String spammerUsername)  {
        String responseString=this.userService.cancelSpammingCases(principal, spammerUsername);

        return responseString;
    }







}
