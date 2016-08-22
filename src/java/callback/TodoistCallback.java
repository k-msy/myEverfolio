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
public class TodoistCallback
  extends HttpServlet
{
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    HttpSession session = req.getSession(true);
    session.setAttribute("state", req.getParameter("state"));
    session.setAttribute("code", req.getParameter("code"));
    try
    {
      resp.sendRedirect("http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true");
    }
    catch (IOException ex)
    {
      System.out.println("todoist���������������������������������������");
    }
  }
}
