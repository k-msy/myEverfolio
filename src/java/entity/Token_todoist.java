package entity;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@RequestScoped
@Entity
@Table(name = "TOKEN_TODOIST")
public class Token_todoist
        implements Serializable {

    @Id
    @NotNull
    private String userId;
    private String todo_code;
    private String todo_state;
    private String access_token;
    private String token_type;

    public Token_todoist() {
    }

    public Token_todoist(String userId, String todo_code, String todo_state, String access_token, String token_type) {
        this.userId = userId;
        this.todo_code = todo_code;
        this.todo_state = todo_state;
        this.access_token = access_token;
        this.token_type = token_type;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTodo_code() {
        return this.todo_code;
    }

    public void setTodo_code(String todo_code) {
        this.todo_code = todo_code;
    }

    public String getTodo_state() {
        return this.todo_state;
    }

    public void setTodo_state(String todo_state) {
        this.todo_state = todo_state;
    }

    public String getAccess_token() {
        return this.access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return this.token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }
}
