/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bpg0129
 */
public class RedirectReceiver extends HttpServlet {
    
        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {


        String param = request.getParameter("oauth_verifier");
        System.out.println("oauth_verifier : " + param);

    }
    
}
