package controller;

import http.HttpRequest;
import http.HttpResponse;

/**
 * Created by Moonchan on 2016. 7. 6..
 */
public abstract class AbstractController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        switch (request.getMethod()) {
            case GET:
                doGet(request, response);
                break;
            case POST:
                doPost(request, response);
        }
    }

    public void doPost(HttpRequest request, HttpResponse response){}

    public void doGet(HttpRequest request, HttpResponse response){}
}
