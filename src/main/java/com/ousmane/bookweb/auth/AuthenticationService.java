package com.ousmane.bookweb.auth;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ousmane.bookweb.role.Role;
import com.ousmane.bookweb.role.RoleRepository;
import com.ousmane.bookweb.user.Token;
import com.ousmane.bookweb.user.TokenRepository;
import com.ousmane.bookweb.user.User;
import com.ousmane.bookweb.user.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    public void register(@Valid RegistrationRequest request) {
        // recuperer le role USER 
        var userRole = roleRepository.findByName("USER")
            .orElseThrow(()-> new UsernameNotFoundException("role non trouver"));
        // List<Role> roles = new ArrayList<>();
        // roles.add(userRole);

        var user = User.builder()
                    .firstname(request.getFirstname())
                    .lastname(request.getLastname())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .accountLocked(false)
                    .enabled(false)
                    //.roles(roles)
                    .roles(List.of(userRole))
            .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }
    private void sendValidationEmail(User user) {
       var newToken = generateAndSaveActivationToken(user);
       // send email
    }
    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationCode(6);
        var token = Token.builder()
                            .createdAt(LocalDateTime.now())
                            .expiresAt(LocalDateTime.now().plusMinutes(15))
                            .user(user)
                            .token(generatedToken)
                            .build();
        tokenRepository.save(token);
        return null;
    }
    // Méthode pour générer un code d'activation aléatoire de la longueur spécifiée
    private String generateActivationCode(int length) {
        // Chaîne de caractères contenant les chiffres de 0 à 9
        String characters = "0123456789";
        
        // Utilisation de StringBuilder pour construire le code d'activation
        StringBuilder codeBuilder = new StringBuilder();
        
        // Création d'une instance de SecureRandom pour générer des nombres aléatoires sécurisés
        SecureRandom secureRandom = new SecureRandom();
        
        // Boucle pour ajouter des caractères aléatoires jusqu'à atteindre la longueur spécifiée
        for(int i = 0; i < length; i++) {
            // Génère un index aléatoire basé sur la longueur de la chaîne de caractères
            int randomIndex = secureRandom.nextInt(characters.length());
            
            // Ajoute le caractère à l'index aléatoire à codeBuilder
            codeBuilder.append(characters.charAt(randomIndex));
        }
        
        // Convertit le StringBuilder en String et le retourne
        return codeBuilder.toString();
    }


}
