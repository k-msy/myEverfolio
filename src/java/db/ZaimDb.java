package db;

import entity.Token_zaim;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import oauth.Ozaim;

@Stateless
public class ZaimDb {

    @PersistenceContext(unitName = "myEverfolioPU")
    private EntityManager em;
    @Inject
    Ozaim ozaim;

    public void update(HttpServletRequest request, HttpSession session) {
        Token_zaim token = (Token_zaim) em.find(Token_zaim.class, session.getAttribute("user_id").toString());
        token.setRequest_token(session.getAttribute("za_request_token").toString());
        token.setRequest_token_secret(session.getAttribute("za_request_token_secret").toString());
        token.setOauth_verifier(session.getAttribute("za_oauth_verifier").toString());
        token.setOauth_token(session.getAttribute("za_oauth_token").toString());
        token.setAccess_token(session.getAttribute("za_access_token").toString());
        token.setAccess_token_secret(session.getAttribute("za_access_token_secret").toString());
        em.merge(token);
    }

    public Token_zaim findObj(String userId) {
        return (Token_zaim) em.find(Token_zaim.class, userId);
    }

    public void coopZaim(HttpSession session) {
        try {
            ozaim.getRequestToken();
        } catch (IOException ex) {
            Logger.getLogger(ZaimDb.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void releaseCoopZaim(HttpSession session) {
        Token_zaim token = (Token_zaim) em.find(Token_zaim.class, session.getAttribute("user_id").toString());
        token.setRequest_token("");
        token.setRequest_token_secret("");
        token.setOauth_verifier("");
        token.setOauth_token("");
        token.setAccess_token("");
        token.setAccess_token_secret("");
        em.merge(token);
    }
}
