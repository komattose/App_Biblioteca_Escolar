package biblioteca.model;

public record Categoria(int id, String nombre) {
  public String toString() {
    return nombre;
  }
}
