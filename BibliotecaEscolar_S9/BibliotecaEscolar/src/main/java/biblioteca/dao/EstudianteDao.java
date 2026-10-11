package biblioteca.dao;

import biblioteca.model.*;
import java.sql.*;
import java.util.*;

public final class EstudianteDao extends JdbcDao<Estudiante> {
  public EstudianteDao(Connection c) {
    super(c);
  }

  private static final String SELECT =
      "SELECT u.*,e.curso FROM estudiantes e JOIN usuarios u ON u.id=e.usuario_id";

  protected Estudiante map(ResultSet r) throws SQLException {
    return new Estudiante(
        r.getInt("id"),
        r.getString("nombre"),
        r.getString("login"),
        r.getString("contrasena"),
        r.getString("rut"),
        r.getString("curso"),
        r.getString("correo"));
  }

  public int crear(Estudiante x) throws SQLException {
    change(
        "INSERT INTO estudiantes(id,usuario_id,nombre,rut,curso,correo) VALUES(?,?,?,?,?,?)",
        x.getId(),
        x.getId(),
        x.getNombre(),
        x.getRut(),
        x.getCurso(),
        x.getEmail());
    return x.getId();
  }

  public List<Estudiante> listar() throws SQLException {
    return query(SELECT + " ORDER BY u.nombre");
  }

  public Estudiante buscar(int id) throws SQLException {
    return first(query(SELECT + " WHERE e.usuario_id=?", id));
  }

  public void actualizar(Estudiante x) throws SQLException {
    change(
        "UPDATE estudiantes SET nombre=?,rut=?,curso=?,correo=? WHERE usuario_id=?",
        x.getNombre(),
        x.getRut(),
        x.getCurso(),
        x.getEmail(),
        x.getId());
  }

  public void eliminar(int id) throws SQLException {
    change("DELETE FROM estudiantes WHERE usuario_id=?", id);
  }
}
