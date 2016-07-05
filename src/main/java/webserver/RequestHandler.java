package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Map;

import com.google.common.collect.Maps;
import controller.Controller;
import controller.CreateUserController;
import controller.ListUserController;
import controller.LoginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import http.HttpRequest;
import http.HttpResponse;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private Map<String, Controller> controllerMap;

    public RequestHandler(Socket connectionSocket) {

        this.connection = connectionSocket;
        initController();
    }

    private void initController() {
        controllerMap = Maps.newHashMap();
        controllerMap.put("/user/create", new CreateUserController());
        controllerMap.put("/user/login", new LoginController());
        controllerMap.put("/user/list", new ListUserController());
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            final HttpRequest httpRequest = new HttpRequest(in);
            final HttpResponse httpResponse = new HttpResponse(out);

            startService(httpRequest.getPath(), httpRequest, httpResponse);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void startService(String path, HttpRequest request, HttpResponse response){
        Controller controller = controllerMap.get(path);
        if(controller != null)
            controller.service(request, response);
        else
            response.forward(request.getHeader("Accept"), path);
    }

}

