package biblioteca.dao;

import java.sql.SQLException;
import java.util.List;

/** Contrato común para las cinco entidades persistentes. */
public interface CrudDao<T> {
  int crear(T entidad) throws SQLException;

  List<T> listar() throws SQLException;

  T buscar(int id) throws SQLException;

  void actualizar(T entidad) throws SQLException;

  void eliminar(int id) throws SQLException;
}
