package biblioteca.dao;

import biblioteca.model.*;
import java.sql.*;
import java.util.*;

public final class PrestamoDao extends JdbcDao<Prestamo> {
  public PrestamoDao(Connection c) {
    super(c);
  }

  protected Prestamo map(ResultSet r) throws SQLException {
    java.sql.Date d = r.getDate("fecha_devolucion");
    return new Prestamo(
        r.getInt("id"),
        r.getInt("id_estudiante"),
        r.getInt("id_libro"),
        r.getDate("fecha_prestamo").toLocalDate(),
        r.getDate("vencimiento").toLocalDate(),
        d == null ? null : d.toLocalDate(),
        r.getInt("dias_atraso"));
  }

  public int crear(Prestamo x) throws SQLException {
    return insert(
        "INSERT INTO"
            + " prestamos(id_estudiante,id_libro,fecha_prestamo,vencimiento,fecha_devolucion,dias_atraso)"
            + " VALUES(?,?,?,?,?,?)",
        x.estudianteId(),
        x.libroId(),
        x.fecha(),
        x.vencimiento(),
        x.devolucion(),
        x.diasAtraso());
  }

  public List<Prestamo> listar() throws SQLException {
    return query("SELECT * FROM prestamos ORDER BY id DESC");
  }

  public Prestamo buscar(int id) throws SQLException {
    return first(query("SELECT * FROM prestamos WHERE id=?", id));
  }

  public Prestamo bloquear(int id) throws SQLException {
    return first(query("SELECT * FROM prestamos WHERE id=? FOR UPDATE", id));
  }

  public boolean activo(int alumno, int libro) throws SQLException {
    return !query(
            "SELECT * FROM prestamos WHERE id_estudiante=? AND id_libro=? AND fecha_devolucion IS"
                + " NULL",
            alumno,
            libro)
        .isEmpty();
  }

  public void actualizar(Prestamo x) throws SQLException {
    change(
        "UPDATE prestamos SET"
            + " id_estudiante=?,id_libro=?,fecha_prestamo=?,vencimiento=?,fecha_devolucion=?,dias_atraso=?"
            + " WHERE id=?",
        x.estudianteId(),
        x.libroId(),
        x.fecha(),
        x.vencimiento(),
        x.devolucion(),
        x.diasAtraso(),
        x.id());
  }

  public void eliminar(int id) throws SQLException {
    change("DELETE FROM prestamos WHERE id=?", id);
  }
}
