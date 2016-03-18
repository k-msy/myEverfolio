/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package callback;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author bpg0129
 */
public class Withings extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HttpSession session = request.getSession(true);
        System.out.println("帰ってきたウルトラマン！？");
        session.setAttribute("userid", request.getParameter("userid"));
        session.setAttribute("oauth_token", request.getParameter("oauth_token"));
        session.setAttribute("oauth_verifier", request.getParameter("oauth_verifier"));

        try {
            response.sendRedirect("http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true");
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}