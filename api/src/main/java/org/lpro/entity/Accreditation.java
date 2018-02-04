package org.lpro.entity;

public class Accreditation {
    private String username;
    private String password;

    public Accreditation() {
    }

    public Accreditation(String nomUtilisateur, String motDePasse) {
        this.username = nomUtilisateur;
        this.password = motDePasse;
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
    
    
}
