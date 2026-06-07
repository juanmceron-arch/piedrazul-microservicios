package co.unicauca.especialista_service.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Juan Martin
 */
public class SQLConexionBD {
     private static final String URL="jdbc:sqlite:/app/data/especialista.bd";
    
    public static Connection SQLiteConexionBD()throws SQLException{
        return DriverManager.getConnection(URL);
    }
    
    public static void inicializar() {

        try (Connection conecta=SQLiteConexionBD();
            Statement stmt=conecta.createStatement();) {

            // TABLA ESPECIALISTAS
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS especialistas (
                    id TEXT PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    especialidad TEXT NOT NULL
                )
            """);
            
            // TABLA disponipilidad_especialista
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS disponibilidad_especialista (
                    id TEXT PRIMARY KEY,
                    especialista_id TEXT NOT NULL,
                    dias_atencion TEXT NOT NULL,
                    hora_inicio TEXT NOT NULL,
                    hora_fin TEXT NOT NULL,
                    intervalo_seg INTEGER NOT NULL,
                    num_semanas INTEGER NOT NULL,
                    FOREIGN KEY (especialista_id) REFERENCES especialistas(id)
                )
            """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
