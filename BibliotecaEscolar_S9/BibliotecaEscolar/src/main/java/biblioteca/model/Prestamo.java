package biblioteca.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Prestamo(
    int id,
    int estudianteId,
    int libroId,
    LocalDate fecha,
    LocalDate vencimiento,
    LocalDate devolucion,
    int diasAtraso) {
  public long atrasoActual() {
    return devolucion == null
        ? Math.max(0, ChronoUnit.DAYS.between(vencimiento, LocalDate.now()))
        : diasAtraso;
  }
}
