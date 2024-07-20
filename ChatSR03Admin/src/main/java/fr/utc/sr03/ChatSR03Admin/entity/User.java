package fr.utc.sr03.ChatSR03Admin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jdk.jfr.BooleanFlag;

@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int idUser;

    @Column(name = "isAdmin")
    @BooleanFlag
    private boolean isAdmin;
    @Column(name = "familyName")
    @Size(min = 2)
    @NotEmpty(message = "lastName obligatoire")
    private String lastName;

    @Column(name = "firstName")
    @Size(min = 2)
    @NotEmpty(message = "firstName obligatoire")
    private String firstName;

    @Column(name = "email")
    @Email
    @NotEmpty(message = "email obligatoire")
    private String email;

    @Column(name = "password")
    @Size(min = 5)
    @NotEmpty(message = "password obligatoire")
    private String password;

    @Column(name = "isActivated")
    @BooleanFlag
    private boolean isActivated;

    @Column(name = "loginAttempts")
    private int loginAttempts = 0;


    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }
}
