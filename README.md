Sistema de Gestión de Biblioteca Escolar
Aplicación de escritorio desarrollada en Java, Swing y MySQL para administrar una biblioteca escolar. Permite gestionar libros, categorías y estudiantes, registrar préstamos y devoluciones, y consultar reportes.
Proyecto realizado para la evaluación final de Desarrollo Orientado a Objetos II.
Funcionalidades
Bibliotecario
- Crear, consultar, modificar y eliminar libros, categorías y estudiantes.
- Registrar cuentas de acceso para estudiantes.
- Registrar préstamos verificando la disponibilidad de ejemplares.
- Gestionar devoluciones y registrar días de atraso.
- Consultar libros más prestados, historial por estudiante y préstamos activos.
Estudiante
- Consultar el catálogo de libros.
- Solicitar préstamos y registrar sus propias devoluciones.
- Consultar su historial y sus préstamos activos.
Los permisos se validan tanto en la interfaz como en la lógica de negocio.
Tecnologías
- Java 17 o superior.
- Swing y JFrame.
- MySQL Server 8.0.16 o superior.
- JDBC y MySQL Connector/J 9.2.0.
- Maven para la gestión de dependencias.
- IntelliJ IDEA como entorno de desarrollo.
Arquitectura y orientación a objetos
El proyecto utiliza los siguientes patrones:
Patrón	Implementación
MVC	Separación de modelos, vistas y controladores.
DAO	Clases dedicadas al acceso y persistencia de cada entidad.
Singleton	DatabaseConnection administra una única instancia y conexión JDBC por aplicación.


La clase abstracta Usuario es la base de Bibliotecario y Estudiante. Ambas especializan sus permisos mediante métodos sobrescritos, aplicando herencia, abstracción y polimorfismo.
La interfaz genérica CrudDao<T> establece el contrato de las operaciones CRUD.
Concurrencia e integridad de datos
Las operaciones de base de datos se ejecutan en segundo plano mediante SwingWorker, manteniendo disponible el hilo de eventos de la interfaz.
Para proteger la consistencia del stock se utilizan:
- Métodos synchronized en operaciones críticas.
- Transacciones JDBC con commit y rollback.
- Bloqueo de filas mediante SELECT ... FOR UPDATE.
- Validaciones y restricciones en MySQL.
El registro del préstamo y el descuento de stock se confirman juntos. Si la operación falla, los cambios se revierten.
Cada préstamo tiene un plazo de 14 días. La devolución repone el ejemplar, conserva el historial y registra el atraso cuando corresponde.
Estructura del proyecto
BibliotecaEscolar/
├── src/
│   ├── main/java/biblioteca/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dao/
│   │   ├── model/
│   │   ├── security/
│   │   ├── service/
│   │   ├── view/
│   │   └── Main.java
│   └── test/java/biblioteca/
├── sql/
│   ├── 01_crear_tablas.sql
│   ├── 02_poblar_tablas.sql
│   └── 03_usuario_local.sql
├── lib/
├── docs/
├── BibliotecaEscolar.jar
├── config.example.properties
├── compilar.ps1
├── probar.ps1
├── iniciar.bat
├── iniciar.sh
├── pom.xml
└── PRUEBAS.md
Instalación
1. Descargar el proyecto
Clona este repositorio o descarga el ZIP y extrae todos sus archivos.
2. Configurar MySQL
Comprueba que el servidor MySQL esté en ejecución. Desde MySQL Workbench, ingresa con una cuenta administradora y ejecuta los siguientes scripts en orden:
1. sql/01_crear_tablas.sql
2. sql/02_poblar_tablas.sql
3. sql/03_usuario_local.sql
Estos scripts crean la base biblioteca_escolar, sus tablas, los datos de demostración y la cuenta de conexión de la aplicación.
Ejecuta los scripts una sola vez sobre una base nueva. Si ya existe una base con ese nombre y contiene datos, revisa su estructura antes de continuar.
3. Configurar la conexión
Copia config.example.properties y renombra la copia como config.properties. Déjala en la raíz del proyecto, junto a pom.xml.
Para la configuración local de demostración:
db.url=jdbc:mysql://localhost:3306/biblioteca_escolar?connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=15000
db.user=biblioteca_app
db.password=BibliotecaLocal2026!
Ajusta el puerto y las credenciales si tu instalación utiliza otros valores.
La cuenta biblioteca_app corresponde a la conexión con MySQL. Es distinta de las cuentas utilizadas para ingresar a la interfaz.
También pueden utilizarse estas variables de entorno, que tienen prioridad sobre el archivo:
BIBLIOTECA_DB_URL
BIBLIOTECA_DB_USER
BIBLIOTECA_DB_PASSWORD
Ejecución
Desde IntelliJ IDEA
1. Abre la carpeta del proyecto o su archivo pom.xml.
2. Selecciona un JDK 17 o superior.
3. Espera a que Maven cargue las dependencias.
4. Configura la raíz del proyecto como directorio de trabajo.
5. Ejecuta la clase biblioteca.Main.
Desde el ejecutable
En Windows, abre:
iniciar.bat
También puedes ejecutar desde la raíz del proyecto:
java -jar BibliotecaEscolar.jar
Mantén la carpeta lib junto al JAR y el servidor MySQL en ejecución.
Cuentas de demostración
Rol	Usuario	Contraseña
Bibliotecario	admin	Admin2026!
Estudiante	ana	Estudiante2026!
Estudiante	diego	Estudiante2026!


Estas cuentas se crean con el script de poblamiento y están destinadas a pruebas locales.
Base de datos
El sistema utiliza cinco tablas principales:
Tabla	Propósito
usuarios	Credenciales, información personal y roles.
estudiantes	Información académica y vinculación con la cuenta de acceso.
categorias	Clasificación de los libros.
libros	Catálogo y stock disponible.
prestamos	Préstamos, vencimientos, devoluciones e historial de atrasos.


Las claves foráneas impiden eliminar registros relacionados con el historial. El campo stock representa los ejemplares disponibles.
Los scripts incluidos son propios, elaborados a partir de la estructura descrita en la pauta académica. No se garantiza su compatibilidad directa con los scripts externos del AVA.
Seguridad y validaciones
- Contraseñas almacenadas mediante PBKDF2-HMAC-SHA256 con sal.
- Consultas parametrizadas mediante PreparedStatement.
- Restricción de funciones según el rol.
- Protección del historial de otros estudiantes.
- Validación de campos obligatorios y stock no negativo.
- Rechazo de préstamos activos duplicados del mismo libro para un estudiante.
- Prevención de devoluciones repetidas.
- Protección frente a ediciones que intenten sobrescribir un stock desactualizado.
El archivo config.properties está excluido mediante .gitignore. No publiques credenciales personales. Las contraseñas incluidas en los ejemplos son únicamente de demostración.
Compilación
Para reconstruir el JAR en Windows, ejecuta desde la raíz:
powershell -ExecutionPolicy Bypass -File .\compilar.ps1
El script requiere un JDK instalado y genera BibliotecaEscolar.jar con compatibilidad para Java 17.
Pruebas
El proyecto incluye pruebas de integración con MySQL y una comprobación de la interfaz Swing.
Se verificaron 33 condiciones, incluyendo:
- Autenticación y permisos.
- Operaciones CRUD.
- Persistencia y rollback.
- Dos solicitudes simultáneas para un único ejemplar.
- Control de stock y devolución duplicada.
- Cálculo de vencimiento y atraso.
- Reportes y privacidad del historial.
- Respuesta del hilo de interfaz mientras un trabajador espera la base de datos.
Consulta [PRUEBAS.md](PRUEBAS.md) para conocer el procedimiento y las condiciones de ejecución. Las pruebas deben realizarse sobre una base independiente de los datos de uso habitual.
