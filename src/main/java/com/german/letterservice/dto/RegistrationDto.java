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
@ToString
public class RegistrationDto {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 5,max = 80,message = "Username must be of length in [5,80] range")
    private String username;

    @NotNull(message = "Password cannot be null")
    @Size(min=8,max = 255,message = "Password must be of length in [8,255] range")
    private String password;

    @NotNull(message = "Confirmation password cannot be null")
    @Size(min = 8, max = 255,message = "Confirmation password must be of length in [8,255] range")
    private String confirmPassword;


}
