package biblioteca.view;

import java.awt.*;
import javax.swing.*;

public final class LoginView extends JFrame {
  public final JTextField usuario = new JTextField(20);
  public final JPasswordField password = new JPasswordField(20);
  public final JButton entrar = new JButton("Ingresar");
  public final JLabel estado = new JLabel("Ingresa con tu cuenta de biblioteca");

  public LoginView() {
    super("Biblioteca escolar | Acceso");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    JPanel p = new JPanel(new GridLayout(0, 1, 8, 8));
    p.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
    JLabel title = new JLabel("Biblioteca escolar");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 24));
    p.add(title);
    p.add(estado);
    p.add(new JLabel("Usuario"));
    p.add(usuario);
    p.add(new JLabel("Contraseña"));
    p.add(password);
    p.add(entrar);
    setContentPane(p);
    getRootPane().setDefaultButton(entrar);
    pack();
    setMinimumSize(new Dimension(460, 340));
    setLocationRelativeTo(null);
  }
}
