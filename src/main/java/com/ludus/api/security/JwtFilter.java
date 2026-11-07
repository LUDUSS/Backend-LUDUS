package com.ludus.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.filter.GenericFilterBean;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JwtFilter extends GenericFilterBean {
    public static String signature = "¡D3finiend0-Nuestr4Clav3S3cr3t4-PArA-nuestr4-ap1!";
    public static final String secret = Base64.getEncoder().encodeToString(signature.getBytes(StandardCharsets.UTF_8));

    public static SecretKey getSignInKey(){
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        if(requiereAutenticacion(httpRequest)){
            String token = obtenerToken(httpRequest);
            validarToken(token);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean requiereAutenticacion(HttpServletRequest httpRequest) {
        String metodo = httpRequest.getMethod();
        String URI = httpRequest.getRequestURI();

        if (metodo.equalsIgnoreCase("OPTIONS")) {
            return false;
        }


        // Solo login y registro son publicos; la busqueda IGDB conserva el JWT LUDUS.
        // Comparar rutas exactas evita omitir autenticacion en subrutas de usuario/login.
        if ((metodo.equals("POST") && (URI.equals("/ludus/login") || URI.equals("/ludus/login/")))
                || (metodo.equals("POST") && (URI.equals("/ludus/usuario") || URI.equals("/ludus/usuario/")))) {
            return false; // No requiere token
        }
        return true; // Todo lo demás requiere token
    }

    private String obtenerToken(HttpServletRequest httpRequest) throws ServletException {
        String authHeader = httpRequest.getHeader("Authorization");
        if(authHeader == null || !authHeader.contains("Bearer ")){
            throw new ServletException("Token invalido. Encabezado nulo o no contiene {Bearer TOKEN}");
        }
        return authHeader.substring(7);
    }

    private void validarToken(String token) throws ServletException{
        try {
            Claims claims = Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            System.out.println("Usuario autenticado: " + claims.getSubject());
        }catch (SignatureException | MalformedJwtException | ExpiredJwtException e){
            throw new ServletException("Token invalido. Verifica la firma o que no esté expirado.");
        }
    }
}