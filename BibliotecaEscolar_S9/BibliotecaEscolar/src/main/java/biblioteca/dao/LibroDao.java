package biblioteca.dao;

import biblioteca.model.*;
import java.sql.*;
import java.util.*;

public final class LibroDao extends JdbcDao<Libro> {
  public LibroDao(Connection c) {
    super(c);
  }

  protected Libro map(ResultSet r) throws SQLException {
    return new Libro(
        r.getInt("id"),
        r.getString("titulo"),
        r.getString("autor"),
        r.getString("editorial"),
        r.getString("isbn"),
        r.getInt("id_categoria"),
        r.getInt("stock"));
  }

  public int crear(Libro x) throws SQLException {
    return insert(
        "INSERT INTO libros(titulo,autor,editorial,isbn,id_categoria,stock) VALUES(?,?,?,?,?,?)",
        x.titulo(),
        x.autor(),
        x.editorial(),
        x.isbn(),
        x.categoriaId(),
        x.stock());
  }

  public List<Libro> listar() throws SQLException {
    return query("SELECT * FROM libros ORDER BY titulo");
  }

  public Libro buscar(int id) throws SQLException {
    return first(query("SELECT * FROM libros WHERE id=?", id));
  }

  public Libro bloquear(int id) throws SQLException {
    return first(query("SELECT * FROM libros WHERE id=? FOR UPDATE", id));
  }

  public void actualizar(Libro x) throws SQLException {
    change(
        "UPDATE libros SET titulo=?,autor=?,editorial=?,isbn=?,id_categoria=?,stock=? WHERE id=?",
        x.titulo(),
        x.autor(),
        x.editorial(),
        x.isbn(),
        x.categoriaId(),
        x.stock(),
        x.id());
  }

  public void eliminar(int id) throws SQLException {
    change("DELETE FROM libros WHERE id=?", id);
  }

  public boolean descontar(int id) throws SQLException {
    return change("UPDATE libros SET stock=stock-1 WHERE id=? AND stock>0", id) == 1;
  }

  public void reponer(int id) throws SQLException {
    change("UPDATE libros SET stock=stock+1 WHERE id=?", id);
  }
}
