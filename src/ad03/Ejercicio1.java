package ad03;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Ejercicio1 {

	private static final String URL = "jdbc:mysql://localhost:3306/dbformacion";
	private static final String USER = "root";
	private static final String PASSWORD = "";

	public static void main(String[] args) {

		String sql = """
				SELECT curso.id_curso, curso.titulo, curso.fecha,
				       COUNT(m.dni) AS matriculados,
				       aula.capacidad, aula.nombre AS aula, aula.edificio
				FROM cursos curso
				JOIN aulas aula ON curso.id_aula = aula.id_aula
				LEFT JOIN matriculas m ON curso.id_curso = m.id_curso
				GROUP BY curso.id_curso, curso.titulo, curso.fecha,
				         aula.capacidad, aula.nombre, aula.edificio
				ORDER BY curso.fecha
				""";

		try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(sql)) {

			System.out.printf(
					"%-3s %-25s %-13s %-12s %-25s %-15s%n",
					"ID", "Curso", "Fecha", "Matrículas", "Aula", "Edificio");

			System.out.println("-".repeat(95));

			while (rs.next()) {
				int matriculados = rs.getInt("matriculados");
				int capacidad = rs.getInt("capacidad");

				System.out.printf("%-3d %-25s %-15s %-10s %-25s %-15s%n",
						rs.getInt("id_curso"),
						rs.getString("titulo"),
						rs.getDate("fecha"),
						matriculados + "/" + capacidad,
						rs.getString("aula"),
						rs.getString("edificio"));
			}

		} catch (SQLException e) {
			System.err.println("Error al consultar los cursos:");
			e.printStackTrace();
		}
	}
}
