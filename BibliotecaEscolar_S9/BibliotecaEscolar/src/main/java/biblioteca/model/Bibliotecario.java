package biblioteca.model;

public final class Bibliotecario extends Usuario {
  public Bibliotecario(int id, String nombre, String login, String hash) {
    this(id, nombre, login, hash, "", "");
  }

  public Bibliotecario(int id, String nombre, String login, String hash, String rut, String email) {
    super(id, nombre, login, hash, rut, email);
  }

  @Override
  public boolean puedeAdministrar() {
    return true;
  }

  @Override
  public String getRol() {
    return "BIBLIOTECARIO";
  }
}
