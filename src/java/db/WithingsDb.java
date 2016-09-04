package db;

import entity.Token_withings;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import oauth.Owithings;

@Stateless
public class WithingsDb {

    @PersistenceContext(unitName = "myEverfolioPU")
    private EntityManager em;
    @Inject
    Owithings owi;

    public Token_withings findObj(String userId) {
        return (Token_withings) em.find(Token_withings.class, userId);
    }

    public void update(HttpServletRequest request, HttpSession session) {
        Token_withings token = (Token_withings) em.find(Token_withings.class, session.getAttribute("user_id").toString());
        token.setAccess_token(session.getAttribute("wi_access_token").toString());
        token.setAccess_token_secret(session.getAttribute("wi_access_token_secret").toString());
        token.setOauth_token(session.getAttribute("wi_oauth_token").toString());
        token.setOauth_verifier(session.getAttribute("wi_oauth_verifier").toString());
        token.setRequest_token(session.getAttribute("wi_request_token").toString());
        token.setRequest_token_secret(session.getAttribute("wi_request_token_secret").toString());
        token.setWi_userId(session.getAttribute("wi_userId").toString());
    }

    public void releaseCoopWithings(HttpSession session) {
        Token_withings token = (Token_withings) em.find(Token_withings.class, session.getAttribute("user_id").toString());
        token.setAccess_token("");
        token.setAccess_token_secret("");
        token.setOauth_token("");
        token.setOauth_verifier("");
        token.setRequest_token("");
        token.setRequest_token_secret("");
        token.setWi_userId("");
        em.merge(token);
    }

    public void coopWithings(HttpSession session) {
        this.owi.getRequestToken();
    }
}
