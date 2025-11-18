-- Usuario de prueba para iniciar sesión
-- contraseña: password
INSERT INTO public.usuario (nombre, correo_electronico, contrasena, tipo_usuario)
VALUES ('Admin', 'admin@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOeIsrG3Ablj1G7a/5a0nZ3vXbJG8Gxa', 'administrador')
ON CONFLICT (correo_electronico) DO NOTHING;
