package biblioteca;

import biblioteca.config.DatabaseConnection;
import biblioteca.controller.AppController;
import javax.swing.*;

public final class Main {
  public static void main(String[] args) {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  try {
                    DatabaseConnection.getInstance().close();
                  } catch (Exception ignored) {
                  }
                }));
    SwingUtilities.invokeLater(
        () -> {
          try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          } catch (Exception ignored) {
          }
          new AppController().start();
        });
  }
}
