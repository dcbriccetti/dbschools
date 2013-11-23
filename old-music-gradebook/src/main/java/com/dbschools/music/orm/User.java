package com.dbschools.music.orm;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name="music_user") public class User implements Serializable {
    private static final long serialVersionUID = -7891036118125166320L;

    @Id     
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    
    @Column(length=20,nullable=false) private String login;
    @Column(length=30,nullable=false) private String password;
    @Column(name="first_name", length=40,nullable=false) private String firstName;
    @Column(name="last_name", length=40,nullable=false) private String lastName;
    
    public User() {
        super();
    }
    
    public User(String login, String password, String firstName, String lastName) {
        super();
        this.login = login;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Object getDisplayName() {
        return lastName + ", " + firstName;
    }
    
}
