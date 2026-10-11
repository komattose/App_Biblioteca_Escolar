package biblioteca.dao;

import java.sql.*;
import java.util.*;

public final class ReporteDao {
  private final Connection c;

  public ReporteDao(Connection c) {
    this.c = c;
  }

  public record Tabla(String[] columnas, List<Object[]> filas) {}

  private Tabla query(String sql, Object... args) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql)) {
      for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
      try (ResultSet r = p.executeQuery()) {
        var m = r.getMetaData();
        String[] cols = new String[m.getColumnCount()];
        for (int i = 0; i < cols.length; i++) cols[i] = m.getColumnLabel(i + 1);
        List<Object[]> rows = new ArrayList<>();
        while (r.next()) {
          Object[] a = new Object[cols.length];
          for (int i = 0; i < a.length; i++) a[i] = r.getObject(i + 1);
          rows.add(a);
        }
        return new Tabla(cols, rows);
      }
    }
  }

  public Tabla populares() throws SQLException {
    return query(
        "SELECT l.titulo AS Libro,COUNT(p.id) AS Prestamos FROM libros l LEFT JOIN prestamos p ON"
            + " l.id=p.id_libro GROUP BY l.id,l.titulo ORDER BY Prestamos DESC,Libro");
  }

  public Tabla historial(Integer alumno, boolean activos) throws SQLException {
    String sql =
        "SELECT p.id AS ID,u.nombre AS Estudiante,l.titulo AS Libro,p.fecha_prestamo AS"
            + " Fecha,p.vencimiento AS Vencimiento,p.fecha_devolucion AS Devolucion,CASE WHEN"
            + " p.fecha_devolucion IS NULL THEN GREATEST(0,DATEDIFF(CURRENT_DATE,p.vencimiento))"
            + " ELSE p.dias_atraso END AS Dias_atraso FROM prestamos p JOIN usuarios u ON"
            + " u.id=p.id_estudiante JOIN libros l ON l.id=p.id_libro WHERE 1=1";
    if (activos) sql += " AND p.fecha_devolucion IS NULL";
    if (alumno != null) sql += " AND p.id_estudiante=?";
    sql += " ORDER BY p.id DESC";
    return alumno == null ? query(sql) : query(sql, alumno);
  }
}
