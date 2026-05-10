package co.unicauca.especialista_service.repository;

import co.unicauca.especialista_service.model.DisponibilidadEspecialista;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Juan Martin
 */
@Repository
public class SQLDisponibilidadRepositorio implements DisponibilidadRepositorio{

    private final Connection connection;

    public SQLDisponibilidadRepositorio(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(String id_especialista, DisponibilidadEspecialista disponibilidad) {
        String deleteSql = "DELETE FROM disponibilidad_especialista WHERE especialista_id = ?";
        String insertSql = """
           INSERT INTO disponibilidad_especialista
           (id, especialista_id, dias_atencion, hora_inicio, hora_fin, intervalo_seg, num_semanas)
           VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, id_especialista);
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setString(1, java.util.UUID.randomUUID().toString());
                insertStmt.setString(2, id_especialista);
                insertStmt.setString(3, convertirDiasAString(disponibilidad.getDiasAtencion()));
                insertStmt.setString(4, disponibilidad.getHoraInicio().toString());
                insertStmt.setString(5, disponibilidad.getHoraFin().toString());
                insertStmt.setInt(6, disponibilidad.getIntervaloMinutos());
                insertStmt.setInt(7, disponibilidad.getSemanasHabilitadas());
                insertStmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) { }
            throw new RuntimeException("Error guardando disponibilidad", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) { }
        }
    }

    @Override
    public DisponibilidadEspecialista buscarPorEspecialistaId(String id_especialista) {
        String sql = """
            SELECT * FROM disponibilidad_especialista
            WHERE especialista_id = ?
            LIMIT 1
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, id_especialista);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DisponibilidadEspecialista disponibilidad = new DisponibilidadEspecialista();
                    disponibilidad.setDiasAtencion(convertirStringADias(rs.getString("dias_atencion")));
                    disponibilidad.setHoraInicio(LocalTime.parse(rs.getString("hora_inicio")));
                    disponibilidad.setHoraFin(LocalTime.parse(rs.getString("hora_fin")));
                    disponibilidad.setIntervaloMinutos(rs.getInt("intervalo_seg"));
                    disponibilidad.setSemanasHabilitadas(rs.getInt("num_semanas"));
                    return disponibilidad;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando disponibilidad", e);
        }

        return null;
    }

    private String convertirDiasAString(List<DayOfWeek> dias) {
        return String.join(",", dias.stream().map(Enum::name).toList());
    }

    private List<DayOfWeek> convertirStringADias(String texto) {
        return Arrays.stream(texto.split(",")).map(DayOfWeek::valueOf).toList();
    }
}
