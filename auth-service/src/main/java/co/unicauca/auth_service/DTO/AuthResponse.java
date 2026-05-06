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

    public AuthResponse() {
    }

    public AuthResponse(String mensaje, int idUsuario, String rolUsuario, boolean autenticado) {
        this.mensaje = mensaje;
        this.idUsuario = idUsuario;
        this.rolUsuario = rolUsuario;
        this.autenticado = autenticado;
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
    
}
