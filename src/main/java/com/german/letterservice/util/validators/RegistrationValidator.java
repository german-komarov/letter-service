package com.german.letterservice.util.validators;

import com.german.letterservice.dto.RegistrationDto;
import com.german.letterservice.exceptions.PasswordNotValidException;
import com.german.letterservice.exceptions.RegistrationException;
import com.german.letterservice.exceptions.UsernameNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistrationValidator {


    private final UsernameValidator usernameValidator;
    private final PasswordValidator passwordValidator;

    @Autowired
    public RegistrationValidator(UsernameValidator usernameValidator, PasswordValidator passwordValidator) {
        this.usernameValidator = usernameValidator;
        this.passwordValidator = passwordValidator;
    }


    public void validate(RegistrationDto registrationDto) throws RegistrationException {

        try {
            this.usernameValidator.validate(registrationDto.getUsername());
        } catch (UsernameNotValidException e) {
            throw new RegistrationException(e.getMessage());
        }


        String rawPassword=registrationDto.getPassword();

        try {
            this.passwordValidator.validate(rawPassword);
        } catch (PasswordNotValidException e) {
            throw new RegistrationException(e.getMessage());
        }


        String rawConfirmPassword=registrationDto.getConfirmPassword();


        if( ! rawPassword.equals(rawConfirmPassword) ) {
            throw new RegistrationException("Password and confirmation password are not matched");
        }

    }

}
