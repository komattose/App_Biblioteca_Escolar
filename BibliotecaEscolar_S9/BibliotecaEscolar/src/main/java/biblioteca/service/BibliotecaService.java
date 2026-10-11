package biblioteca.service;

import biblioteca.config.*;
import biblioteca.dao.*;
import biblioteca.model.*;
import biblioteca.security.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Reglas de negocio y autorización. Ninguna decisión de permisos depende solo de la vista. */
public final class BibliotecaService {
  private final DatabaseConnection db = DatabaseConnection.getInstance();

  private void admin(Usuario u) {
    if (u == null || !u.puedeAdministrar())
      throw new IllegalArgumentException("Acción exclusiva del bibliotecario.");
  }

  private void own(Usuario u, int id) {
    if (u == null || (!u.puedeAdministrar() && u.getId() != id))
      throw new IllegalArgumentException("Solo puedes operar tus propios préstamos.");
  }

  private String required(String s, String label, int max) {
    if (s == null || s.isBlank() || s.trim().length() > max)
      throw new IllegalArgumentException(label + ": obligatorio, máximo " + max + " caracteres.");
    return s.trim();
  }

  public Usuario login(String login, char[] password) throws Exception {
    try {
      return db.read(
          c -> {
            Usuario u = new UsuarioDao(c).porLogin(login.trim());
            if (u == null || !Passwords.verify(password, u.getPasswordHash()))
              throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
            if (!u.puedeAdministrar()) u = new EstudianteDao(c).buscar(u.getId());
            if (u == null) throw new IllegalArgumentException("Perfil de estudiante incompleto.");
            return u;
          });
    } finally {
      Arrays.fill(password, '\0');
    }
  }

  public List<Libro> libros() throws Exception {
    return db.read(c -> new LibroDao(c).listar());
  }

  public List<Categoria> categorias() throws Exception {
    return db.read(c -> new CategoriaDao(c).listar());
  }

  public List<Estudiante> estudiantes(Usuario u) throws Exception {
    return db.read(
        c ->
            u.puedeAdministrar()
                ? new EstudianteDao(c).listar()
                : List.of(new EstudianteDao(c).buscar(u.getId())));
  }

  public int guardarCategoria(Usuario u, int id, String nombre) throws Exception {
    admin(u);
    String n = required(nombre, "Nombre", 80);
    return db.transaction(
        c -> {
          var d = new CategoriaDao(c);
          if (id == 0) return d.crear(new Categoria(0, n));
          if (d.buscar(id) == null) throw new IllegalArgumentException("Categoría inexistente.");
          d.actualizar(new Categoria(id, n));
          return id;
        });
  }

  /** El monitor local y la transacción impiden cambios de stock intercalados. */
  public synchronized int guardarLibro(Usuario u, Libro x) throws Exception {
    return guardarLibro(u, x, null);
  }

  public synchronized int guardarLibro(Usuario u, Libro x, Integer stockEsperado) throws Exception {
    admin(u);
    required(x.titulo(), "Título", 150);
    required(x.autor(), "Autor", 120);
    required(x.editorial(), "Editorial", 120);
    required(x.isbn(), "ISBN", 30);
    if (!x.isbn().matches("[0-9Xx-]{10,17}"))
      throw new IllegalArgumentException(
          "ISBN: utiliza de 10 a 17 caracteres, dígitos, X o guiones.");
    if (x.stock() < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
    return db.transaction(
        c -> {
          var d = new LibroDao(c);
          if (new CategoriaDao(c).buscar(x.categoriaId()) == null)
            throw new IllegalArgumentException("Selecciona una categoría válida.");
          if (x.id() == 0) return d.crear(x);
          Libro actual = d.bloquear(x.id());
          if (actual == null) throw new IllegalArgumentException("Libro inexistente.");
          if (stockEsperado != null && actual.stock() != stockEsperado)
            throw new IllegalArgumentException(
                "El stock cambió mientras editabas. Actualiza los datos y vuelve a intentarlo.");
          d.actualizar(x);
          return x.id();
        });
  }

  public int guardarEstudiante(
      Usuario u,
      int id,
      String nombre,
      String login,
      char[] password,
      String rut,
      String curso,
      String email)
      throws Exception {
    try {
      admin(u);
      required(nombre, "Nombre", 120);
      required(login, "Usuario", 60);
      required(rut, "RUT", 20);
      required(curso, "Curso", 40);
      required(email, "Correo", 120);
      if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
        throw new IllegalArgumentException("Correo inválido.");
      if ((id == 0 || password.length > 0) && password.length < 8)
        throw new IllegalArgumentException("Contraseña: mínimo 8 caracteres.");
      String hash = password.length == 0 ? null : Passwords.hash(password);
      return db.transaction(
          c -> {
            var ud = new UsuarioDao(c);
            var ed = new EstudianteDao(c);
            Usuario old = id == 0 ? null : ud.buscar(id);
            if (id != 0 && (old == null || old.puedeAdministrar()))
              throw new IllegalArgumentException("Estudiante inexistente.");
            String h = hash == null ? old.getPasswordHash() : hash;
            Estudiante e =
                new Estudiante(
                    id, nombre.trim(), login.trim(), h, rut.trim(), curso.trim(), email.trim());
            if (id == 0) {
              int newId = ud.crear(e);
              ed.crear(
                  new Estudiante(
                      newId,
                      e.getNombre(),
                      e.getLogin(),
                      h,
                      e.getRut(),
                      e.getCurso(),
                      e.getEmail()));
              return newId;
            }
            ud.actualizar(e);
            ed.actualizar(e);
            return id;
          });
    } finally {
      Arrays.fill(password, '\0');
    }
  }

  public synchronized void eliminar(Usuario u, String entidad, int id) throws Exception {
    admin(u);
    db.transaction(
        c -> {
          switch (entidad) {
            case "Libros" -> new LibroDao(c).eliminar(id);
            case "Categorías" -> new CategoriaDao(c).eliminar(id);
            case "Estudiantes" -> {
              if (new EstudianteDao(c).buscar(id) == null)
                throw new IllegalArgumentException("Estudiante inexistente.");
              new EstudianteDao(c).eliminar(id);
              new UsuarioDao(c).eliminar(id);
            }
            default -> throw new IllegalArgumentException("Entidad no permitida.");
          }
          return null;
        });
  }

  /**
   * Préstamo, stock e historial (fila de prestamos) se confirman o revierten juntos. synchronized
   * protege esta JVM; FOR UPDATE y UNIQUE protegen también otras instancias.
   */
  public synchronized int prestar(Usuario u, int alumno, int libro) throws Exception {
    own(u, alumno);
    return db.transaction(
        c -> {
          var ld = new LibroDao(c);
          var pd = new PrestamoDao(c);
          Libro l = ld.bloquear(libro);
          if (l == null || l.stock() == 0)
            throw new IllegalArgumentException("No hay stock disponible.");
          if (new EstudianteDao(c).buscar(alumno) == null)
            throw new IllegalArgumentException("Estudiante inexistente.");
          if (pd.activo(alumno, libro))
            throw new IllegalArgumentException(
                "El estudiante ya tiene un préstamo activo de este libro.");
          if (!ld.descontar(libro)) throw new IllegalArgumentException("No hay stock disponible.");
          LocalDate hoy = LocalDate.now();
          return pd.crear(new Prestamo(0, alumno, libro, hoy, hoy.plusDays(14), null, 0));
        });
  }

  public synchronized void devolver(Usuario u, int prestamo) throws Exception {
    db.transaction(
        c -> {
          var pd = new PrestamoDao(c);
          Prestamo p = pd.bloquear(prestamo);
          if (p == null) throw new IllegalArgumentException("Préstamo inexistente.");
          own(u, p.estudianteId());
          if (p.devolucion() != null)
            throw new IllegalArgumentException("El préstamo ya fue devuelto.");
          LocalDate hoy = LocalDate.now();
          int atraso = (int) Math.max(0, ChronoUnit.DAYS.between(p.vencimiento(), hoy));
          pd.actualizar(
              new Prestamo(
                  p.id(), p.estudianteId(), p.libroId(), p.fecha(), p.vencimiento(), hoy, atraso));
          new LibroDao(c).reponer(p.libroId());
          return null;
        });
  }

  public ReporteDao.Tabla reporte(Usuario u, int tipo, Integer alumno) throws Exception {
    if (tipo == 0) {
      admin(u);
      return db.read(c -> new ReporteDao(c).populares());
    }
    if (!u.puedeAdministrar()) alumno = u.getId();
    final Integer filter = alumno;
    return db.read(c -> new ReporteDao(c).historial(filter, tipo == 2));
  }
}
