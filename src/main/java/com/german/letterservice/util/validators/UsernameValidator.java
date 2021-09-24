package com.german.letterservice.util.validators;

import com.german.letterservice.exceptions.UsernameNotValidException;
import org.springframework.stereotype.Component;

@Component
public class UsernameValidator{




    public void validate(String rawUsername) throws UsernameNotValidException {


        if ( rawUsername==null ) {
            throw new UsernameNotValidException("Username cannot be null");
        }

        rawUsername=rawUsername.trim();

        if(rawUsername.length()<5 || rawUsername.length()>255) {
            throw new UsernameNotValidException("Username must be of length between 8 and 255");
        }

        if( ! rawUsername.chars().allMatch( (c) -> (c>=65 && c<=90) || (c>=97 && c<=122) || (c>=48 && c<=57) || c==46 ) ) {
            throw new UsernameNotValidException("Username can contain only English alphabetical characters in lower/upper case, digits and points");
        }



        char firstChar=rawUsername.charAt(0);

        if ( (firstChar>=48 && firstChar<=57) || firstChar==46 ) {
            throw new UsernameNotValidException("Username cannot start with digit or point");
        }


    }





}
