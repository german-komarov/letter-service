package com.german.letterservice.util.validators;

import com.german.letterservice.exceptions.PasswordNotValidException;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {


    public void validate(String rawPassword) throws PasswordNotValidException {

        if(rawPassword.length()<8 || rawPassword.length()>255) {
            throw new PasswordNotValidException("Password must be of length in [8,255] range");
        }

        if( ! rawPassword.chars().allMatch( (c)-> c>=32 && c<=126 ) ) {
            throw new PasswordNotValidException("Password can contain characters with ASCII code in [32,126] range");
        }
    }


}
