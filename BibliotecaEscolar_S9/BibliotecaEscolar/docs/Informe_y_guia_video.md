# Informe y guía del video de Biblioteca Escolar

## Resumen del proyecto

La aplicación permite gestionar una biblioteca escolar mediante Java, ventanas JFrame y una base de datos MySQL. El bibliotecario administra libros, categorías y estudiantes; registra préstamos y devoluciones, y consulta reportes. Los estudiantes pueden consultar el catálogo, solicitar y devolver sus propios libros y revisar su historial.

Cada préstamo descuenta un ejemplar y fija un vencimiento a 14 días. Al devolverlo se repone el stock y se registra el atraso, si existe. El historial permanece almacenado después de la devolución. Las claves foráneas impiden eliminar registros relacionados, y las validaciones evitan stock negativo, duplicados y devoluciones repetidas.

El proyecto incluye código fuente, JAR ejecutable, MySQL Connector/J, scripts de creación y poblamiento, instrucciones de instalación y pruebas. Se aprobaron **33 verificaciones de integración con MySQL**, incluyendo concurrencia, permisos, CRUD y respuesta del hilo de interfaz. También se comprobó el inicio de sesión y la carga de las pantallas de ambos roles.

## Justificación técnica para explicar con tus palabras

- **POO:** `Usuario` es una clase abstracta. `Bibliotecario` y `Estudiante` heredan y sobrescriben los métodos de permisos. El servicio utiliza referencias `Usuario`, por lo que el comportamiento se resuelve mediante polimorfismo. `CrudDao<T>` aporta una interfaz común.
- **MVC:** `model` representa los datos; `view` contiene las ventanas; `controller` atiende eventos. `service` concentra las reglas de negocio y permisos. Así, la interfaz no contiene consultas SQL.
- **DAO y JDBC:** las cinco entidades tienen DAO con operaciones CRUD. Los DAO usan consultas parametrizadas y liberan sentencias y resultados. El CRUD visible completo corresponde a libros, categorías y estudiantes.
- **Singleton:** `DatabaseConnection.getInstance()` devuelve la misma instancia y mantiene una conexión JDBC por aplicación, creada mediante `DriverManager.getConnection`.
- **Hilos y consistencia:** `SwingWorker.doInBackground()` ejecuta el trabajo de BD; `done()` actualiza la interfaz. `synchronized` protege operaciones de stock dentro de la aplicación. La transacción confirma juntos préstamo y stock, o revierte ambos. El bloqueo `FOR UPDATE` y las restricciones de MySQL complementan esa protección.

## Preparación antes de grabar

Instala la base siguiendo el README y ensaya con `admin / Admin2026!` y `ana / Estudiante2026!`. Abre IntelliJ con los archivos señalados abajo y aumenta la fuente para que se pueda leer. Usa Kaltura, verifica el micrófono y muestra tu rostro al menos en la presentación. Apunta a **4 minutos y 50 segundos**, dentro del requisito de 3 a 5 minutos.

Deja preparada una categoría, un libro y un estudiante temporales **sin préstamos** para demostrar edición y eliminación. Puedes usar ISBN `9780000000002`, usuario `demo`, contraseña `Demo2026!`, RUT `99.999.999-9` y correo `demo@example.com`. Estos datos son solo para la demostración. Para mostrar atraso, el poblamiento ya contiene un préstamo vencido de Ana; grábalo antes de devolverlo.

## Guion propuesto de 4 minutos y 50 segundos

| Tiempo | Qué mostrar y explicar |
| --- | --- |
| 0:00–0:20 | Rostro visible, tu nombre y objetivo. «Este sistema controla el catálogo, los estudiantes y la circulación de libros de una biblioteca escolar». |
| 0:20–0:40 | Ingreso como bibliotecario y navegación por las cinco pestañas. Explica brevemente que existen dos roles. |
| 0:40–1:40 | CRUD: crea una categoría y edítala; registra un libro y un estudiante. Muestra sus filas guardadas. Edita el libro y el curso del estudiante. Elimina los registros temporales, primero el libro y luego la categoría. Explica que solo se eliminan registros sin historial. Ensaya los campos para no perder tiempo escribiendo. |
| 1:40–2:20 | Préstamo de un libro disponible: muestra stock antes y después y vencimiento a 14 días. Devuélvelo y comprueba reposición. Muestra el préstamo vencido y sus días de atraso; al devolverlo quedará en el historial. |
| 2:20–2:45 | Reportes: abre libros más prestados, filtra el historial de Ana y muestra préstamos activos. Explica qué aporta cada consulta. |
| 2:45–3:05 | Cierra sesión e ingresa como Ana. Muestra que desaparecen las opciones administrativas y que solo puede operar y consultar sus propios préstamos. |
| 3:05–4:00 | IntelliJ: muestra `Usuario`, sus dos subclases y `CrudDao`. Luego recorre las carpetas MVC, un DAO y `DatabaseConnection`. Justifica herencia, polimorfismo, abstracción, interfaz, separación de responsabilidades y conexión única. |
| 4:00–4:35 | Muestra `AppController.run`, `BibliotecaService.prestar` y `DatabaseConnection.transaction`. Explica SwingWorker, synchronized, commit y rollback. Enseña el resultado de la prueba de dos solicitudes con un ejemplar: una aceptada y stock cero. No presentes clics consecutivos como si fueran una prueba simultánea. |
| 4:35–4:50 | Muestra el repositorio público ya publicado, su estructura y commits descriptivos. Cierra indicando que entregas código, SQL, conector, ejecutable y video. |

Si el ensayo supera cinco minutos, reduce la introducción y las pausas al escribir. Conserva la demostración de roles, préstamo/devolución, los tres reportes y la explicación de POO, patrones y concurrencia. No leas todas las líneas de código: señala el método y explica para qué sirve.

## Lista final según la evaluación

| Criterio | Puntos | Evidencia que debes dejar visible |
| --- | ---: | --- |
| Orientación a objetos | 15 | Clase abstracta, herencia, métodos sobrescritos y `CrudDao<T>`. |
| MVC, DAO y Singleton | 15 | Carpetas y clases concretas; explicación de su responsabilidad. |
| Concurrencia y sincronización | 15 | SwingWorker, synchronized, transacción y resultado de la prueba. |
| CRUD | 15 | Operaciones de libros, categorías y estudiantes con persistencia. |
| Interfaz JFrame | 15 | Pantallas integradas, navegación y ambos roles. |
| JDBC | 10 | Conexión real con MySQL, Singleton, consultas y manejo de errores. |
| Video | 10 | Duración de 3 a 5 minutos, sistema, IDE, justificación y rostro visible. |
| GitHub | 5 | Repositorio público, estructura ordenada y commits descriptivos. |

## Pendientes antes de entregar

Los scripts incluidos son propios y reflejan las cinco tablas y campos de la pauta, con extensiones para el funcionamiento. **El enlace a los scripts oficiales del AVA no estuvo accesible**: si debes utilizar esos archivos exactos, descárgalos desde tu cuenta y compara su estructura antes de la entrega. Las pruebas realizadas corresponden a los scripts propios.

Publica el proyecto en GitHub, comprueba que el enlace abre sin iniciar sesión y pégalo en el AVA. No publiques `config.properties` con credenciales personales. Graba y revisa el video en Kaltura, verifica voz y legibilidad, y entrega tanto el ZIP como el video. El repositorio público y el video todavía no están creados.
