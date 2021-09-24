package com.german.letterservice.util.validators;


import com.german.letterservice.exceptions.InternalLetterSendingException;
import com.german.letterservice.util.constants.DangerousFileExtensions;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AttachedFilesValidator {

    private List<String> dangerousFileExtensions= Stream.of(DangerousFileExtensions.values()).map(Enum::name).map(String::toLowerCase).collect(Collectors.toList());

    {
        dangerousFileExtensions.add("0XE");
        dangerousFileExtensions.add("73K");
        dangerousFileExtensions.add("89K");
    }

    public void validate(List<MultipartFile> attachedFiles) throws InternalLetterSendingException {


        if(attachedFiles.size()>10) {
            throw new InternalLetterSendingException("Maximum number of attached to files is 10");
        }


        for (MultipartFile file:attachedFiles) {
            String fileName=file.getOriginalFilename();
            assert fileName != null;
            String[] fileDotDelimitedParts=fileName.split("\\.");
            String fileExtension=fileDotDelimitedParts[fileDotDelimitedParts.length-1];
            if ( dangerousFileExtensions.contains(fileExtension.toLowerCase()) ) {
                throw new InternalLetterSendingException(String.format("You cannot send file whose extension is in this following list %s", dangerousFileExtensions.toString()));
            }
         }


        long totalFilesSize=attachedFiles.stream().mapToLong(MultipartFile::getSize).sum();
        long maxAllowedTotalFilesSize= (long) (40*Math.pow(1024,2));

        if(totalFilesSize>maxAllowedTotalFilesSize) {
            throw new InternalLetterSendingException("Maximum total size of attached files is 40 MB (Megabyte)");
        }

    }




    public List<String> getDangerousFileExtensions() {
        return dangerousFileExtensions;
    }
}
