# Biblioteca escolar

Aplicación de escritorio en Java 17, Swing JFrame y MySQL. Incluye autenticación de bibliotecarios y estudiantes, CRUD de libros, categorías y estudiantes, préstamos, devoluciones e informes. Proyecto preparado para abrirse en IntelliJ IDEA.

## Inicio rápido en Windows

1. Instala o utiliza **Java 17 o superior** y **MySQL Server 8.0.16 o superior**. El servidor debe estar iniciado. MySQL Workbench es una herramienta para administrar el servidor; por sí solo no lo reemplaza.
2. Extrae **todo el ZIP**. No abras el JAR desde el interior del archivo comprimido.
3. Abre MySQL Workbench con una cuenta administradora. Ejecuta, en este orden, los archivos `sql/01_crear_tablas.sql`, `sql/02_poblar_tablas.sql` y `sql/03_usuario_local.sql`. Se crea la base `biblioteca_escolar` y una cuenta de aplicación limitada a SELECT, INSERT, UPDATE y DELETE.
4. Los scripts se ejecutan **una vez sobre una base nueva**. Si ya tienes una base con ese nombre, utiliza otra base y ajusta el nombre en los tres scripts y en la configuración; no elimines datos existentes. Los scripts no son una migración de bases previas.
5. Copia `config.example.properties` como `config.properties`. El ejemplo ya apunta a MySQL local, puerto 3306, con la cuenta creada por el tercer script. Si usas otro puerto o credenciales, edita esos valores. Si la cuenta `biblioteca_app` ya existía, el script no cambia su contraseña: utiliza sus credenciales o crea una cuenta distinta.
6. Ejecuta `iniciar.bat`. También puedes usar `java -jar BibliotecaEscolar.jar` desde la carpeta del proyecto. Mantén `lib/` junto al JAR.

| Rol | Usuario | Contraseña de demostración |
| --- | --- | --- |
| Bibliotecario | admin | Admin2026! |
| Estudiante | ana | Estudiante2026! |
| Estudiante | diego | Estudiante2026! |

Estas cuentas y la contraseña de conexión son datos de demostración local. `config.properties` está excluido de Git para evitar subir credenciales personales. Las contraseñas de los usuarios se almacenan mediante PBKDF2-HMAC-SHA256 con sal; no se guardan en texto plano.

También se admiten las variables `BIBLIOTECA_DB_URL`, `BIBLIOTECA_DB_USER` y `BIBLIOTECA_DB_PASSWORD`, que tienen prioridad sobre el archivo. No se necesita Maven para ejecutar el JAR incluido.

## Abrir y ejecutar en IntelliJ IDEA

Abre la carpeta del proyecto o su `pom.xml`, selecciona un JDK 17 o superior y espera a que Maven importe MySQL Connector/J. Ejecuta `biblioteca.Main`. La carpeta de trabajo de la configuración de ejecución debe ser la raíz del proyecto, donde está `config.properties`.

Para reconstruir el JAR en Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\compilar.ps1
```

El script usa el JDK instalado, compila con `--release 17` y genera `BibliotecaEscolar.jar`. Si `java` corresponde a un JRE sin compilador, configura un JDK en PATH. El proyecto también se puede compilar con `mvn compile` en un entorno con Maven; ese comando no sustituye el script de empaquetado del JAR.

En Linux o macOS, con Java y MySQL configurados, ejecuta `sh iniciar.sh`.

## Uso de la aplicación

- **Libros:** consulta del catálogo para todos. El bibliotecario puede crear, editar y eliminar. El stock representa ejemplares disponibles, sin contar los que están prestados. Para editar o eliminar, selecciona una fila.
- **Categorías:** CRUD completo para el bibliotecario. Crea una categoría antes de registrar libros asociados.
- **Estudiantes:** el alta crea la cuenta y el perfil en una única transacción. Al editar, una contraseña vacía conserva la actual. La eliminación borra ambos registros únicamente si no existe historial asociado.
- **Préstamos y devoluciones:** selecciona estudiante y libro y pulsa Registrar préstamo. Se calcula un vencimiento a 14 días. Para devolver, selecciona el préstamo activo y confirma. El estudiante solo opera sobre sí mismo.
- **Reportes:** libros más prestados, historial por estudiante y préstamos activos. Los estudiantes consultan únicamente su historial y sus préstamos activos. Después de elegir tipo y filtro, pulsa Ver reporte.
- **Actualizar datos:** recarga la base, útil si otra instancia modifica registros. Los reportes se limpian al recargar y se generan nuevamente a petición.

Las tablas permiten ordenar pulsando sus encabezados. Las operaciones de base de datos se ejecutan en segundo plano con un indicador de progreso. Durante cada operación se deshabilitan botones para evitar envíos repetidos; la ventana y su reloj siguen atendiendo eventos.

No se eliminan libros, categorías o estudiantes con referencias. Esto conserva la integridad y el historial. El CRUD de `PrestamoDao` existe como contrato de persistencia; la interfaz utiliza solo los flujos de préstamo y devolución para que no se borre el historial ni se altere stock fuera de una transacción.

## Estructura y decisiones

```text
src/main/java/biblioteca/
  Main.java                  Punto de entrada
  model/                     Usuario, Bibliotecario, Estudiante, Libro, Categoria, Prestamo
  view/                      LoginView y MainView con JFrame
  controller/                AppController, eventos y SwingWorker
  service/                   BibliotecaService, permisos y reglas de negocio
  dao/                       CrudDao, JdbcDao y DAO de las cinco entidades; ReporteDao
  config/                    DatabaseConnection, Singleton y transacciones
  security/                  Passwords, hash y verificación
src/test/java/biblioteca/     Integración MySQL y prueba de interfaz
sql/                         Creación, datos de ejemplo y usuario local
lib/                         MySQL Connector/J incluido
BibliotecaEscolar.jar         Ejecutable Java
```

`Usuario` es abstracta. `Bibliotecario` y `Estudiante` heredan y sobrescriben `puedeAdministrar()` y `getRol()`. El servicio trabaja con referencias de tipo `Usuario` y resuelve sus permisos por polimorfismo. `CrudDao<T>` define una interfaz común y `JdbcDao<T>` comparte infraestructura de consultas parametrizadas.

La conexión Singleton es única por aplicación. Su monitor serializa el acceso JDBC para que dos trabajadores no mezclen transacciones sobre la misma conexión. `prestar`, `devolver` y la modificación de stock usan `synchronized`. La transacción reúne el cambio de stock y el registro de préstamo, que constituye el historial. `FOR UPDATE`, la actualización condicionada por stock y las restricciones de MySQL complementan la protección entre instancias. La edición de libros comprueba si el stock cambió desde que se abrió el formulario.

Esta serialización es deliberada para cumplir el requisito académico de una conexión única. Para cargas mayores se podría utilizar un pool, pero eso cambiaría ese diseño.

## Correspondencia con la pauta y scripts del AVA

Se incluyen las cinco tablas y sus campos mínimos: `usuarios`, `estudiantes`, `libros`, `categorias` y `prestamos`. Se usan identificadores sin tildes: `contrasena` corresponde a contraseña y `titulo` a título. Se agregan `login`, `usuario_id`, `vencimiento`, `dias_atraso` y una marca interna para impedir préstamos activos duplicados.

`fecha_devolucion` guarda la devolución efectiva; `vencimiento` guarda la fecha límite. `devuelto` se calcula a partir de la devolución efectiva. El perfil de estudiante comparte su identificador con el usuario; las altas y modificaciones de ambos se confirman juntas. Se mantienen nombre, RUT y correo en ambas tablas para reflejar la estructura mínima de la pauta.

**Los scripts son propios.** El enlace del AVA indicado en la pauta no estuvo accesible durante la preparación. No se ha validado compatibilidad directa con sus scripts oficiales ni se afirma haber utilizado sus datos. Si el docente exige esos archivos exactos, descárgalos desde tu sesión del AVA y compara campos, tipos y claves antes de adaptar o importar. No ejecutes el poblamiento propio sobre la estructura oficial sin esa revisión.

## Pruebas y entrega

Consulta `PRUEBAS.md` para las 33 verificaciones ejecutadas y cómo repetirlas en una base de pruebas. El informe y guion está en `docs/Informe_y_guia_video.md`.

El video y la publicación en GitHub quedan pendientes. Antes de entregar, crea un repositorio público, sube el proyecto con commits descriptivos y copia su enlace en el AVA. No subas configuraciones con credenciales personales ni carpetas de datos de MySQL. El ZIP debe mantener juntos código, scripts, conector y ejecutable.

## Referencias técnicas

- [JDBC DriverManager con MySQL Connector/J](https://dev.mysql.com/doc/connector-j/en/connector-j-usagenotes-connect-drivermanager.html).
- [SwingWorker y ejecución en segundo plano](https://docs.oracle.com/javase/8/docs/api/javax/swing/SwingWorker.html).

## Dependencias

MySQL Connector/J 9.2.0 se distribuye sin modificar en `lib/`. Sus avisos de licencia se conservan dentro del JAR en `LICENSE` e `INFO_BIN`. Java y MySQL Server no se distribuyen dentro del ZIP.
