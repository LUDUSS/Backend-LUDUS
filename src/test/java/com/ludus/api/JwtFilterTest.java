package com.ludus.api;

import com.ludus.api.security.JwtFilter;
import com.ludus.api.security.SecurityConfig;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.CorsFilter;
import java.util.Date;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JwtFilterTest {
    private MockMvc mvc;
    @BeforeEach void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new Probe())
                .addFilters(new CorsFilter(new SecurityConfig().corsConfigurationSource()), new JwtFilter()).build();
    }
    @RestController static class Probe {
        @RequestMapping("/ludus/igdb/buscar") String search() { return "ok"; }
        @PostMapping("/ludus/login") String login() { return "ok"; }
        @PostMapping({"/ludus/usuario", "/ludus/usuario/"}) String register() { return "ok"; }
        @PostMapping("/ludus/usuario/admin") String protectedWrite() { return "ok"; }
    }
    @Test void missingTokenIs401() throws Exception {
        mvc.perform(get("/ludus/igdb/buscar")).andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"));
    }
    @Test void invalidTokensAre401() throws Exception {
        for (String value : new String[]{"Bearer invalid", "Bearer ", "prefix Bearer invalid", "Basic abc"})
            mvc.perform(get("/ludus/igdb/buscar").header("Authorization", value)).andExpect(status().isUnauthorized());
    }
    @Test void writesRemainProtected() throws Exception {
        mvc.perform(post("/ludus/igdb/buscar")).andExpect(status().isUnauthorized());
        mvc.perform(put("/ludus/igdb/buscar")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/ludus/igdb/buscar")).andExpect(status().isUnauthorized());
    }
    @Test void validTokenPasses() throws Exception {
        mvc.perform(get("/ludus/igdb/buscar").header("Authorization", "Bearer " + token(new Date(System.currentTimeMillis()+60000))))
                .andExpect(status().isOk());
    }
    @Test void expiredAndWrongSignatureAre401() throws Exception {
        mvc.perform(get("/ludus/igdb/buscar").header("Authorization", "Bearer " + token(new Date(0))))
                .andExpect(status().isUnauthorized());
        String forged = Jwts.builder().subject("test").signWith(Jwts.SIG.HS256.key().build()).compact();
        mvc.perform(get("/ludus/igdb/buscar").header("Authorization", "Bearer " + forged)).andExpect(status().isUnauthorized());
    }
    @Test void publicLoginRegistrationAndCorsStillWork() throws Exception {
        mvc.perform(post("/ludus/login")).andExpect(status().isOk());
        mvc.perform(post("/ludus/usuario")).andExpect(status().isOk());
        mvc.perform(post("/ludus/usuario/")).andExpect(status().isOk());
        mvc.perform(post("/ludus/usuario/admin")).andExpect(status().isUnauthorized());
        mvc.perform(options("/ludus/igdb/buscar").header("Origin", "http://localhost:5502")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5502"));
    }
    private String token(Date expiry) {
        return Jwts.builder().subject("test@example.com").expiration(expiry).signWith(JwtFilter.getSignInKey()).compact();
    }
}
