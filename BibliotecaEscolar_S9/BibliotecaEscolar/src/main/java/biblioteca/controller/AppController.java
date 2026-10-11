package biblioteca.controller;

import biblioteca.dao.ReporteDao;
import biblioteca.model.*;
import biblioteca.service.*;
import biblioteca.view.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;

/** Eventos y trabajadores. La vista solo se modifica en el EDT. */
public final class AppController {
  private final BibliotecaService service = new BibliotecaService();
  private Usuario user;
  private MainView view;
  private List<Libro> books = List.of();
  private List<Categoria> categories = List.of();
  private List<Estudiante> students = List.of();

  private record Snapshot(
      List<Libro> books,
      List<Categoria> categories,
      List<Estudiante> students,
      ReporteDao.Tabla loans) {}

  public void start() {
    LoginView login = new LoginView();
    login.entrar.addActionListener(
        e -> {
          String name = login.usuario.getText();
          char[] pass = login.password.getPassword();
          login.entrar.setEnabled(false);
          login.estado.setText("Conectando...");
          new SwingWorker<Usuario, Void>() {
            protected Usuario doInBackground() throws Exception {
              return service.login(name, pass);
            }

            protected void done() {
              try {
                user = get();
                login.dispose();
                open();
              } catch (Exception ex) {
                error(login, ex);
                login.estado.setText("Revisa tus datos o la configuración de MySQL.");
                login.entrar.setEnabled(true);
                login.password.setText("");
              }
            }
          }.execute();
        });
    login.setVisible(true);
  }

  private void open() {
    view = new MainView(user);
    view.actualizar.addActionListener(e -> refresh());
    view.salir.addActionListener(
        e -> {
          view.dispose();
          user = null;
          start();
        });
    bind(view.libros, "Libros");
    bind(view.categorias, "Categorías");
    bind(view.estudiantes, "Estudiantes");
    view.prestar.addActionListener(
        e -> {
          Libro b = (Libro) view.libro.getSelectedItem();
          Estudiante s = (Estudiante) view.alumno.getSelectedItem();
          if (b == null || s == null) {
            error(view, new Exception("Registra un libro y un estudiante antes de prestar."));
            return;
          }
          mutate("Registrando préstamo...", () -> service.prestar(user, s.getId(), b.id()));
        });
    view.devolver.addActionListener(
        e -> {
          try {
            int id = MainView.selectedId(view.prestamos);
            if (JOptionPane.showConfirmDialog(
                    view,
                    "¿Confirmar devolución del préstamo " + id + "?",
                    "Devolución",
                    JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION)
              mutate(
                  "Registrando devolución...",
                  () -> {
                    service.devolver(user, id);
                    return null;
                  });
          } catch (Exception ex) {
            error(view, ex);
          }
        });
    view.verReporte.addActionListener(e -> report());
    view.setVisible(true);
    refresh();
  }

  private Snapshot snapshot() throws Exception {
    return new Snapshot(
        service.libros(),
        service.categorias(),
        service.estudiantes(user),
        service.reporte(user, 2, null));
  }

  private void apply(Snapshot s) {
    books = s.books();
    categories = s.categories();
    students = s.students();
    MainView.fill(
        view.libros.table,
        new String[] {
          "ID", "Título", "Autor", "Editorial", "ISBN", "Categoría", "Stock disponible"
        },
        books.stream()
            .map(
                b ->
                    new Object[] {
                      b.id(),
                      b.titulo(),
                      b.autor(),
                      b.editorial(),
                      b.isbn(),
                      categories.stream()
                          .filter(c -> c.id() == b.categoriaId())
                          .map(Categoria::nombre)
                          .findFirst()
                          .orElse(""),
                      b.stock()
                    })
            .toList());
    MainView.fill(
        view.categorias.table,
        new String[] {"ID", "Nombre"},
        categories.stream().map(c -> new Object[] {c.id(), c.nombre()}).toList());
    MainView.fill(
        view.estudiantes.table,
        new String[] {"ID", "Nombre", "Usuario", "RUT", "Curso", "Correo"},
        students.stream()
            .map(
                a ->
                    new Object[] {
                      a.getId(), a.getNombre(), a.getLogin(), a.getRut(), a.getCurso(), a.getEmail()
                    })
            .toList());
    view.libro.removeAllItems();
    books.forEach(view.libro::addItem);
    view.alumno.removeAllItems();
    students.forEach(view.alumno::addItem);
    view.filtroAlumno.removeAllItems();
    if (user.puedeAdministrar()) view.filtroAlumno.addItem("Todos los estudiantes");
    students.forEach(view.filtroAlumno::addItem);
    MainView.fill(view.prestamos, s.loans().columnas(), s.loans().filas());
    MainView.fill(
        view.reporte, new String[] {"Selecciona un reporte y pulsa Ver reporte"}, List.of());
  }

  private <T> void run(String message, Callable<T> task, Consumer<T> done) {
    view.busy(true, message);
    new SwingWorker<T, Void>() {
      protected T doInBackground() throws Exception {
        return task.call();
      }

      protected void done() {
        try {
          T result = get();
          view.busy(false, "Operación completada");
          done.accept(result);
        } catch (Exception ex) {
          view.busy(false, "No se completó la operación");
          error(view, ex);
        }
      }
    }.execute();
  }

  private void refresh() {
    run("Cargando datos...", this::snapshot, this::apply);
  }

  private void mutate(String message, Callable<?> task) {
    run(
        message,
        () -> {
          task.call();
          return snapshot();
        },
        this::apply);
  }

  private void report() {
    int type = view.tipoReporte.getSelectedIndex() + (user.puedeAdministrar() ? 0 : 1);
    Object selected = view.filtroAlumno.getSelectedItem();
    Integer id = selected instanceof Estudiante s ? s.getId() : null;
    run(
        "Generando reporte...",
        () -> service.reporte(user, type, id),
        t -> MainView.fill(view.reporte, t.columnas(), t.filas()));
  }

  private void bind(MainView.Grid grid, String entity) {
    grid.nuevo.addActionListener(e -> edit(entity, 0));
    grid.editar.addActionListener(
        e -> {
          try {
            edit(entity, MainView.selectedId(grid.table));
          } catch (Exception ex) {
            error(view, ex);
          }
        });
    grid.eliminar.addActionListener(
        e -> {
          try {
            int id = MainView.selectedId(grid.table);
            if (JOptionPane.showConfirmDialog(
                    view,
                    "¿Eliminar el registro "
                        + id
                        + "? Los registros con historial o referencias no se eliminan.",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION)
              mutate(
                  "Eliminando...",
                  () -> {
                    service.eliminar(user, entity, id);
                    return null;
                  });
          } catch (Exception ex) {
            error(view, ex);
          }
        });
  }

  private JTextField field(String text) {
    return new JTextField(text, 25);
  }

  private void row(JPanel p, String label, JComponent c) {
    p.add(new JLabel(label));
    p.add(c);
  }

  private boolean confirm(JPanel p, String title) {
    return JOptionPane.showConfirmDialog(
            view, p, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        == JOptionPane.OK_OPTION;
  }

  private void edit(String entity, int id) {
    try {
      JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
      if (entity.equals("Categorías")) {
        Categoria c =
            categories.stream().filter(x -> x.id() == id).findFirst().orElse(new Categoria(0, ""));
        JTextField n = field(c.nombre());
        row(p, "Nombre", n);
        if (confirm(p, "Categoría")) {
          String name = n.getText();
          mutate("Guardando categoría...", () -> service.guardarCategoria(user, id, name));
        }
      } else if (entity.equals("Libros")) {
        Libro b =
            books.stream()
                .filter(x -> x.id() == id)
                .findFirst()
                .orElse(new Libro(0, "", "", "", "", 0, 0));
        JTextField title = field(b.titulo()),
            author = field(b.autor()),
            editor = field(b.editorial()),
            isbn = field(b.isbn());
        JSpinner stock = new JSpinner(new SpinnerNumberModel(b.stock(), 0, 100000, 1));
        JComboBox<Categoria> cat = new JComboBox<>(categories.toArray(Categoria[]::new));
        for (Categoria c : categories) if (c.id() == b.categoriaId()) cat.setSelectedItem(c);
        row(p, "Título", title);
        row(p, "Autor", author);
        row(p, "Editorial", editor);
        row(p, "ISBN", isbn);
        row(p, "Categoría", cat);
        row(p, "Stock disponible", stock);
        if (confirm(p, "Libro")) {
          stock.commitEdit();
          Categoria c = (Categoria) cat.getSelectedItem();
          if (c == null) throw new IllegalArgumentException("Primero registra una categoría.");
          Libro updated =
              new Libro(
                  id,
                  title.getText().trim(),
                  author.getText().trim(),
                  editor.getText().trim(),
                  isbn.getText().trim(),
                  c.id(),
                  (int) stock.getValue());
          mutate("Guardando libro...", () -> service.guardarLibro(user, updated, b.stock()));
        }
      } else {
        Estudiante s =
            students.stream()
                .filter(x -> x.getId() == id)
                .findFirst()
                .orElse(new Estudiante(0, "", "", "", "", "", ""));
        JTextField n = field(s.getNombre()),
            l = field(s.getLogin()),
            rut = field(s.getRut()),
            curso = field(s.getCurso()),
            email = field(s.getEmail());
        JPasswordField pass = new JPasswordField();
        row(p, "Nombre", n);
        row(p, "Usuario", l);
        row(p, id == 0 ? "Contraseña (mín. 8)" : "Contraseña (vacío conserva)", pass);
        row(p, "RUT", rut);
        row(p, "Curso", curso);
        row(p, "Correo", email);
        if (confirm(p, "Estudiante")) {
          String name = n.getText(),
              login = l.getText(),
              r = rut.getText(),
              cu = curso.getText(),
              em = email.getText();
          char[] pw = pass.getPassword();
          mutate(
              "Guardando estudiante...",
              () -> service.guardarEstudiante(user, id, name, login, pw, r, cu, em));
        }
      }
    } catch (Exception ex) {
      error(view, ex);
    }
  }

  private void error(Component owner, Throwable e) {
    while (e.getCause() != null) e = e.getCause();
    String message = e.getMessage();
    if (e instanceof java.sql.SQLException sql) {
      String state = sql.getSQLState();
      if (state != null && state.startsWith("23"))
        message =
            "Registro duplicado o relacionado con otros datos. Revisa usuario, RUT, ISBN y"
                + " referencias; se conserva el historial.";
      else
        message =
            "No se pudo completar la operación MySQL. Revisa el servidor, los scripts y"
                + " config.properties. Código: "
                + sql.getErrorCode()
                + " / "
                + state;
    }
    JOptionPane.showMessageDialog(
        owner,
        message == null ? e.getClass().getSimpleName() : message,
        "Biblioteca escolar",
        JOptionPane.ERROR_MESSAGE);
  }
}
