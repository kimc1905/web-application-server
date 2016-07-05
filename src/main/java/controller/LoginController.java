package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;

/**
 * Created by Moonchan on 2016. 7. 6..
 */
public class LoginController extends AbstractController {

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        if (!userId.isEmpty() && !password.isEmpty() && isLogin(userId, password)) {
            response.addCookie("logined", "true");
            response.sendRedirect("/index.html");
        }else {
            response.addCookie("logined", "false");
            response.sendRedirect("/user/login_failed.html");
        }
    }

    public boolean isLogin(String id, String password) {
        return DataBase.findUserById(id)
                .map(user -> user.getPassword().equals(password))
                .orElseGet(() -> false);
    }
}
