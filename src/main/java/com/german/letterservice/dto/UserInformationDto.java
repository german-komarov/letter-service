package com.german.letterservice.dto;


import com.german.letterservice.entities.User;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "username")
@ToString
public class UserInformationDto {

    private String username;
    private String phoneNumber;
    private boolean isPhoneNumberConfirmed;
    private int maximumAllowedSpammingCases;


    public UserInformationDto(User originalUser) {
        this.setUsername(originalUser.getUsername());
        this.setPhoneNumber(originalUser.getPhoneNumber());
        this.setPhoneNumberConfirmed(originalUser.isPhoneNumberApproved());
        this.setMaximumAllowedSpammingCases(originalUser.getMaximumAllowedSpammingCases());
    }



}
