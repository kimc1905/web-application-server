package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;

/**
 * Created by Moonchan on 2016. 7. 6..
 */
public class ListUserController extends AbstractController {



    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        String logined = request.getCookie("logined");
        if(logined.equals("true"))
            responseUserListPage(response);
        else
            response.sendRedirect("/user/login.html");
    }

    private void responseUserListPage(HttpResponse httpResponse) {
        byte[] body = makeUserListHtml().getBytes();
        httpResponse.forwardBody(body);
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
}
