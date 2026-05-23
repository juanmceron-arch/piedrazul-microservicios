package co.unicauca.especialista_service.repository;

import co.unicauca.especialista_service.conexion.SQLConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import co.unicauca.especialista_service.model.Especialista;
import co.unicauca.especialista_service.model.TipoEspecialista;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Juan Martin
 */
@Repository
public class SQLEspecialistaRepositorio implements EspecialistaRepositorio{

    @Override
    public Especialista buscarPorId(String id) {
        String sql = "SELECT * FROM especialistas WHERE id = ?";

        try (Connection conn = SQLConexionBD.SQLiteConexionBD();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Especialista no encontrado: " + id);
                }

                Especialista especialista = new Especialista();
                especialista.setId(rs.getString("id"));
                especialista.setNombre(rs.getString("nombre"));
                especialista.setEspecialidad(
                        TipoEspecialista.valueOf(rs.getString("especialidad"))
                );

                return especialista;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando especialista", e);
        }
    }

    @Override
    public List<Especialista> listarEspecialistas() {
        String sql = "SELECT * FROM especialistas ORDER BY nombre";
        List<Especialista> especialistas = new ArrayList<>();

        try (Connection conn = SQLConexionBD.SQLiteConexionBD();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Especialista especialista = new Especialista();
                especialista.setId(rs.getString("id"));
                especialista.setNombre(rs.getString("nombre"));
                especialista.setEspecialidad(
                        TipoEspecialista.valueOf(rs.getString("especialidad"))
                );
                especialistas.add(especialista);
            }

            return especialistas;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando especialistas", e);
        }
    }

    @Override
    public void guardar(Especialista especialista) {
        String sql = """
            INSERT OR REPLACE INTO especialistas(id, nombre, especialidad)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = SQLConexionBD.SQLiteConexionBD();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, especialista.getId());
            ps.setString(2, especialista.getNombre());
            ps.setString(3, especialista.getEspecialidad().toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error guardando especialista", e);
        }
    }
}
