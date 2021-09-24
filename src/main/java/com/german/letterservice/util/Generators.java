package com.german.letterservice.util;


import java.util.UUID;

public class Generators {

    public static String generateUniqueCode() {
        String uniqueCode = UUID.randomUUID().toString().replace("-","");

        return uniqueCode;
    }

    public static String generaConfirmationCode() {
        String confirmationCode = UUID.randomUUID().toString().replace("-","").substring(0,8).toLowerCase();

        return confirmationCode;
    }


}
