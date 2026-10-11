package biblioteca.model;

/** Abstracción común; los permisos se resuelven mediante polimorfismo. */
public abstract class Usuario {
  private final int id;
  private final String nombre, login, passwordHash, rut, email;

  protected Usuario(
      int id, String nombre, String login, String passwordHash, String rut, String email) {
    this.id = id;
    this.nombre = nombre;
    this.login = login;
    this.passwordHash = passwordHash;
    this.rut = rut;
    this.email = email;
  }

  public int getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public String getLogin() {
    return login;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getRut() {
    return rut;
  }

  public String getEmail() {
    return email;
  }

  public abstract boolean puedeAdministrar();

  public abstract String getRol();

  public String toString() {
    return nombre + " (" + login + ")";
  }
}
