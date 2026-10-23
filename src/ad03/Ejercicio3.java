package ad03;

import java.sql.*;
import java.util.Scanner;

public class Ejercicio3 {

	private static final String URL = "jdbc:mysql://localhost:3306/dbformacion";
	private static final String USER = "root";
	private static final String PASSWORD = "";

	public static void main(String[] args) {

		String dni;
		String nombre = null;
		String nombreCurso = null;
		boolean alumnoNuevo = false;

		try (Scanner sc = new Scanner(System.in); Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
			System.out.print("Introduce el DNI del alumno: ");
			dni = sc.nextLine().trim().toUpperCase();
			if (!dni.matches("[0-9]{8}[A-Z]")) {
				System.out.println("DNI no válido.");
				return;
			}

			String sqlAlumno = "SELECT nombre FROM alumnos WHERE dni = ?";
			try (PreparedStatement ps = con.prepareStatement(sqlAlumno)) {
				ps.setString(1, dni);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						nombre = rs.getString("nombre");
						System.out.println("Alumno: " + nombre);
					} else {
						System.out.print("Nombre del nuevo alumno: ");
						nombre = sc.nextLine().trim();
						if (nombre.isEmpty()) {
							System.out.println("El nombre no puede estar vacío.");
							return;
						}
						alumnoNuevo = true;
					}
				}
			}

			String sqlCursos = """
					SELECT curso.id_curso, curso.titulo, aula.capacidad,
					       COUNT(matricula.dni) AS matriculados,
					       aula.capacidad - COUNT(matricula.dni) AS libres
					FROM cursos curso
					JOIN aulas aula ON curso.id_aula = aula.id_aula
					LEFT JOIN matriculas matricula ON curso.id_curso = matricula.id_curso
					GROUP BY curso.id_curso, curso.titulo, aula.capacidad
					ORDER BY curso.id_curso
					""";

			try (PreparedStatement ps = con.prepareStatement(sqlCursos); ResultSet rs = ps.executeQuery()) {
				System.out.println("\nCursos disponibles:");
				while (rs.next()) {
					System.out.printf("%d. %-25s plazas libres: %d%n", rs.getInt("id_curso"), rs.getString("titulo"),
							rs.getInt("libres"));
				}
			}

			System.out.print("Introduce el ID del curso: ");
			int idCurso;
			try {
				idCurso = Integer.parseInt(sc.nextLine().trim());
			} catch (NumberFormatException e) {
				System.out.println("El ID debe ser un número entero.");
				return;
			}

			con.setAutoCommit(false);
			try {
				String sqlCurso = "SELECT titulo FROM cursos WHERE id_curso = ?";
				try (PreparedStatement ps = con.prepareStatement(sqlCurso)) {
					ps.setInt(1, idCurso);
					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next()) {
							throw new SQLException("El curso no existe.");
						}
						nombreCurso = rs.getString("titulo");
					}
				}

				String sqlDuplicado = "SELECT dni FROM matriculas WHERE dni = ? AND id_curso = ?";
				try (PreparedStatement ps = con.prepareStatement(sqlDuplicado)) {
					ps.setString(1, dni);
					ps.setInt(2, idCurso);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							throw new SQLException("El alumno ya está matriculado en ese curso.");
						}
					}
				}

				String sqlCapacidad = """
						SELECT aula.capacidad - COUNT(matricula.dni) AS libres
						FROM cursos curso
						JOIN aulas aula ON curso.id_aula = aula.id_aula
						LEFT JOIN matriculas matricula ON curso.id_curso = matricula.id_curso
						WHERE curso.id_curso = ?
						GROUP BY aula.capacidad
						""";

				try (PreparedStatement ps = con.prepareStatement(sqlCapacidad)) {
					ps.setInt(1, idCurso);
					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next() || rs.getInt("libres") <= 0) {
							throw new SQLException("No hay plazas libres en " + nombreCurso + ".");
						}
					}
				}

				if (alumnoNuevo) {
					String sqlInsertAlumno = "INSERT INTO alumnos (dni, nombre) VALUES (?, ?)";
					try (PreparedStatement ps = con.prepareStatement(sqlInsertAlumno)) {
						ps.setString(1, dni);
						ps.setString(2, nombre);
						ps.executeUpdate();
					}
				}

				String sqlMatricula = """
						INSERT INTO matriculas (dni, id_curso, fecha_matricula)
						VALUES (?, ?, CURRENT_DATE)
						""";
				try (PreparedStatement ps = con.prepareStatement(sqlMatricula)) {
					ps.setString(1, dni);
					ps.setInt(2, idCurso);
					ps.executeUpdate();
				}
				con.commit();
				System.out.println("Matrícula registrada para " + nombre + " en " + nombreCurso + ".");
			} catch (SQLException e) {
				con.rollback();
				System.out.println("Matrícula cancelada: " + e.getMessage());
			} finally {
				con.setAutoCommit(true);
			}

		} catch (SQLException e) {
			System.err.println("Error de base de datos:");
			e.printStackTrace();
		}
	}
}
