package bean;

import db.RegisterDb;
import entity.UserGroup;
import entity.Users;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import util.SHA256Encoder;

@Named
@RequestScoped
public class RegisterBb extends SuperBb implements Serializable {
    
    @NotNull
    private String id;
    @NotNull
    private String pw;
    @NotNull
    private String name;
    
    private UserGroup group;
    
    @EJB
    RegisterDb db;
    
    public String create(){
        group = new UserGroup(this.id, "user");
        Users user = new Users(this.id, getEncodedPw(this.pw), this.name, group);
        try{
          db.create(user); 
          return "/login.xhtml?faces-redirect=true";
        }catch(Exception e){
          super.facesErrorMsg("そのIDは既に利用されています");
        }
        return null;
    }
    
    public void clear(){
        id = pw = name = null;
    }
    
    private String getEncodedPw(String pw) {
        SHA256Encoder encoder = new SHA256Encoder();
        return encoder.encodePassword(pw);
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

    public RegisterDb getDb() {
        return db;
    }

    public void setDb(RegisterDb db) {
        this.db = db;
    }
    
    
}
