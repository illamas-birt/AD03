package ad03;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Ejercicio2 {

	private static final String URL = "jdbc:mysql://localhost:3306/dbformacion";
	private static final String USER = "root";
	private static final String PASSWORD = "";

	public static void main(String[] args) {

		try (Scanner sc = new Scanner(System.in); Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
			System.out.print("Introduce el DNI del alumno: ");
			String dni = sc.nextLine().trim().toUpperCase();
			if (!dni.matches("[0-9]{8}[A-Z]")) {
				System.out.println("DNI no válido.");
				return;
			}

			String sqlBuscar = "SELECT nombre FROM alumnos WHERE dni = ?";
			try (PreparedStatement ps = con.prepareStatement(sqlBuscar)) {
				ps.setString(1, dni);
				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next()) {
						System.out.println("No existe ningún alumno con ese DNI.");
						return;
					}
					String nombreActual = rs.getString("nombre");
					System.out.println("Nombre actual: " + nombreActual);
				}
			}

			System.out.print("Nuevo nombre (deja en blanco para no modificar): ");
			String nuevoNombre = sc.nextLine().trim();
			if (nuevoNombre.isEmpty()) {
				System.out.println("Nombre vacío. No se han realizado cambios.");
				return;
			}

			String sqlActualizar = "UPDATE alumnos SET nombre = ? WHERE dni = ?";
			try (PreparedStatement ps = con.prepareStatement(sqlActualizar)) {
				ps.setString(1, nuevoNombre);
				ps.setString(2, dni);
				int filas = ps.executeUpdate();
				if (filas > 0) {
					System.out.println("Nombre actualizado correctamente.");
				} else {
					System.out.println("No se ha modificado ningún alumno.");
				}
			}

		} catch (SQLException e) {
			System.err.println("Error de base de datos:");
			e.printStackTrace();
		}
	}
}
