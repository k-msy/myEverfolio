package callback;

import java.io.IOException;
import java.io.PrintStream;
import javax.ejb.TransactionManagement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@TransactionManagement
public class ZaimCallback
  extends HttpServlet
{
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
  {
    HttpSession session = request.getSession(true);
    session.setAttribute("za_oauth_token", request.getParameter("oauth_token"));
    session.setAttribute("za_oauth_verifier", request.getParameter("oauth_verifier"));
    try
    {
      response.sendRedirect("http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true");
    }
    catch (IOException ex)
    {
      System.out.println("zaim���������������������������������������");
    }
  }
}
