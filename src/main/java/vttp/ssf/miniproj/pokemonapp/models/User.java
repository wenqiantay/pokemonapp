package vttp.ssf.miniproj.pokemonapp.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class User {
    
    @NotNull(message="Required field")
    @Size(min=2, max=100, message="Username must be between 2 and 100 characters.")
    private String username;

    @NotNull(message="Required field")
    @Pattern(regexp="(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}", 
    message="Password must contain 8 or more characters that are of at least one number, and one uppercase and lowercase letter.")
    private String password;

    @NotEmpty(message="Required field")
    @Email(message="Enter email in the correct format")
    private String email;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public String toString() {
        return "User [username=" + username + ", password=" + password + "]";
    }
    
}
