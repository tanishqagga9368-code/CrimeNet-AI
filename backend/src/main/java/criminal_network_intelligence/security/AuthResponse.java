package criminal_network_intelligence.security;

public class AuthResponse {

    private String token;
    private String username;
    private String fullName;
    private String role;
    private long expiresIn;

    public AuthResponse() {
    }

    public AuthResponse(
            String token,
            String username,
            String fullName,
            String role,
            long expiresIn
    ) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}