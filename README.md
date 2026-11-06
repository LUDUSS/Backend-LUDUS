# Backend-LUDUS

## Registro y autenticacion

La ruta canonica de registro es `POST /ludus/usuario`. Se admite tambien
`POST /ludus/usuario/` por compatibilidad, sin redireccion. Ambas son publicas,
al igual que `POST /ludus/login`, y no requieren `Authorization`.
El registro requiere `nombre`, `correo`, `contrasenia` y `telefono` no vacios;
los campos obligatorios ausentes o vacios devuelven 400.
Las demas operaciones sobre usuarios requieren JWT LUDUS; PUT y DELETE
requieren ademas que el usuario autenticado sea el propietario.
Bacjeknd para LUDUS, creación y consumo de APIs
