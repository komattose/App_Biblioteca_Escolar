package biblioteca.view;

import biblioteca.model.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainView extends JFrame {
  public static final class Grid {
    public final JPanel panel = new JPanel(new BorderLayout(8, 8));
    public final JTable table = new JTable();
    public final JButton nuevo = new JButton("Nuevo"),
        editar = new JButton("Editar"),
        eliminar = new JButton("Eliminar");

    Grid(boolean admin) {
      JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
      if (admin) {
        bar.add(nuevo);
        bar.add(editar);
        bar.add(eliminar);
      }
      panel.add(bar, BorderLayout.NORTH);
      panel.add(new JScrollPane(table), BorderLayout.CENTER);
      setup(table);
    }
  }

  public final Grid libros, categorias, estudiantes;
  public final JTable prestamos = new JTable(), reporte = new JTable();
  public final JComboBox<Libro> libro = new JComboBox<>();
  public final JComboBox<Estudiante> alumno = new JComboBox<>();
  public final JComboBox<Object> filtroAlumno = new JComboBox<>();
  public final JComboBox<String> tipoReporte;
  public final JButton prestar = new JButton("Registrar préstamo"),
      devolver = new JButton("Devolver seleccionado"),
      actualizar = new JButton("Actualizar datos"),
      verReporte = new JButton("Ver reporte"),
      salir = new JButton("Cerrar sesión");
  public final JLabel estado = new JLabel("Listo"), reloj = new JLabel();
  public final JProgressBar progress = new JProgressBar();
  public final JTabbedPane tabs = new JTabbedPane();
  private final javax.swing.Timer timer;

  public MainView(Usuario u) {
    super("Biblioteca escolar | " + u.getNombre() + " | " + u.getRol());
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    JPanel base = new JPanel(new BorderLayout(12, 12));
    base.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    JPanel top = new JPanel(new BorderLayout());
    JLabel title = new JLabel("Biblioteca escolar");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 25));
    top.add(title, BorderLayout.WEST);
    JPanel nav = new JPanel();
    nav.add(actualizar);
    nav.add(salir);
    top.add(nav, BorderLayout.EAST);
    base.add(top, BorderLayout.NORTH);
    libros = new Grid(u.puedeAdministrar());
    categorias = new Grid(true);
    estudiantes = new Grid(true);
    tabs.add("Libros", libros.panel);
    if (u.puedeAdministrar()) {
      tabs.add("Categorías", categorias.panel);
      tabs.add("Estudiantes", estudiantes.panel);
    }
    JPanel loans = new JPanel(new BorderLayout(8, 8));
    JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
    fields.add(new JLabel("Estudiante"));
    fields.add(new JLabel("Libro"));
    fields.add(alumno);
    fields.add(libro);
    fields.add(prestar);
    fields.add(devolver);
    loans.add(fields, BorderLayout.NORTH);
    setup(prestamos);
    loans.add(new JScrollPane(prestamos), BorderLayout.CENTER);
    loans.add(
        new JLabel("Préstamos activos · Plazo de 14 días · Selecciona una fila para devolver"),
        BorderLayout.SOUTH);
    tabs.add("Préstamos y devoluciones", loans);
    tipoReporte =
        new JComboBox<>(
            u.puedeAdministrar()
                ? new String[] {
                  "Libros más prestados", "Historial por estudiante", "Préstamos activos"
                }
                : new String[] {"Mi historial", "Mis préstamos activos"});
    JPanel reports = new JPanel(new BorderLayout(8, 8));
    JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
    filter.add(tipoReporte);
    filter.add(filtroAlumno);
    filter.add(verReporte);
    reports.add(filter, BorderLayout.NORTH);
    setup(reporte);
    reports.add(new JScrollPane(reporte), BorderLayout.CENTER);
    tabs.add("Reportes", reports);
    base.add(tabs, BorderLayout.CENTER);
    JPanel bottom = new JPanel(new BorderLayout(10, 0));
    bottom.add(estado, BorderLayout.CENTER);
    progress.setPreferredSize(new Dimension(140, 16));
    bottom.add(progress, BorderLayout.WEST);
    bottom.add(reloj, BorderLayout.EAST);
    base.add(bottom, BorderLayout.SOUTH);
    timer =
        new javax.swing.Timer(
            500, e -> reloj.setText(java.time.LocalTime.now().withNano(0).toString()));
    timer.start();
    setContentPane(base);
    setSize(1100, 700);
    setMinimumSize(new Dimension(900, 600));
    setLocationRelativeTo(null);
  }

  @Override
  public void dispose() {
    timer.stop();
    super.dispose();
  }

  private static void setup(JTable t) {
    t.setRowHeight(28);
    t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    t.setAutoCreateRowSorter(true);
    t.setFillsViewportHeight(true);
  }

  public static void fill(JTable t, String[] cols, java.util.List<Object[]> rows) {
    DefaultTableModel m =
        new DefaultTableModel(cols, 0) {
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    rows.forEach(m::addRow);
    t.setModel(m);
  }

  public static int selectedId(JTable t) {
    int row = t.getSelectedRow();
    if (row < 0) throw new IllegalArgumentException("Selecciona una fila primero.");
    return ((Number) t.getValueAt(row, 0)).intValue();
  }

  public void busy(boolean value, String message) {
    estado.setText(message);
    progress.setIndeterminate(value);
    setButtons(getContentPane(), !value);
  }

  private void setButtons(Container p, boolean enable) {
    for (Component c : p.getComponents()) {
      if (c instanceof JButton || c instanceof JComboBox<?>) c.setEnabled(enable);
      if (c instanceof Container child) setButtons(child, enable);
    }
  }
}
