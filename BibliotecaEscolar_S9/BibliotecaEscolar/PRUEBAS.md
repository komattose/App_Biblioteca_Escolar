# Verificación del proyecto

Fecha: 10 de octubre de 2026. Compilación dirigida a Java 17. Pruebas con Java 26.0.1 y repetición final desde el JAR con Java 17, MySQL Server 8.0.46 y MySQL Connector/J 9.2.0 sobre una instancia local temporal independiente. Los datos de demostración del proyecto se cargaron en una base nueva; no se usaron los scripts inaccesibles del AVA.

## Resultado de integración

**33 verificaciones correctas.**

1. Autenticación de ambos roles y permisos polimórficos.
2. Identidad de la instancia Singleton.
3. Rechazo de contraseña incorrecta.
4. Entrada de tipo inyección SQL sin acceso.
5. Generación y verificación PBKDF2.
6. Estudiante sin acceso a administración.
7. Estudiante sin préstamos para otro usuario.
8. Imposibilidad de borrar al bibliotecario como estudiante.
9. Crear, leer y actualizar categoría.
10. Crear, leer y actualizar libro.
11. Rechazo de stock negativo.
12. Alta y modificación del estudiante; contraseña conservada al dejarla vacía.
13. Rechazo de RUT duplicado.
14. Rollback del alta evita cuentas huérfanas.
15. Dos solicitudes simultáneas para un ejemplar: una sola aceptada.
16. Stock final cero en esa concurrencia.
17. Vencimiento automático a 14 días.
18. Edición con stock obsoleto rechazada.
19. Devolución ajena rechazada.
20. Conservación de historial mediante claves foráneas.
21. Devolución repone el ejemplar.
22. Segunda devolución rechazada.
23. Segunda devolución no aumenta el stock.
24. Devolución atrasada registra seis días.
25. Reporte de libros más prestados.
26. Historial por estudiante.
27. Reporte de préstamos activos.
28. Historial del estudiante no expone el de terceros.
29. Ranking administrativo restringido.
30. EDT responde mientras SwingWorker espera la base de datos.
31. Eliminación de estudiante y cuenta sin historial.
32. Eliminación de libro sin historial.
33. Eliminación de categoría sin referencias.

La prueba de concurrencia lanza dos hilos y dos objetos de servicio contra el Singleton compartido. No es una prueba de carga distribuida. La respuesta del EDT se comprueba con una espera controlada de la BD; no se induce lentitud artificial en la aplicación entregada.

## Interfaz

`UiSmokeTest` inicia el controlador real, ingresa como bibliotecario, carga las cinco pestañas, genera un reporte, cierra sesión e ingresa como estudiante. Se revisaron visualmente las pantallas renderizadas de Swing. Esta verificación complementa la integración; conviene ensayar manualmente el flujo exacto antes de grabar.

## Repetir las pruebas

Utiliza una base independiente de tus datos habituales. Crea una copia de los tres scripts SQL cambiando `biblioteca_escolar` por `biblioteca_test`, ejecútalos en orden y configura:

```powershell
$env:BIBLIOTECA_DB_URL = 'jdbc:mysql://localhost:3306/biblioteca_test?connectionTimeZone=LOCAL'
$env:BIBLIOTECA_DB_USER = 'biblioteca_app'
$env:BIBLIOTECA_DB_PASSWORD = 'BibliotecaLocal2026!'
$env:BIBLIOTECA_TEST_CONFIRM = 'SI'
powershell -ExecutionPolicy Bypass -File .\probar.ps1
```

La base debe contener las cuentas `admin`, `ana` y `diego` del poblamiento. La prueba crea registros propios y los elimina al finalizar correctamente. Si falla a mitad, usa una base nueva para repetir; no limpies datos productivos para satisfacer una prueba.

Después de compilar las pruebas, la comprobación de interfaz se puede ejecutar así, con las mismas variables de conexión:

```powershell
java -cp 'BibliotecaEscolar.jar;build/test-classes;lib/*' biblioteca.UiSmokeTest build/ui-qa
```

El ejecutable requiere un escritorio gráfico y una conexión MySQL configurada. No se probó en Linux ni macOS. El video, la carga en AVA y la publicación pública en GitHub no forman parte de las pruebas ejecutadas.
