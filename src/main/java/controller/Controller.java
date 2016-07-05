package controller;

import http.HttpRequest;
import http.HttpResponse;

/**
 * Created by Moonchan on 2016. 7. 6..
 */
public interface Controller {
    void service(HttpRequest request, HttpResponse response);
}
