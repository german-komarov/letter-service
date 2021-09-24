package com.german.letterservice.util.validators;


import com.german.letterservice.exceptions.PasswordNotValidException;
import com.german.letterservice.exceptions.PasswordRestoringException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RestorePasswordValidator {

    private final PasswordValidator passwordValidator;


    @Autowired
    public RestorePasswordValidator(PasswordValidator passwordValidator) {
        this.passwordValidator = passwordValidator;
    }



    public void validate(String rawNewPassword, String rawConfirmNewPassword) throws PasswordRestoringException {

        try {
            this.passwordValidator.validate(rawNewPassword);
        } catch (PasswordNotValidException e) {
            throw new PasswordRestoringException(e.getMessage());
        }


        if ( ! rawNewPassword.equals(rawConfirmNewPassword) ) {
            throw new PasswordRestoringException("New password and its confirmation are not matched");
        }




    }





}
