package services;
import com.ludus.api.dtos.CambioPasswordDto;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.UsuarioRepository;

import com.ludus.api.services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    @Test
    void updatePassword_debeActualizarContraseniaCorrectamente() {

        // 1. Preparar un usuario ficticio
        Usuario usuario = new Usuario();
        usuario.setId_usuario(4);
        usuario.setContrasenia("hashAnterior");

        // 2. Preparar los datos del cambio
        CambioPasswordDto dto = new CambioPasswordDto();
        dto.setPasswordActual("Esparta2026!");
        dto.setPasswordNueva("NuevaClave2026!");

        // 3. Simular la búsqueda del usuario
        when(repository.findById(4))
                .thenReturn(Optional.of(usuario));

        // 4. Simular la verificación de BCrypt
        when(passwordEncoder.matches(
                "Esparta2026!", "hashAnterior"))
                .thenReturn(true);

        when(passwordEncoder.matches(
                "NuevaClave2026!", "hashAnterior"))
                .thenReturn(false);

        // 5. Simular la generación del nuevo hash
        when(passwordEncoder.encode("NuevaClave2026!"))
                .thenReturn("hashNuevo");

        // 6. Ejecutar el método
        service.updatePassword(4, dto);

        // 7. Comprobar el resultado
        assertEquals("hashNuevo", usuario.getContrasenia());

        // 8. Comprobar que se guardó el usuario
        verify(repository).save(usuario);
    }
}
