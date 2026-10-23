package ad03;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Scanner;

public class Ejercicio4 {

	private static final String URL = "jdbc:mysql://localhost:3306/dbformacion";
	private static final String USER = "root";
	private static final String PASSWORD = "";

	public static void main(String[] args) {

		String sqlCursos = "SELECT id_curso, titulo FROM cursos ORDER BY id_curso";
		try (Scanner sc = new Scanner(System.in);
				Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(sqlCursos)) {

			System.out.println("Cursos:");
			while (rs.next()) {
				System.out.println(rs.getInt("id_curso") + ". " + rs.getString("titulo"));
			}

			System.out.print("Introduce el ID del curso: ");
			int idCurso = Integer.parseInt(sc.nextLine().trim());
			try (CallableStatement cs = con.prepareCall("{? = CALL obtener_numero_matriculados(?)}")) {
				cs.registerOutParameter(1, Types.INTEGER);
				cs.setInt(2, idCurso);
				cs.execute();
				int total = cs.getInt(1);
				System.out.println("Número de alumnos matriculados: " + total);
			}

		} catch (NumberFormatException e) {
			System.out.println("El ID debe ser un número entero.");
		} catch (SQLException e) {
			System.err.println("Error de base de datos:");
			e.printStackTrace();
		}
	}
}
