package callback;

import static constants.Const_oauth.*;
import static constants.Const_zaim.*;
import java.io.IOException;
import javax.ejb.TransactionManagement;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.UtilLogic;

@TransactionManagement
public class ZaimCallback extends HttpServlet {

    @Inject
    UtilLogic utiLogic;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(true);
        session.setAttribute(ZA_OUTH_TOKEN, request.getParameter(OAUTH_TOKEN));
        session.setAttribute(ZA_OAUTH_VERIFIER, request.getParameter(OAUTH_VERIFIER));
        try {
            response.sendRedirect(utiLogic.getAbsoluteContextPath(request) + "/faces/main/top.xhtml?faces-redirect=true");
        } catch (IOException ex) {
            System.out.println("zaim認証からのリダイレクト失敗");
        }
    }
}
