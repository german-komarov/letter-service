package com.german.letterservice.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ChangePasswordDto {


    @JsonValue
    @NotNull(message = "Current password cannot be null")
    @Size(min = 8, max = 255,message = "Current password must be of length in [8,255] range")
    private String currentPassword;


    @NotNull(message = "New password cannot be null")
    @Size(min = 8, max = 255,message = "New password must be of length in [8,255] range")
    private String newPassword;


    @NotNull(message = "Confirm new password cannot be null")
    @Size(min = 8, max = 255,message = "Confirm new password must be of length in [8,255] range")
    private String confirmNewPassword;


}
