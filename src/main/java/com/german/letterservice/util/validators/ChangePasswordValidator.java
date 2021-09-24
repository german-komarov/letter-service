package com.german.letterservice.util.validators;


import com.german.letterservice.dto.ChangePasswordDto;
import com.german.letterservice.exceptions.PasswordChangingException;
import com.german.letterservice.exceptions.PasswordNotValidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordValidator {


    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    @Autowired
    public ChangePasswordValidator(BCryptPasswordEncoder passwordEncoder, PasswordValidator passwordValidator) {
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
    }


    public void validate(String encodedRealCurrentPassword,ChangePasswordDto changePasswordDto) throws PasswordChangingException {

        String rawInputCurrentPassword=changePasswordDto.getCurrentPassword();


        if ( ! this.passwordEncoder.matches(rawInputCurrentPassword,encodedRealCurrentPassword) ) {
            throw new PasswordChangingException("Wrong current password");
        }

        String rawNewPassword=changePasswordDto.getNewPassword();

        try {
            this.passwordValidator.validate(rawNewPassword);
        } catch (PasswordNotValidException e) {
            throw new PasswordChangingException(e.getMessage());
        }


        String rawConfirmNewPassword=changePasswordDto.getConfirmNewPassword();

        if ( ! rawNewPassword.equals(rawConfirmNewPassword) ) {
            throw new PasswordChangingException("New password and its confirmation are not matched");
        }




    }
}
