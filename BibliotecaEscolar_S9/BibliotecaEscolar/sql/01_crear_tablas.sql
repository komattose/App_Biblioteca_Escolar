-- Esquema propio basado en las cinco tablas y campos indicados en la pauta.
-- Identificadores sin tildes: contrasena equivale a contraseña y titulo a título.
-- Requiere MySQL 8.0.16 o superior. Ejecutar una sola vez sobre una base nueva.
CREATE DATABASE IF NOT EXISTS biblioteca_escolar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca_escolar;
CREATE TABLE usuarios (
 id INT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(120) NOT NULL,
 rut VARCHAR(20) NOT NULL, correo VARCHAR(120) NOT NULL,
 contrasena VARCHAR(255) NOT NULL, rol ENUM('BIBLIOTECARIO','ESTUDIANTE') NOT NULL,
 login VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB;
CREATE TABLE estudiantes (
 id INT PRIMARY KEY, nombre VARCHAR(120) NOT NULL, rut VARCHAR(20) NOT NULL UNIQUE,
 curso VARCHAR(40) NOT NULL, correo VARCHAR(120) NOT NULL, usuario_id INT NOT NULL UNIQUE,
 FOREIGN KEY(usuario_id) REFERENCES usuarios(id), CHECK(id=usuario_id)
) ENGINE=InnoDB;
CREATE TABLE categorias (id INT PRIMARY KEY AUTO_INCREMENT,nombre VARCHAR(80) NOT NULL UNIQUE) ENGINE=InnoDB;
CREATE TABLE libros (
 id INT PRIMARY KEY AUTO_INCREMENT,titulo VARCHAR(150) NOT NULL,autor VARCHAR(120) NOT NULL,
 editorial VARCHAR(120) NOT NULL,isbn VARCHAR(30) NOT NULL UNIQUE,id_categoria INT NOT NULL,
 stock INT NOT NULL CHECK(stock>=0),FOREIGN KEY(id_categoria) REFERENCES categorias(id)
) ENGINE=InnoDB;
CREATE TABLE prestamos (
 id INT PRIMARY KEY AUTO_INCREMENT,id_estudiante INT NOT NULL,id_libro INT NOT NULL,
 fecha_prestamo DATE NOT NULL,fecha_devolucion DATE NULL,
 devuelto BOOLEAN GENERATED ALWAYS AS (fecha_devolucion IS NOT NULL) STORED,
 vencimiento DATE NOT NULL,dias_atraso INT NOT NULL DEFAULT 0,
 activo TINYINT GENERATED ALWAYS AS (CASE WHEN fecha_devolucion IS NULL THEN 1 ELSE NULL END) STORED,
 UNIQUE KEY un_activo(id_estudiante,id_libro,activo),
 FOREIGN KEY(id_estudiante) REFERENCES estudiantes(id),FOREIGN KEY(id_libro) REFERENCES libros(id),
 CHECK(vencimiento>=fecha_prestamo),CHECK(fecha_devolucion IS NULL OR fecha_devolucion>=fecha_prestamo),CHECK(dias_atraso>=0)
) ENGINE=InnoDB;
-- Prestamos conserva el historial. Devuelto se calcula para evitar estados contradictorios.
