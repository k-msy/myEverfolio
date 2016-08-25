package entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "FOLIO_USER")
public class Users implements Serializable {

    @Id
    private String id;
    private String pw;
    private String name;
    @OneToOne(cascade = {javax.persistence.CascadeType.ALL})
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
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return this.pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserGroup getGroup() {
        return this.group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
    }
}
