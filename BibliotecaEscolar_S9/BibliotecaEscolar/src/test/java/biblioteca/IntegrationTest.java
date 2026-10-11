package biblioteca;

import biblioteca.config.DatabaseConnection;
import biblioteca.dao.*;
import biblioteca.model.*;
import biblioteca.security.Passwords;
import biblioteca.service.BibliotecaService;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

/** Ejecutar únicamente contra una base de pruebas poblada, nunca contra datos reales. */
public final class IntegrationTest {
  private static int checks;
  private static final BibliotecaService service = new BibliotecaService();
  private static final DatabaseConnection db = DatabaseConnection.getInstance();

  private static void check(boolean condition, String name) {
    if (!condition) throw new AssertionError(name);
    checks++;
    System.out.println("OK " + name);
  }

  @FunctionalInterface
  interface Action {
    void run() throws Exception;
  }

  private static void rejects(Action action, String name) throws Exception {
    boolean rejected = false;
    try {
      action.run();
    } catch (IllegalArgumentException | SQLException expected) {
      rejected = true;
    }
    check(rejected, name);
  }

  private static Libro book(int id) throws Exception {
    return db.read(c -> new LibroDao(c).buscar(id));
  }

  public static void main(String[] args) throws Exception {
    if (!"SI".equals(System.getenv("BIBLIOTECA_TEST_CONFIRM")))
      throw new IllegalStateException(
          "Configura BIBLIOTECA_TEST_CONFIRM=SI solo para una base de pruebas.");
    Usuario admin = service.login("admin", "Admin2026!".toCharArray());
    Usuario ana = service.login("ana", "Estudiante2026!".toCharArray());
    Usuario diego = service.login("diego", "Estudiante2026!".toCharArray());
    check(admin.puedeAdministrar() && !ana.puedeAdministrar(), "Login y permisos polimórficos");
    check(db == DatabaseConnection.getInstance(), "Singleton");
    rejects(() -> service.login("admin", "incorrecta".toCharArray()), "Contraseña incorrecta");
    rejects(
        () -> service.login("' OR 1=1 --", "incorrecta".toCharArray()),
        "Entrada SQL no altera autenticación");
    check(
        Passwords.verify("Segura2026!".toCharArray(), Passwords.hash("Segura2026!".toCharArray())),
        "PBKDF2");
    rejects(() -> service.guardarCategoria(ana, 0, "No permitida"), "Estudiante no administra");
    rejects(() -> service.prestar(ana, diego.getId(), 2), "Estudiante no pide para otro");
    rejects(
        () -> service.eliminar(admin, "Estudiantes", admin.getId()),
        "No elimina bibliotecario como estudiante");
    int cat = service.guardarCategoria(admin, 0, "Prueba temporal");
    service.guardarCategoria(admin, cat, "Prueba editada");
    check(
        service.categorias().stream()
            .anyMatch(c -> c.id() == cat && c.nombre().equals("Prueba editada")),
        "Crear leer actualizar categoría");
    int b =
        service.guardarLibro(
            admin, new Libro(0, "Libro prueba", "Autor", "Editorial", "9780000000002", cat, 1));
    service.guardarLibro(
        admin, new Libro(b, "Libro editado", "Autor", "Editorial", "9780000000002", cat, 1));
    check(book(b).titulo().equals("Libro editado"), "Crear leer actualizar libro");
    rejects(
        () ->
            service.guardarLibro(
                admin, new Libro(0, "Inválido", "Autor", "Editorial", "9780000000003", cat, -1)),
        "Stock negativo rechazado");
    int student =
        service.guardarEstudiante(
            admin,
            0,
            "Prueba",
            "prueba",
            "Prueba2026!".toCharArray(),
            "99.999.999-9",
            "1 A",
            "prueba@example.com");
    service.guardarEstudiante(
        admin,
        student,
        "Prueba editada",
        "prueba",
        new char[0],
        "99.999.999-9",
        "2 B",
        "prueba@example.com");
    check(
        service.login("prueba", "Prueba2026!".toCharArray()).getNombre().equals("Prueba editada"),
        "CRUD estudiante y contraseña conservada");
    int usersBefore = db.read(c -> new UsuarioDao(c).listar().size());
    rejects(
        () ->
            service.guardarEstudiante(
                admin,
                0,
                "Duplicado",
                "duplicado",
                "Prueba2026!".toCharArray(),
                "99.999.999-9",
                "1 A",
                "d@example.com"),
        "RUT duplicado rechazado");
    check(
        usersBefore == db.read(c -> new UsuarioDao(c).listar().size()),
        "Rollback evita usuario huérfano");

    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch gate = new CountDownLatch(1);
    Callable<Integer> first =
        () -> {
          gate.await();
          try {
            return service.prestar(admin, ana.getId(), b);
          } catch (IllegalArgumentException e) {
            return -1;
          }
        };
    Callable<Integer> second =
        () -> {
          gate.await();
          try {
            return new BibliotecaService().prestar(admin, diego.getId(), b);
          } catch (IllegalArgumentException e) {
            return -1;
          }
        };
    Future<Integer> f1 = pool.submit(first), f2 = pool.submit(second);
    gate.countDown();
    int p1 = f1.get(), p2 = f2.get();
    check((p1 > 0) ^ (p2 > 0), "Dos solicitudes simultáneas con un ejemplar: solo una aceptada");
    check(book(b).stock() == 0, "Stock final cero sin sobrepréstamo");
    int loan = Math.max(p1, p2);
    Prestamo p = db.read(c -> new PrestamoDao(c).buscar(loan));
    check(p.vencimiento().equals(p.fecha().plusDays(14)), "Vencimiento automático 14 días");
    rejects(
        () ->
            service.guardarLibro(
                admin,
                new Libro(b, "Libro editado", "Autor", "Editorial", "9780000000002", cat, 1),
                1),
        "Edición obsoleta no pisa stock");
    Usuario wrong = p.estudianteId() == ana.getId() ? diego : ana;
    rejects(() -> service.devolver(wrong, loan), "No devuelve un préstamo ajeno");
    rejects(() -> service.eliminar(admin, "Libros", b), "Clave foránea conserva historial");
    service.devolver(admin, loan);
    check(book(b).stock() == 1, "Devolución repone stock");
    rejects(() -> service.devolver(admin, loan), "Doble devolución rechazada");
    check(book(b).stock() == 1, "Doble devolución no duplica stock");
    int overdue = service.prestar(admin, student, b);
    db.transaction(
        c -> {
          PrestamoDao d = new PrestamoDao(c);
          Prestamo x = d.buscar(overdue);
          d.actualizar(
              new Prestamo(
                  x.id(),
                  x.estudianteId(),
                  x.libroId(),
                  LocalDate.now().minusDays(20),
                  LocalDate.now().minusDays(6),
                  null,
                  0));
          return null;
        });
    service.devolver(admin, overdue);
    check(
        db.read(c -> new PrestamoDao(c).buscar(overdue)).diasAtraso() == 6,
        "Registra seis días de atraso");
    check(service.reporte(admin, 0, null).filas().size() > 0, "Reporte de populares");
    check(service.reporte(admin, 1, student).filas().size() == 1, "Historial por estudiante");
    check(
        service.reporte(admin, 2, null).filas().stream().allMatch(r -> r[5] == null),
        "Reporte de activos");
    check(
        service.reporte(ana, 1, student).filas().stream()
            .allMatch(r -> r[1].equals(ana.getNombre())),
        "Reporte estudiantil no expone historial ajeno");
    rejects(() -> service.reporte(ana, 0, null), "Estudiante no consulta ranking administrativo");

    // Se simula una operación de BD lenta y se comprueba que el EDT sigue atendiendo eventos.
    CountDownLatch databaseHeld = new CountDownLatch(1),
        release = new CountDownLatch(1),
        edt = new CountDownLatch(1);
    Future<?> holder =
        pool.submit(
            () -> {
              try {
                db.read(
                    c -> {
                      databaseHeld.countDown();
                      release.await();
                      return null;
                    });
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
    databaseHeld.await();
    SwingWorker<java.util.List<Libro>, Void> worker =
        new SwingWorker<>() {
          protected java.util.List<Libro> doInBackground() throws Exception {
            return service.libros();
          }
        };
    worker.execute();
    SwingUtilities.invokeLater(edt::countDown);
    check(
        edt.await(2, TimeUnit.SECONDS) && !worker.isDone(),
        "EDT responde mientras el trabajador espera la BD");
    release.countDown();
    holder.get();
    worker.get();
    pool.shutdown();
    // Limpieza limitada a los IDs creados por esta prueba; el historial productivo no se borra.
    db.transaction(
        c -> {
          PrestamoDao d = new PrestamoDao(c);
          d.eliminar(loan);
          d.eliminar(overdue);
          return null;
        });
    service.eliminar(admin, "Estudiantes", student);
    check(
        service.estudiantes(admin).stream().noneMatch(s -> s.getId() == student),
        "Eliminar estudiante y cuenta");
    service.eliminar(admin, "Libros", b);
    check(book(b) == null, "Eliminar libro sin historial");
    service.eliminar(admin, "Categorías", cat);
    check(
        service.categorias().stream().noneMatch(c -> c.id() == cat),
        "Eliminar categoría sin referencias");
    db.close();
    System.out.println("RESULTADO: " + checks + " verificaciones correctas");
    System.exit(0);
  }
}
