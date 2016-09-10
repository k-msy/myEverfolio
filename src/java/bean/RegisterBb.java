package bean;

import db.RegisterDb;
import entity.Todoist_karma;
import entity.Token_todoist;
import entity.Token_withings;
import entity.Token_zaim;
import entity.UserGroup;
import entity.Users;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
    @Inject
    SHA256Encoder encorder;

    /**
     * アカウント登録
     * @return 
     */
    public String create() {
        group = new UserGroup(id, "user");
        Users user = new Users(id, encorder.encodePassword(pw), name, group);
        Token_withings wi_token = new Token_withings(id, "", "", "", "", "", "", "");
        Token_zaim za_token = new Token_zaim(id, "", "", "", "", "", "");
        Token_todoist to_token = new Token_todoist(id, "", "", "", "");
        Todoist_karma karma = new Todoist_karma(id, "");
        try {
            db.create(user, wi_token, za_token, to_token, karma);
            return "/login.xhtml?faces-redirect=true";
        } catch (Exception e) {
            super.facesErrorMsg("IDは既に使用されています。");
        }
        return null;
    }

    /**
     * 入力値のクリア
     */
    public void clear() {
        id = (pw = name = null);
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
