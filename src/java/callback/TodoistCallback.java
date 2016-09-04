package callback;

import static constants.Const_todoist.*;
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
public class TodoistCallback extends HttpServlet {
    
    @Inject
    UtilLogic utiLogic;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        session.setAttribute(TODO_STATE, req.getParameter(TODO_STATE));
        session.setAttribute(TODO_CODE, req.getParameter(TODO_CODE));
        try {
            resp.sendRedirect(utiLogic.getAbsoluteContextPath(req) + "/faces/main/top.xhtml?faces-redirect=true");
        } catch (IOException ex) {
            System.out.println("todoist認証からのリダイレクト失敗");
        }
    }
}
