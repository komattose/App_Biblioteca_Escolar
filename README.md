# Evaluación Final — Desarrollo Orientado a Objetos II

## Proyecto Biblioteca Escolar

Sistema de escritorio desarrollado en **Java**, con interfaz gráfica **Swing JFrame** y persistencia en **MySQL**. Permite administrar el catálogo de una biblioteca escolar, gestionar estudiantes y controlar préstamos y devoluciones.

## Autor del proyecto

- **Nombre:** Martín Belaunde
- **Carrera:** Analista Programador Computacional
- **Modalidad:** Duoc UC — Online
- **Asignatura:** Desarrollo Orientado a Objetos II

## Objetivo general

Desarrollar una aplicación que facilite la gestión de una biblioteca escolar, integrando programación orientada a objetos, patrones de diseño, interfaz gráfica, concurrencia y conexión a una base de datos.

El sistema permite mantener un inventario actualizado, controlar la disponibilidad de ejemplares y conservar el historial de préstamos de cada estudiante.

## Funcionalidades del sistema

### Autenticación y roles

El acceso distingue entre dos tipos de usuarios:

| Rol | Funciones disponibles |
| --- | --- |
| Bibliotecario | Administración de libros, categorías y estudiantes; registro de préstamos y devoluciones; consulta de reportes. |
| Estudiante | Consulta del catálogo, solicitud y devolución de sus propios préstamos y revisión de su historial. |

Los permisos se comprueban en la interfaz y en la lógica de negocio.

### Gestión de libros

- Registro de título, autor, editorial, ISBN, categoría y stock.
- Consulta del catálogo.
- Modificación de los datos de un libro.
- Eliminación de registros sin préstamos asociados.
- Control del stock disponible.

### Gestión de categorías y estudiantes

- Creación, consulta, modificación y eliminación de categorías.
- Registro de estudiantes y creación de sus cuentas de acceso.
- Actualización de información personal y académica.
- Conservación de registros relacionados con el historial de préstamos.

### Préstamos y devoluciones

- Validación de disponibilidad antes de prestar un libro.
- Descuento automático del stock.
- Cálculo del vencimiento a **14 días**.
- Registro de devolución y reposición del ejemplar.
- Cálculo de días de atraso.
- Prevención de préstamos activos duplicados y devoluciones repetidas.

### Reportes

- Libros más prestados.
- Historial por estudiante.
- Libros actualmente en préstamo.

## Estructura general del proyecto

```text
BibliotecaEscolar/
├── src/
│   ├── main/java/biblioteca/
│   │   ├── config/          # Conexión JDBC y transacciones
│   │   ├── controller/      # Eventos y tareas en segundo plano
│   │   ├── dao/             # Acceso y persistencia de datos
│   │   ├── model/           # Entidades del sistema
│   │   ├── security/        # Protección de contraseñas
│   │   ├── service/         # Reglas de negocio y permisos
│   │   ├── view/            # Ventanas e interfaz gráfica
│   │   └── Main.java        # Punto de entrada
│   └── test/java/biblioteca/
│       ├── IntegrationTest.java
│       └── UiSmokeTest.java
├── sql/
│   ├── 01_crear_tablas.sql
│   ├── 02_poblar_tablas.sql
│   └── 03_usuario_local.sql
├── lib/                     # MySQL Connector/J
├── docs/                    # Documentación del proyecto
├── BibliotecaEscolar.jar    # Ejecutable Java
├── config.example.properties
├── compilar.ps1
├── probar.ps1
├── iniciar.bat
├── iniciar.sh
├── pom.xml
├── PRUEBAS.md
└── README.md
```

## Conceptos de orientación a objetos

| Concepto | Aplicación en el proyecto |
| --- | --- |
| Encapsulamiento | Atributos privados y acceso mediante métodos públicos; modelos inmutables mediante records. |
| Abstracción | `Usuario` define una base abstracta para los usuarios del sistema. |
| Herencia | `Bibliotecario` y `Estudiante` heredan de `Usuario`. |
| Polimorfismo | Cada tipo de usuario implementa su comportamiento de permisos mediante métodos sobrescritos. |
| Interfaces | `CrudDao<T>` establece un contrato común para las operaciones de persistencia. |
| Genéricos | Los DAO utilizan tipos genéricos para compartir contratos e infraestructura. |
| Colecciones | Uso de listas para manejar libros, categorías, estudiantes y resultados de consultas. |

## Patrones de diseño

### MVC

Se separan las responsabilidades del sistema:

- **Modelo:** representa las entidades y sus datos.
- **Vista:** contiene las ventanas y componentes Swing.
- **Controlador:** atiende eventos y coordina las acciones de la interfaz.

La capa `service` concentra las reglas de negocio y las validaciones de permisos.

### DAO

Cada entidad cuenta con una clase encargada de interactuar con la base de datos.

Las consultas utilizan `PreparedStatement`, separando los valores ingresados por el usuario de las instrucciones SQL.

### Singleton

La clase `DatabaseConnection` proporciona una única instancia mediante `getInstance()` y administra una conexión JDBC por aplicación.

El acceso a esa conexión se sincroniza para evitar que distintos trabajadores mezclen sus transacciones.

## Concurrencia y control de consistencia

Las operaciones de base de datos se ejecutan en segundo plano mediante **SwingWorker**, evitando bloquear el hilo de eventos de la interfaz.

El sistema utiliza:

- `doInBackground()` para realizar el trabajo en segundo plano.
- `done()` para procesar el resultado y actualizar la interfaz.
- Métodos `synchronized` para proteger operaciones críticas dentro de la aplicación.
- Transacciones JDBC con `commit` y `rollback`.
- Bloqueos de filas mediante `SELECT ... FOR UPDATE`.
- Restricciones y validaciones en MySQL.

**El préstamo y el descuento de stock forman una única transacción.** Si ocurre un error, los cambios se revierten.

La devolución repone el ejemplar y mantiene el registro del préstamo como parte del historial.

## Base de datos

La base de datos se llama **`biblioteca_escolar`** y contiene cinco tablas principales:

| Tabla | Información almacenada |
| --- | --- |
| `usuarios` | Datos personales, credenciales y rol. |
| `estudiantes` | Perfil académico y relación con la cuenta de usuario. |
| `categorias` | Clasificación de los libros. |
| `libros` | Información bibliográfica y stock disponible. |
| `prestamos` | Fechas, vencimientos, devoluciones y atrasos. |

Las claves foráneas mantienen las relaciones entre los registros e impiden eliminar información asociada al historial.

Los scripts incluidos son propios y se basan en la estructura descrita en la pauta académica. No se ha validado su compatibilidad directa con los scripts externos del AVA.

## Requisitos del entorno

| Componente | Requisito |
| --- | --- |
| Java | JDK 17 o superior |
| Base de datos | MySQL Server 8.0.16 o superior |
| Conector JDBC | MySQL Connector/J 9.2.0, incluido en `lib/` |
| IDE recomendado | IntelliJ IDEA |
| Codificación | UTF-8 |

El proyecto fue probado en Windows. MySQL debe permanecer en ejecución mientras se utiliza la aplicación.

## Instalación y configuración

### 1. Descargar el proyecto

Clona el repositorio o descarga su archivo ZIP y extrae todo su contenido.

Conserva la estructura de carpetas, especialmente `lib/`, `sql/` y `src/`.

### 2. Preparar la base de datos

Abre MySQL Workbench con una cuenta administradora y ejecuta estos scripts en orden:

```text
1. sql/01_crear_tablas.sql
2. sql/02_poblar_tablas.sql
3. sql/03_usuario_local.sql
```

- El primer script crea la base y sus tablas.
- El segundo incorpora los datos de demostración.
- El tercero crea la cuenta de conexión de la aplicación.

Ejecuta los scripts una sola vez sobre una base nueva. Si ya existe una base con ese nombre, revisa su contenido antes de continuar.

### 3. Configurar la conexión

Copia el archivo:

```text
config.example.properties
```

Renombra la copia como:

```text
config.properties
```

Debe quedar en la raíz del proyecto, junto a `pom.xml`.

Configuración local de demostración:

```properties
db.url=jdbc:mysql://localhost:3306/biblioteca_escolar?connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=15000
db.user=biblioteca_app
db.password=BibliotecaLocal2026!
```

Ajusta el puerto y las credenciales si tu instalación utiliza otros valores.

**La cuenta `biblioteca_app` conecta Java con MySQL. No es la cuenta utilizada para ingresar a la ventana de la biblioteca.**

## Ejecución del programa

### Desde IntelliJ IDEA

1. Abre la carpeta del proyecto o el archivo `pom.xml`.
2. Selecciona un JDK 17 o superior.
3. Espera a que Maven cargue las dependencias.
4. Utiliza la raíz del proyecto como directorio de trabajo.
5. Abre `src/main/java/biblioteca/Main.java`.
6. Ejecuta el método `main`.

### Desde el ejecutable

En Windows, abre:

```text
iniciar.bat
```

También puedes ejecutar desde la raíz del proyecto:

```bash
java -jar BibliotecaEscolar.jar
```

La carpeta `lib/` debe permanecer junto al archivo JAR.

## Cuentas de demostración

| Rol | Usuario | Contraseña |
| --- | --- | --- |
| Bibliotecario | `admin` | `Admin2026!` |
| Estudiante | `ana` | `Estudiante2026!` |
| Estudiante | `diego` | `Estudiante2026!` |

Estas cuentas se crean al ejecutar el script de poblamiento y están destinadas a la demostración local.

## Flujo de uso

1. Iniciar sesión con una cuenta registrada.
2. Consultar el catálogo y comprobar el stock.
3. Seleccionar un estudiante y un libro disponible.
4. Registrar el préstamo y verificar su vencimiento.
5. Registrar la devolución cuando corresponda.
6. Consultar el historial y los reportes.

El bibliotecario también puede administrar libros, categorías y estudiantes desde las pestañas correspondientes.

## Validaciones y protección de datos

- Contraseñas almacenadas mediante **PBKDF2-HMAC-SHA256 con sal**.
- Consultas SQL parametrizadas.
- Verificación de permisos según el rol.
- Acceso del estudiante limitado a su propio historial.
- Validación de campos obligatorios.
- Rechazo de stock negativo.
- Control de duplicados mediante restricciones de base de datos.
- Prevención de devoluciones repetidas.
- Conservación de registros con historial asociado.
- Rechazo de ediciones que intenten sobrescribir un stock desactualizado.

`config.properties` está excluido mediante `.gitignore`. No deben publicarse credenciales personales en el repositorio.

## Compilación

Para reconstruir el ejecutable en Windows, utiliza:

```powershell
powershell -ExecutionPolicy Bypass -File .\compilar.ps1
```

El script compila el código y genera `BibliotecaEscolar.jar` con compatibilidad para Java 17.

## Pruebas realizadas

El proyecto incluye **33 verificaciones de integración con MySQL**, entre ellas:

- Autenticación de ambos roles.
- Restricciones de acceso.
- Operaciones CRUD.
- Persistencia y rollback.
- Dos solicitudes simultáneas para un único ejemplar.
- Control del stock.
- Prevención de devolución duplicada.
- Cálculo de vencimientos y atrasos.
- Generación de reportes.
- Protección del historial de cada estudiante.
- Respuesta del hilo de interfaz durante una espera de base de datos.

También se verificó el inicio de sesión y la carga de pantallas mediante `UiSmokeTest`.

Las instrucciones para repetir las pruebas se encuentran en [PRUEBAS.md](PRUEBAS.md). Deben ejecutarse sobre una base de pruebas independiente.

## Documentación complementaria

- [Informe y guía del video](docs/Informe_y_guia_video.md)
- [Procedimiento y resultados de pruebas](PRUEBAS.md)

## Contexto académico

**Institución:** Duoc UC  
**Carrera:** Analista Programador Computacional  
**Asignatura:** Desarrollo Orientado a Objetos II  
**Actividad:** Evaluación Final Transversal  
**Caso:** Sistema de Gestión de Biblioteca Escolar
