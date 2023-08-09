/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cloudserver;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author ntu-user
 */
public class User {
    private SimpleStringProperty firstname;
    private SimpleStringProperty lastname;
    private SimpleStringProperty username;
    private SimpleStringProperty password;
    private SimpleStringProperty email;
    private Integer isAdmin;
    private Integer isLoggedIn;
    
    /**
     * 
     * @param username
     * @param password
     * @param firstname
     * @param lastname
     * @param email
     * @param isLoggedIn
     * @param isAdmin 
     */
    User(String username, String password, String firstname, String lastname, String email, Integer isLoggedIn, Integer isAdmin) {
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.firstname = new SimpleStringProperty(firstname);
        this.lastname = new SimpleStringProperty(lastname);
        this.email = new SimpleStringProperty(email);
        this.isAdmin = isAdmin;
        this.isLoggedIn = isLoggedIn;
        
    }
    
    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }
    
    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }
    
    public String getFirstName() {
        return firstname.get();
    }

    public void setFirstName(String firstname) {
        this.firstname.set(firstname);
    }
    
    public String getLastname() {
        return lastname.get();
    }

    public void setLastname(String firstname) {
        this.lastname.set(firstname);
    }
    
    public void setIsLoggedIn(Integer value) {
        this.isLoggedIn = value;
    }
    
        
    public Integer getIsLoggedIn() {
        return isLoggedIn;
    }
    
    public void setIsAdmin(Integer value) {
        this.isAdmin = value;
    }
    
    public Integer getIsAdmin() {
        return isAdmin;
    }
}
