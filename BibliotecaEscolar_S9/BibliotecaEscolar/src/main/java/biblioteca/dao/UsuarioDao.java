package biblioteca.dao;

import biblioteca.model.*;
import java.sql.*;
import java.util.*;

public final class UsuarioDao extends JdbcDao<Usuario> {
  public UsuarioDao(Connection c) {
    super(c);
  }

  protected Usuario map(ResultSet r) throws SQLException {
    int id = r.getInt("id");
    String n = r.getString("nombre"), l = r.getString("login"), h = r.getString("contrasena");
    return r.getString("rol").equals("BIBLIOTECARIO")
        ? new Bibliotecario(id, n, l, h, r.getString("rut"), r.getString("correo"))
        : new Estudiante(id, n, l, h, r.getString("rut"), "", r.getString("correo"));
  }

  public int crear(Usuario x) throws SQLException {
    return insert(
        "INSERT INTO usuarios(nombre,login,contrasena,rol,rut,correo) VALUES(?,?,?,?,?,?)",
        x.getNombre(),
        x.getLogin(),
        x.getPasswordHash(),
        x.getRol(),
        x.getRut(),
        x.getEmail());
  }

  public List<Usuario> listar() throws SQLException {
    return query("SELECT * FROM usuarios ORDER BY nombre");
  }

  public Usuario buscar(int id) throws SQLException {
    return first(query("SELECT * FROM usuarios WHERE id=?", id));
  }

  public Usuario porLogin(String login) throws SQLException {
    return first(query("SELECT * FROM usuarios WHERE login=?", login));
  }

  public void actualizar(Usuario x) throws SQLException {
    change(
        "UPDATE usuarios SET nombre=?,login=?,contrasena=?,rol=?,rut=?,correo=? WHERE id=?",
        x.getNombre(),
        x.getLogin(),
        x.getPasswordHash(),
        x.getRol(),
        x.getRut(),
        x.getEmail(),
        x.getId());
  }

  public void eliminar(int id) throws SQLException {
    change("DELETE FROM usuarios WHERE id=?", id);
  }
}
