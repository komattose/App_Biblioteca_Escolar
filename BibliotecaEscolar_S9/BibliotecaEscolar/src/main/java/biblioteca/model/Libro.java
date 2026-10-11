package biblioteca.model;

public record Libro(
    int id,
    String titulo,
    String autor,
    String editorial,
    String isbn,
    int categoriaId,
    int stock) {
  public String toString() {
    return titulo + " [disponibles: " + stock + "]";
  }
}
