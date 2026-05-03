package app.domain.models.auth;

import app.domain.models.enums.RoleType;

public class AuthSession {

    private String token;
    private String tokenType;
    private String identificationNumber;
    private String username;
    private RoleType role;

    public AuthSession() {
    }

    public AuthSession(String token,
                       String tokenType,
                       String identificationNumber,
                       String username,
                       RoleType role) {
        this.token = token;
        this.tokenType = tokenType;
        this.identificationNumber = identificationNumber;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public String getUsername() {
        return username;
    }

    public RoleType getRole() {
        return role;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }
}