package br.com.cpa.questionario.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") 
public class User {

    @Id
    private String username;
    private String password;
    private String name;
    private String role;
    private String className;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) {
        if (role == null) {
            this.role = "ROLE_USER";
            return;
        }
        this.role = role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
