package vttp.ssf.miniproj.pokemonapp.models;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class User {
    
    @NotNull
    @Size(min=2, max=100, message="Username must be between 2 and 100 characters.")
    private String username;

    @NotNull(message="Required field")
    @Pattern(regexp="(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}", 
    message="Password must contain 8 or more characters that are of at least one number, and one uppercase and lowercase letter.")
    private String password;

    @NotEmpty(message="Required field")
    @Email(message="Enter email in the correct format")
    private String email;

    @NotNull(message="Required field")
    private String fullname;

    @NotNull(message="Please choose your gender")
    private String gender;

    private List<Pokemon> myPokemonList;

    private Set<Pokemon> uniquePokemonSet;

    private LocalDate lastCatchDate;

    private int rerollCounter;
    private LocalDate lastRerollDate;

    private Pokemon currentPokemon;

    public Pokemon getCurrentPokemon() {
        return currentPokemon;
    }

    public void setCurrentPokemon(Pokemon currentPokemon) {
        this.currentPokemon = currentPokemon;
    }

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
    public String getFullname() {
        return fullname;
    }
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public List<Pokemon> getMyPokemonList() {
        return myPokemonList;
    }
    public void setMyPokemonList(List<Pokemon> myPokemonList) {
        this.myPokemonList = myPokemonList;
    }
    public LocalDate getLastCatchDate() {
        return lastCatchDate;
    }
    public void setLastCatchDate(LocalDate lastCatchDate) {
        this.lastCatchDate = lastCatchDate;
    }
    public int getRerollCounter() {
        return rerollCounter;
    }
    public void setRerollCounter(int rerollCounter) {
        this.rerollCounter = rerollCounter;
    }
    public LocalDate getLastRerollDate() {
        return lastRerollDate;
    }
    public void setLastRerollDate(LocalDate lastRerollDate) {
        this.lastRerollDate = lastRerollDate;
    }

    public Set<Pokemon> getUniquePokemonSet() {
        return uniquePokemonSet;
    }

    public void setUniquePokemonSet(Set<Pokemon> uniquePokemonSet) {
        this.uniquePokemonSet = uniquePokemonSet;
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", password=" + password + ", email=" + email + ", fullname=" + fullname
                + ", gender=" + gender + ", myPokemonList=" + myPokemonList + ", uniquePokemonSet=" + uniquePokemonSet
                + ", lastCatchDate=" + lastCatchDate + ", rerollCounter=" + rerollCounter + ", lastRerollDate="
                + lastRerollDate + ", currentPokemon=" + currentPokemon + "]";
    }

    
}
