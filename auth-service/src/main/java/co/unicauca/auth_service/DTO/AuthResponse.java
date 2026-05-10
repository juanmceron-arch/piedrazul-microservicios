package co.unicauca.auth_service.DTO;

/**
 *
 * @author Juan Martin
 */
public class AuthResponse {

    private String mensaje;
    private int idUsuario;
    private String rolUsuario;
    private boolean autenticado;
    private String nombreUsuario;
    private String apellidoUsuario;

    public AuthResponse() {
    }

    public AuthResponse(String mensaje, int idUsuario, String rolUsuario, boolean autenticado) {
        this.mensaje = mensaje;
        this.idUsuario = idUsuario;
        this.rolUsuario = rolUsuario;
        this.autenticado = autenticado;
    }

    public AuthResponse(String mensaje, int idUsuario, String rolUsuario, boolean autenticado, String nombreUsuario, String apellidoUsuario) {
        this.mensaje = mensaje;
        this.idUsuario = idUsuario;
        this.rolUsuario = rolUsuario;
        this.autenticado = autenticado;
        this.nombreUsuario = nombreUsuario;
        this.apellidoUsuario = apellidoUsuario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getRolUsuario() {
        return rolUsuario;
    }

    public void setRolUsuario(String rolUsuario) {
        this.rolUsuario = rolUsuario;
    }

    public boolean isAutenticado() {
        return autenticado;
    }

    public void setAutenticado(boolean autenticado) {
        this.autenticado = autenticado;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getApellidoUsuario() {
        return apellidoUsuario;
    }

    public void setApellidoUsuario(String apellidoUsuario) {
        this.apellidoUsuario = apellidoUsuario;
    }
}