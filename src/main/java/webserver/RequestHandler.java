package webserver;

import java.io.*;
import java.net.Socket;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import http.HttpRequest;
import http.HttpResponse;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.


            final HttpRequest httpRequest = new HttpRequest(in);
            final HttpResponse httpResponse = new HttpResponse(out);

            log.debug("HttpRequest : " + httpRequest);

            if (httpRequest.getPath().equals("/user/create")) {
                processCreateUser(httpRequest, httpResponse);
            } else if (httpRequest.getPath().equals("/user/login")) {
                processLogin(httpRequest, httpResponse);
            } else if (httpRequest.getPath().equals("/user/list")) {
                processUserList(httpRequest, httpResponse);
            } else {
                httpResponse.forward(httpRequest.getHeader("Accept"), httpRequest.getPath());
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processUserList(HttpRequest httpRequest, HttpResponse httpResponse) {
        String logined = httpRequest.getCookie("logined");
        if(logined.equals("true"))
            responseUserListPage(httpResponse);
        else
            httpResponse.sendRedirect("/user/login.html");
    }

    private void processLogin(HttpRequest httpRequest, HttpResponse httpResponse) {
        String userId = httpRequest.getParameter("userId");
        String password = httpRequest.getParameter("password");

        if (!userId.isEmpty() && !password.isEmpty() && login(userId, password)) {
            httpResponse.addCookie("logined", "true");
            httpResponse.sendRedirect("/index.html");
        }else {
            httpResponse.addCookie("logined", "false");
            httpResponse.sendRedirect("/user/login_failed.html");
        }
    }

    private void processCreateUser(HttpRequest httpRequest, HttpResponse httpResponse) {
        String userId = httpRequest.getParameter("userId");
        String password = httpRequest.getParameter("password");
        String name = httpRequest.getParameter("name");
        String email = httpRequest.getParameter("email");

        if (createUser(userId, password, name, email))
            httpResponse.sendRedirect("/index.html");
        else
            httpResponse.sendRedirect("/user/form.html");
    }

    private void responseUserListPage(HttpResponse httpResponse) {
        byte[] body = makeUserListHtml().getBytes();
        httpResponse.forward(body);
    }

    private String makeUserListHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<html> <head> </head> <body> <table border=\"1\"> <tr> <th>id</th> <th>이름</th> <th>이메일</th> </tr>");
        DataBase.findAll().stream()
                .forEach(user ->
                    html.append("<tr> <td>")
                            .append(user.getUserId())
                            .append("</td> <td>")
                            .append(user.getName())
                            .append("</td> <td>")
                            .append(user.getEmail())
                            .append("</td></tr>")
                );
        html.append("</table> </body> </html>");
        return html.toString();
    }

    private boolean createUser(String userId, String password, String name, String email) {
        if (!(userId.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty())) {
            User user = new User(userId, password, name, email);
            DataBase.addUser(user);
            log.debug(user.toString());
            return true;
        } else {
            return false;
        }
    }

    private boolean login(String id, String password) {
        return DataBase.findUserById(id)
                       .map(user -> user.getPassword().equals(password))
                       .orElseGet(() -> false);
    }
}

