package biblioteca;

import biblioteca.controller.AppController;
import biblioteca.view.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Prueba del controlador real y render de los componentes Swing. Requiere escritorio. */
public final class UiSmokeTest {
  private static MainView mainView;

  private static void waitReady() throws Exception {
    for (int i = 0; i < 200; i++) {
      final boolean[] ready = {false};
      SwingUtilities.invokeAndWait(
          () -> {
            for (Frame f : Frame.getFrames())
              if (f instanceof MainView v && v.isVisible()) {
                mainView = v;
                ready[0] = v.actualizar.isEnabled() && v.libros.table.getRowCount() > 0;
              }
          });
      if (ready[0]) return;
      Thread.sleep(100);
    }
    throw new AssertionError("La interfaz no terminó de cargar");
  }

  private static void capture(Path path) throws Exception {
    AtomicReference<BufferedImage> image = new AtomicReference<>();
    SwingUtilities.invokeAndWait(
        () -> {
          BufferedImage b =
              new BufferedImage(
                  mainView.getWidth(), mainView.getHeight(), BufferedImage.TYPE_INT_RGB);
          Graphics2D g = b.createGraphics();
          mainView.printAll(g);
          g.dispose();
          image.set(b);
        });
    ImageIO.write(image.get(), "png", path.toFile());
  }

  private static void login(String user, String pass) throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          for (Frame f : Frame.getFrames())
            if (f instanceof LoginView v && v.isVisible()) {
              v.usuario.setText(user);
              v.password.setText(pass);
              v.entrar.doClick();
              return;
            }
          throw new AssertionError("Sin login");
        });
    waitReady();
  }

  public static void main(String[] args) throws Exception {
    Path dir = Path.of(args[0]);
    Files.createDirectories(dir);
    try {
      SwingUtilities.invokeAndWait(() -> new AppController().start());
      login("admin", "Admin2026!");
      for (int i = 0; i < 5; i++) {
        final int index = i;
        SwingUtilities.invokeAndWait(() -> mainView.tabs.setSelectedIndex(index));
        capture(dir.resolve("admin-" + i + ".png"));
      }
      SwingUtilities.invokeAndWait(
          () -> {
            mainView.tabs.setSelectedIndex(4);
            mainView.verReporte.doClick();
          });
      waitReady();
      capture(dir.resolve("reporte.png"));
      SwingUtilities.invokeAndWait(() -> mainView.salir.doClick());
      login("ana", "Estudiante2026!");
      SwingUtilities.invokeAndWait(
          () -> {
            if (mainView.tabs.getTabCount() != 3) throw new AssertionError("Permisos incorrectos");
            mainView.tabs.setSelectedIndex(1);
          });
      capture(dir.resolve("estudiante.png"));
      System.out.println(
          "OK: login real, cinco pantallas de bibliotecario, reporte, cierre de sesión y tres"
              + " pantallas de estudiante");
    } finally {
      SwingUtilities.invokeAndWait(
          () -> {
            for (Frame f : Frame.getFrames()) f.dispose();
          });
      biblioteca.config.DatabaseConnection.getInstance().close();
    }
    System.exit(0);
  }
}
