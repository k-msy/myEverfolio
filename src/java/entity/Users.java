/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author bpg0129
 */
@Entity
@Table(name="FOLIO_USER")
public class Users implements Serializable {

    @Id
    private String id;
    private String pw;
    private String name;

    @OneToOne(cascade = {CascadeType.ALL})
    private UserGroup group;

    public Users() {

    }

    public Users(String id, String pw, String name, UserGroup group) {
        this.id = id;
        this.pw = pw;
        this.name = name;
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
    }
    
    
}
