package co.unicauca.auth_service.DTO;

/**
 *
 * @author Juan Martin
 */
public class LoginRequest {
    private int id;
    private String passwordHash;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
        
}
