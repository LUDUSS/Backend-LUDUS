package com.ludus.api.services;

import com.ludus.api.dtos.CambioPasswordDto;
import com.ludus.api.models.Usuario;
import com.ludus.api.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import com.ludus.api.dtos.RegistroUsuarioDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }// constructor

    public List<Usuario> getUsuario(){
        return  repository.findAll(); // -> QUERY (SELECT * FROM Usuario)
    }

    public Usuario getUsuario(Integer id_usuario) {
        return repository
                .findById(id_usuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario con el ID ["+ id_usuario +"] no existe"));
    }// fin getUsuario(id)

    public Usuario deleteUsuario (Integer id_usuario){
        Usuario usuario = null;
        if(repository.existsById(id_usuario)){// buscar el usuario por id
            usuario = repository.findById(id_usuario).get();
            repository.delete(usuario);
        }// fin if
        return usuario;
    }// fin deleteUsuario



    public Usuario addUsuario(RegistroUsuarioDto dto) {

        // 1. Validar que se recibieron todos los datos
        if (dto == null ||
                dto.getNombre() == null || dto.getNombre().isBlank() ||
                dto.getCorreo() == null || dto.getCorreo().isBlank() ||
                dto.getContrasenia() == null || dto.getContrasenia().isBlank() ||
                dto.getTelefono() == null || dto.getTelefono().isBlank()) {

            throw new IllegalArgumentException(
                    "Todos los campos del registro son obligatorios."
            );
        }

        // 2. Validar la seguridad de la contraseña
        validarContrasenia(dto.getContrasenia());

        // 3. Normalizar el correo
        String correo = dto.getCorreo().trim().toLowerCase();

        // 4. Verificar que no exista otro usuario con el mismo correo
        if (repository.findByCorreo(correo).isPresent()) {
            throw new IllegalArgumentException(
                    "El correo electrónico ya está registrado."
            );
        }

        // 5. Crear una nueva entidad Usuario
        Usuario usuario = new Usuario();

        usuario.setNombre(dto.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(dto.getTelefono().trim());

        // 6. Generar el hash de la contraseña mediante BCrypt
        usuario.setContrasenia(
                passwordEncoder.encode(dto.getContrasenia())
        );

        // 7. Inicializar la biografía
        usuario.setSobre_mi("");

        // 8. Generar la fecha de registro
        usuario.setFecha_registro(LocalDateTime.now());

        // 9. Guardar el usuario en MySQL
        return repository.save(usuario);

    }// addUsuario


    public Usuario updateUsuario(
            Integer idUsuario,
            String nombre,
            String correo,
            String telefono,
            String sobre_mi) {

        // 1. Buscar el usuario en la base de datos
        Usuario usuario = repository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario con ID [" + idUsuario + "] no existe."
                ));

        // 2. Actualizar nombre
        if (nombre != null && !nombre.isBlank()) {
            usuario.setNombre(nombre.trim());
        }

        // 3. Actualizar correo
        if (correo != null && !correo.isBlank()) {

            String correoNormalizado = correo.trim().toLowerCase();

            // Verificar que el correo no pertenezca a otro usuario
            repository.findByCorreo(correoNormalizado)
                    .ifPresent(existente -> {

                        if (!existente.getId_usuario().equals(idUsuario)) {
                            throw new IllegalArgumentException(
                                    "El correo electrónico ya está registrado."
                            );
                        }

                    });

            usuario.setCorreo(correoNormalizado);
        }

        // 4. Actualizar teléfono
        if (telefono != null && !telefono.isBlank()) {
            usuario.setTelefono(telefono.trim());
        }

        // 5. Actualizar biografía
        if (sobre_mi != null) {
            usuario.setSobre_mi(sobre_mi);
        }

        // 6. Guardar los cambios
        return repository.save(usuario);

    }// updateUsuario


    public void updatePassword(Integer idUsuario, CambioPasswordDto dto) {

        // 1. Validar que se recibieron ambas contraseñas
        if (dto == null ||
                dto.getPasswordActual() == null ||
                dto.getPasswordActual().isBlank() ||
                dto.getPasswordNueva() == null ||
                dto.getPasswordNueva().isBlank()) {

            throw new IllegalArgumentException(
                    "Debes proporcionar la contraseña actual y la nueva."
            );
        }

        // 2. Buscar al usuario
        Usuario usuario = repository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario con ID [" + idUsuario + "] no existe."
                ));

        // 3. Verificar la contraseña actual
        if (!passwordEncoder.matches(
                dto.getPasswordActual(),
                usuario.getContrasenia())) {

            throw new IllegalArgumentException(
                    "La contraseña actual es incorrecta."
            );
        }

        // 4. Comprobar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(
                dto.getPasswordNueva(),
                usuario.getContrasenia())) {

            throw new IllegalArgumentException(
                    "La nueva contraseña debe ser diferente de la actual."
            );
        }

        // 5. Validar la seguridad de la nueva contraseña
        String nueva = dto.getPasswordNueva();

        validarContrasenia(nueva);

        // 6. Generar el nuevo hash mediante BCrypt
        usuario.setContrasenia(
                passwordEncoder.encode(nueva)
        );

        // 7. Guardar la contraseña actualizada
        repository.save(usuario);

    }// updatePassword

    private void validarContrasenia(String contrasenia) {

        // 1. Verificar que la contraseña no sea nula ni vacía
        if (contrasenia == null || contrasenia.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña no puede estar vacía."
            );
        }

        // 2. Validar los requisitos de seguridad
        if (contrasenia.length() < 8 ||
                !contrasenia.matches(".*[A-Z].*") ||
                !contrasenia.matches(".*[a-z].*") ||
                !contrasenia.matches(".*[0-9].*") ||
                !contrasenia.matches(".*[^A-Za-z0-9].*")) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres, " +
                            "una mayúscula, una minúscula, un número " +
                            "y un carácter especial."
            );
        }

    }// validarContrasenia
}// fin class UsuarioService
