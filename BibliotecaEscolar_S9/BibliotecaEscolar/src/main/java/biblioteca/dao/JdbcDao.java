package biblioteca.dao;

import java.sql.*;
import java.util.*;

/** Infraestructura JDBC compartida. Los identificadores SQL son constantes internas. */
public abstract class JdbcDao<T> implements CrudDao<T> {
  protected final Connection c;

  protected JdbcDao(Connection c) {
    this.c = c;
  }

  protected abstract T map(ResultSet r) throws SQLException;

  protected List<T> query(String sql, Object... args) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql)) {
      bind(p, args);
      try (ResultSet r = p.executeQuery()) {
        List<T> out = new ArrayList<>();
        while (r.next()) out.add(map(r));
        return out;
      }
    }
  }

  protected int change(String sql, Object... args) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql)) {
      bind(p, args);
      return p.executeUpdate();
    }
  }

  protected int insert(String sql, Object... args) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      bind(p, args);
      p.executeUpdate();
      try (ResultSet r = p.getGeneratedKeys()) {
        if (r.next()) return r.getInt(1);
        return 0;
      }
    }
  }

  protected void bind(PreparedStatement p, Object[] args) throws SQLException {
    for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
  }

  protected T first(List<T> list) {
    return list.isEmpty() ? null : list.get(0);
  }
}
