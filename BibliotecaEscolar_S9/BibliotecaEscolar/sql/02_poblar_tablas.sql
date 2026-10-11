USE biblioteca_escolar;
START TRANSACTION;
INSERT INTO usuarios(id,nombre,login,contrasena,rol,rut,correo) VALUES
(1,'Bibliotecaria de demostración','admin','210000$ZGVtby1hZG1pbi0yMDI2IQ==$SicD15smzEBtvKKaEzPVl3PmccgRy8UKTF9uPN0X1PE=','BIBLIOTECARIO','11.111.111-1','biblioteca@example.com'),
(2,'Ana Pérez','ana','210000$ZGVtby1hbmEtMjAyNiEhIQ==$h0VhhmxnjeyAqvgBojlBj/QyYUpomWMD+JXLTbLQYvY=','ESTUDIANTE','20.111.222-3','ana@example.com'),
(3,'Diego Soto','diego','210000$ZGVtby1kaWVnby0yMDI2IQ==$tUAPOUAYPQ7J6GE96Rmg5OQTnFczP8eYlLHzjBbukdA=','ESTUDIANTE','20.333.444-5','diego@example.com');
INSERT INTO estudiantes(id,nombre,rut,curso,correo,usuario_id) VALUES(2,'Ana Pérez','20.111.222-3','3 medio A','ana@example.com',2),(3,'Diego Soto','20.333.444-5','4 medio B','diego@example.com',3);
INSERT INTO categorias VALUES(1,'Literatura'),(2,'Ciencias'),(3,'Historia');
INSERT INTO libros VALUES
(1,'El principito','Antoine de Saint-Exupéry','Salamandra','9788498381498',1,3),
(2,'Breve historia del tiempo','Stephen Hawking','Crítica','9788498920335',2,1),
(3,'Historia de Chile','Autor de demostración','Edición escolar','9789560000001',3,1);
INSERT INTO prestamos(id_estudiante,id_libro,fecha_prestamo,vencimiento,fecha_devolucion,dias_atraso) VALUES
(2,1,DATE_SUB(CURRENT_DATE,INTERVAL 20 DAY),DATE_SUB(CURRENT_DATE,INTERVAL 6 DAY),NULL,0),
(3,3,DATE_SUB(CURRENT_DATE,INTERVAL 30 DAY),DATE_SUB(CURRENT_DATE,INTERVAL 16 DAY),DATE_SUB(CURRENT_DATE,INTERVAL 14 DAY),2);
COMMIT;
-- Stock disponible inicial ya descuenta el préstamo activo del libro 1.
