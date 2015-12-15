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
public class Redirect extends HttpServlet {
/*
    public void doGet(HttpServletRequest request, HttpServletResponse response, String url, String token) throws IOException, ServletException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        //log("アクセスされました");

        String redirectUrl = url + "?oauth_token=" + token;
        System.out.println("redirectUrl：" + redirectUrl);
        response.sendRedirect(redirectUrl);
    }
    */
}
