package biblioteca.dao;

import biblioteca.model.*;
import java.sql.*;
import java.util.*;

public final class CategoriaDao extends JdbcDao<Categoria> {
  public CategoriaDao(Connection c) {
    super(c);
  }

  protected Categoria map(ResultSet r) throws SQLException {
    return new Categoria(r.getInt("id"), r.getString("nombre"));
  }

  public int crear(Categoria x) throws SQLException {
    return insert("INSERT INTO categorias(nombre) VALUES(?)", x.nombre());
  }

  public List<Categoria> listar() throws SQLException {
    return query("SELECT * FROM categorias ORDER BY nombre");
  }

  public Categoria buscar(int id) throws SQLException {
    return first(query("SELECT * FROM categorias WHERE id=?", id));
  }

  public void actualizar(Categoria x) throws SQLException {
    change("UPDATE categorias SET nombre=? WHERE id=?", x.nombre(), x.id());
  }

  public void eliminar(int id) throws SQLException {
    change("DELETE FROM categorias WHERE id=?", id);
  }
}
