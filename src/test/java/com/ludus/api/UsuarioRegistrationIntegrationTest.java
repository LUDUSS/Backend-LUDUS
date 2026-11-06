package com.ludus.api;

import com.ludus.api.controllers.LoginController;
import com.ludus.api.controllers.UsuarioController;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.UsuarioRepository;
import com.ludus.api.security.JwtFilter;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioRegistrationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository users;
    @Autowired PasswordEncoder encoder;

    private static final String PAYLOAD = """
            {"id_usuario":999999,"nombre":"Registro","correo":"registro@test.example",
             "contrasenia":"ClaveRegistro123!","telefono":"5551234567"}
            """;

    @ParameterizedTest
    @ValueSource(strings = {"/ludus/usuario", "/ludus/usuario/"})
    void anonymousRegistrationReachesControllerAndPersists(String path) throws Exception {
        long before = users.count();
        mvc.perform(post(path).contentType("application/json").content(PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(UsuarioController.class))
                .andExpect(handler().methodName("addUsuario"))
                .andExpect(jsonPath("$.id_usuario").isNumber())
                .andExpect(jsonPath("$.contrasenia").doesNotExist());

        users.flush();
        Usuario saved = users.findByCorreo("registro@test.example").orElseThrow();
        assertEquals(before + 1, users.count());
        assertNotEquals(999999, saved.getId_usuario());
        assertEquals("Registro", saved.getNombre());
        assertEquals("5551234567", saved.getTelefono());
        assertTrue(encoder.matches("ClaveRegistro123!", saved.getContrasenia()));
        assertNotNull(saved.getFecha_registro());
        assertEquals("", saved.getSobre_mi());
    }

    @Test
    void anonymousUserReadsRemainProtected() throws Exception {
        for (String path : new String[]{"/ludus/usuario/", "/ludus/usuario/1"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/ludus/usuario/admin").contentType("application/json").content(PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void foreignUpdatesAndDeletesRemainProtected() throws Exception {
        Usuario owner = users.save(new Usuario(null,"Owner","owner@registration.test","hash","5550000001","",LocalDateTime.now()));
        Usuario other = users.save(new Usuario(null,"Other","other@registration.test","hash","5550000002","",LocalDateTime.now()));
        String path = "/ludus/usuario/" + owner.getId_usuario();
        String token = Jwts.builder().subject(other.getCorreo())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(JwtFilter.getSignInKey()).compact();

        mvc.perform(put(path).contentType("application/json").content("{\"nombre\":\"Changed\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete(path)).andExpect(status().isUnauthorized());
        mvc.perform(put(path).header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{\"nombre\":\"Changed\"}"))
                .andExpect(status().isForbidden())
                .andExpect(handler().methodName("updateUsuario"));
        mvc.perform(delete(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(handler().methodName("deleteUsuario"));
        assertEquals("Owner", users.findById(owner.getId_usuario()).orElseThrow().getNombre());
    }

    @Test
    void loginWithoutJwtStillReturnsToken() throws Exception {
        mvc.perform(post("/ludus/usuario").contentType("application/json").content(PAYLOAD))
                .andExpect(status().isOk());
        mvc.perform(post("/ludus/login").contentType("application/json")
                        .content("{\"correo\":\"registro@test.example\",\"contrasenia\":\"ClaveRegistro123!\"}"))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(LoginController.class))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/ludus/usuario", "/ludus/usuario/"})
    void invalidRegistrationReturnsValidationError(String path) throws Exception {
        long before = users.count();
        mvc.perform(post(path).contentType("application/json").content("{\"nombre\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(handler().methodName("addUsuario"))
                .andExpect(jsonPath("$.error").value("Entrada invalida"));
        assertEquals(before, users.count());
    }
}
