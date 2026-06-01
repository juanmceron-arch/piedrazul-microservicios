package co.unicauca.appointment_service.config;

import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CitaSchemaMigration implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public CitaSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (requiereMigracionEstados()) {
            migrarEstados();
        }

        asegurarColumnasPaciente();
    }

    private void migrarEstados() {
        jdbcTemplate.execute("PRAGMA foreign_keys=off");
        try {
            jdbcTemplate.execute("drop table if exists cita_migracion");
            jdbcTemplate.execute("""
                    create table cita_migracion (
                        id varchar(255) not null primary key,
                        duracion integer,
                        especialista_especialidad varchar(255),
                        especialista_id varchar(255),
                        especialista_nombre varchar(255),
                        estado varchar(255) check (estado in ('PENDIENTE','AGENDADA','CANCELADA','REAGENDADA','ASISTIDA','NO_ASISTIDA')),
                        fecha date,
                        hora time,
                        paciente_id integer not null,
                        paciente_nombre varchar(255),
                        paciente_apellido varchar(255),
                        paciente_telefono varchar(255),
                        paciente_fecha_nacimiento date,
                        paciente_correo varchar(255),
                        paciente_genero varchar(255)
                    )
                    """);
            jdbcTemplate.execute("""
                    insert into cita_migracion (
                        id, duracion, especialista_especialidad, especialista_id, especialista_nombre,
                        estado, fecha, hora, paciente_id, paciente_nombre
                    )
                    select
                        id, duracion, especialista_especialidad, especialista_id, especialista_nombre,
                        estado, fecha, hora, paciente_id, paciente_nombre
                    from cita
                    """);
            jdbcTemplate.execute("drop table cita");
            jdbcTemplate.execute("alter table cita_migracion rename to cita");
        } finally {
            jdbcTemplate.execute("PRAGMA foreign_keys=on");
        }
    }

    private boolean requiereMigracionEstados() {
        String sql = jdbcTemplate.query(
                "select sql from sqlite_master where type = 'table' and name = 'cita'",
                rs -> rs.next() ? rs.getString("sql") : ""
        );

        String normalizada = String.valueOf(sql).toUpperCase();

        return normalizada.contains("CHECK")
                && normalizada.contains("AGENDADA")
                && !normalizada.contains("ASISTIDA");
    }

    private void asegurarColumnasPaciente() {
        agregarColumnaSiNoExiste("paciente_apellido", "varchar(255)");
        agregarColumnaSiNoExiste("paciente_telefono", "varchar(255)");
        agregarColumnaSiNoExiste("paciente_fecha_nacimiento", "date");
        agregarColumnaSiNoExiste("paciente_correo", "varchar(255)");
        agregarColumnaSiNoExiste("paciente_genero", "varchar(255)");
    }

    private void agregarColumnaSiNoExiste(String nombre, String tipo) {
        boolean existe = jdbcTemplate.queryForList("PRAGMA table_info(cita)").stream()
                .map(Map.class::cast)
                .anyMatch(columna -> nombre.equalsIgnoreCase(String.valueOf(columna.get("name"))));

        if (!existe) {
            jdbcTemplate.execute("alter table cita add column " + nombre + " " + tipo);
        }
    }
}
