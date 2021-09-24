package com.german.letterservice.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;



@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "username")
public class RestorePasswordDto {


    @NotBlank(message = "Username cannot be blank")
    @Size(min = 10,max = 85, message = "Username must be of length in [10,85] range")
    private String username;


    @NotBlank(message = "Confirmation Code cannot be blank")
    private String confirmationCode;

    @NotNull(message = "New password cannot be null")
    @Size(min = 8, max = 255,message = "New password must be of length in [8,255] range")
    private String newPassword;


    @NotNull(message = "Confirm new password cannot be null")
    @Size(min = 8, max = 255,message = "Confirm new password must be of length in [8,255] range")
    private String confirmNewPassword;

}
