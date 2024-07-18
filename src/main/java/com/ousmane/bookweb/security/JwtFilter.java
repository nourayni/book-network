package com.ousmane.bookweb.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService; // Service pour gérer les opérations JWT
    private final UserDetailsServiceImpl userDetailsService; // Service pour charger les détails de l'utilisateur

    // Méthode principale qui filtre les requêtes HTTP
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                @NonNull HttpServletResponse response, 
                @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        // La route /api/v1/auth ne demande pas d'autorisation, donc le filtre ne s'applique pas
        if (request.getServletPath().contains("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Récupère l'en-tête d'autorisation de la requête
        String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Vérifie si l'en-tête est null ou ne commence pas par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrait le token JWT de l'en-tête d'autorisation
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUserEmail(jwt);

        // Vérifie si l'email de l'utilisateur est non null et que l'utilisateur n'est pas déjà authentifié
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Vérifie si le token JWT est valide
            if (jwtService.isTokenValid(userEmail, userDetails)) {
                // Crée un jeton d'authentification UsernamePasswordAuthenticationToken
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                // Associe des détails supplémentaires de la requête HTTP
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Définit l'authentification dans le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Passe la requête et la réponse au filtre suivant dans la chaîne
        filterChain.doFilter(request, response);
    }
}
