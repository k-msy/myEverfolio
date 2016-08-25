package db;

import entity.Todoist_karma;
import entity.Token_todoist;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import oauth.Otodoist;

@Stateless
public class TodoistDb {

    @PersistenceContext(unitName = "myEverfolioPU")
    private EntityManager em;
    @Inject
    Otodoist oto;

    public Token_todoist findObj(String userId) {
        return (Token_todoist) this.em.find(Token_todoist.class, userId);
    }

    public void coopTodoist(HttpSession session) {
        this.oto.getRequestToken();
    }

    public void releaseCoopTodoist(HttpSession session) {
        Token_todoist token = (Token_todoist) this.em.find(Token_todoist.class, session.getAttribute("user_id").toString());
        token.setAccess_token("");
        token.setTodo_code("");
        token.setTodo_state("");
        token.setToken_type("");
        this.em.merge(token);
    }

    public void update(HttpServletRequest request, HttpSession session) {
        Token_todoist token = (Token_todoist) this.em.find(Token_todoist.class, session.getAttribute("user_id").toString());
        token.setAccess_token(session.getAttribute("access_token").toString());
        token.setTodo_code(session.getAttribute("code").toString());
        token.setTodo_state(session.getAttribute("state").toString());
        token.setToken_type(session.getAttribute("token_type").toString());
        this.em.merge(token);
    }

    public void insertKarmaDb(HttpServletRequest request, HttpSession session) {
        String id = session.getAttribute("user_id").toString();
        Todoist_karma todokarma = (Todoist_karma) this.em.find(Todoist_karma.class, id);
        if (todokarma == null) {
            todokarma = new Todoist_karma();
            todokarma.setUserid(id);
            todokarma.setKarma("");
            this.em.persist(todokarma);
        }
    }

    public void updateKarma(String userId, String diff) {
        Todoist_karma todokarma = (Todoist_karma) this.em.find(Todoist_karma.class, userId);
        todokarma.setKarma(diff);
        this.em.persist(todokarma);
    }
}
