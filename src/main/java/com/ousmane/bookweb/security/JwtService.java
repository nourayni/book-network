package com.ousmane.bookweb.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

// import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private long jwtExpiration; // Durée d'expiration du JWT
    //@Value("${application.security.jwt.secret-key}")
    private String secreteKey; // Clé secrète pour signer le JWT

    // Extrait l'email de l'utilisateur du token JWT
    public String extractUserEmail(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    // Extrait les claims d'un token JWT en utilisant une fonction de résolution de claims
    private <T> T extractClaims(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(token);        
        return claimResolver.apply(claims);
    }

    // Extrait tous les claims d'un token JWT
    private Claims extractAllClaims(String token) {
        return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    }

    // Génère un token JWT pour un utilisateur donné
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    // Génère un token JWT avec des claims supplémentaires
    private String generateToken(HashMap<String,Object> claims, UserDetails userDetails) {
        return buildClaim(claims, userDetails, jwtExpiration);
    }

    // Construit un token JWT avec les informations d'utilisateur et la date d'expiration
    private String buildClaim(HashMap<String, Object> extraClaims, UserDetails userDetails, long jwtExpiration2) {
        var authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                    .toList();

        return Jwts
                    .builder()
                    .setClaims(extraClaims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .claim("authorities", authorities)
                    .signWith(getSignInKey())
                    .compact();
    }

    // Récupère la clé de signature en décodant la clé secrète
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secreteKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Vérifie si un token JWT est valide pour un utilisateur donné
    public boolean isTokenValid(String token, UserDetails userDetails){
        String username = extractUserEmail(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Vérifie si un token JWT est expiré
    private boolean isTokenExpired(String token) {
       return extractExpiration(token).before(new Date());
    }

    // Extrait la date d'expiration d'un token JWT
    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }
}
