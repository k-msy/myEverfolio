package callback;

import static constants.Const_oauth.*;
import static constants.Const_withings.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.TransactionManagement;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.UtilLogic;

@TransactionManagement
public class WithingsCallback extends HttpServlet {

    @Inject
    UtilLogic utiLogic;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(true);
        session.setAttribute(WI_USERID, request.getParameter(OAUTH_USERID));
        session.setAttribute(WI_OAUTH_TOKEN, request.getParameter(OAUTH_TOKEN));
        session.setAttribute(WI_OAUTH_VERIFIER, request.getParameter(OAUTH_VERIFIER));
        try {
            response.sendRedirect(utiLogic.getAbsoluteContextPath(request) + "/faces/main/top.xhtml?faces-redirect=true");
        } catch (IOException ex) {
            Logger.getLogger(WithingsCallback.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
