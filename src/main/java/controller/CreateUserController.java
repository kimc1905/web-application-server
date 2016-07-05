package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

/**
 * Created by Moonchan on 2016. 7. 6..
 */
public class CreateUserController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        if (createUser(userId, password, name, email))
            response.sendRedirect("/index.html");
        else
            response.sendRedirect("/user/form.html");
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
}
