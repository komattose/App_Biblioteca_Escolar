-- Ejecutar con una cuenta administradora de MySQL. Credenciales SOLO de demostración local.
CREATE USER IF NOT EXISTS 'biblioteca_app'@'localhost' IDENTIFIED BY 'BibliotecaLocal2026!';
GRANT SELECT,INSERT,UPDATE,DELETE ON biblioteca_escolar.* TO 'biblioteca_app'@'localhost';
