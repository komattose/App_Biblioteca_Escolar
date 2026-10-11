package biblioteca.model;

public final class Estudiante extends Usuario {
  private final String rut, curso, email;

  public Estudiante(
      int id, String nombre, String login, String hash, String rut, String curso, String email) {
    super(id, nombre, login, hash, rut, email);
    this.rut = rut;
    this.curso = curso;
    this.email = email;
  }

  public String getRut() {
    return rut;
  }

  public String getCurso() {
    return curso;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public boolean puedeAdministrar() {
    return false;
  }

  @Override
  public String getRol() {
    return "ESTUDIANTE";
  }
}
