package com.ousmane.bookweb.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class RegistrationRequest {
    @NotEmpty(message = "firstname est obligatoire")
    @NotBlank(message = "firstname est obligatoire")
    private String firstname;

    @NotEmpty(message = "lastname est obligatoire")
    @NotBlank(message = "lastname est obligatoire")
    private String lastname;

    @NotEmpty(message = "email est obligatoire")
    @NotBlank(message = "email est obligatoire")
    @Email(message = "formatage de l'email incorrect")
    private String email;

    @NotEmpty(message = "password est obligatoire")
    @NotBlank(message = "password est obligatoire")
    @Size(min = 8, message = "le nombre de carracteres du password doit superieur ou egal a 8")
    private String password;
}
