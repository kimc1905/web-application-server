package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.internal.ws.transport.http.HttpMetadataPublisher;
import db.DataBase;
import model.User;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.IOUtils;

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
            DataOutputStream dos = new DataOutputStream(out);

            final HttpRequest httpRequest = parseHttpRequest(in);
            final String endPoint = httpRequest.getEndPoint();
            final Optional<Map<String, String>> parameters = getParameters(httpRequest);

            log.debug("HttpRequest : " + httpRequest);

            if(endPoint.equals("/user/create")){
                processCreateUser(dos, parameters);
            }else if(endPoint.equals("/user/login")){
                processLogin(dos, parameters);
            }else if(endPoint.equals("/user/list")){
                processUserList(dos, httpRequest);
            }else {
                responseWebPage(dos, httpRequest.getHeaders().get("Accept"), endPoint);
            }

        } catch (IOException e) {
			log.error(e.getMessage());
		}
	}

    private void processUserList(DataOutputStream dos, HttpRequest httpRequest) {
        if(!httpRequest.getCookies().isPresent())
            return;
        String logined = httpRequest.getCookies().get().get("logined");
        if("true".equals(logined)){
            responseUserListPage(dos);
        }else{
            response302Header(dos, "/user/login.html", Optional.empty());
        }

    }

    private HttpRequest parseHttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String header = IOUtils.readHeader(br);
//        log.debug(header);
        HttpRequest httpRequest = new HttpRequest(header);
        String length = httpRequest.getHeaders().get("Content-Length");
        if(length != null){
            String body = IOUtils.readData(br, Integer.parseInt(length.trim()));
            httpRequest.setBody(Optional.of(body));
        }
        return httpRequest;
    }

    private Optional<Map<String,String>> getParameters(HttpRequest request) {
        switch (request.getMethod()){
            case GET:
                return getParametersFromGetMethod(request.getEndPointWithParameter());
            case POST:
                if(request.getBody().isPresent())
                    return getParametersFromPostMethod(request.getBody().get());
        }
        return Optional.empty();
    }

    private void processLogin(DataOutputStream dos, Optional<Map<String, String>> parameters) {
        parameters.ifPresent(params -> {
            if(login(params.get("userId"), params.get("password"))){
                redirectWebPage(dos, "/index.html", Optional.of("logined=true"));
            }else{
                redirectWebPage(dos, "/user/login_failed.html", Optional.of("logined=false"));
            }
        });
    }


    private void processCreateUser(DataOutputStream dos, Optional<Map<String, String>> parameters) {
        if(createUser(parameters))
            redirectWebPage(dos, "/index.html");
        else
            redirectWebPage(dos, "/user/form.html");
    }

    private void responseWebPage(DataOutputStream dos, String contentType, String endPoint) throws IOException {
        byte[] body = Files.readAllBytes(Paths.get("webapp" + endPoint));

        response200Header(dos, contentType, body.length);
        responseBody(dos, body);
    }

    private void responseUserListPage(DataOutputStream dos) {
        byte[] body = makeUserListHtml().getBytes();
        response200Header(dos, "text/html", body.length);
        responseBody(dos, body);
    }

    private String makeUserListHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<html> <head> </head> <body> <table border=\"1\"> <tr> <th>id</th> <th>이름</th> <th>이메일</th> </tr>");
        DataBase.findAll().stream()
                .forEach(user -> html.append("<tr> <td>" + user.getUserId() + "</td> <td>" + user.getName() +
                        "</td> <td>" + user.getEmail() + "</td></tr>"));
        html.append("</table> </body> </html>");
        return html.toString();
    }

    private void redirectWebPage(DataOutputStream dos, String location) {
        response302Header(dos, location, Optional.empty());
    }

    private void redirectWebPage(DataOutputStream dos, String location, Optional<String> cookies) {
        response302Header(dos, location, cookies);
    }

    private boolean createUser(Optional<Map<String, String>> parameters) {
        String userId = parameters.get().get("userId");
        String password = parameters.get().get("password");
        String name = parameters.get().get("name");
        String email = parameters.get().get("email");

        if(! (userId.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty() )){
            User user = new User(userId, password, name, email);
            DataBase.addUser(user);
            log.debug(user.toString());
            return true;
        }else {
            return false;
        }
    }

    private boolean login(String id, String password){
        if(id == null || password == null)
            return false;
        User user = DataBase.findUserById(id);
        if(user == null)
            return false;
        return user.getPassword().equals(password);
    }

    private Optional<Map<String, String>> getParametersFromGetMethod(String endPoint) {
        String queryString = HttpRequestUtils.getQueryString(endPoint);
        if(queryString.isEmpty())
            return Optional.of(HttpRequestUtils.parseQueryString(queryString));
        return Optional.empty();
    }

    private Optional<Map<String, String>> getParametersFromPostMethod(String body) {
        if(!body.isEmpty())
            return Optional.of(HttpRequestUtils.parseQueryString(body));
        return Optional.empty();
    }

    private void response200Header(DataOutputStream dos, String contentType, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

    private void response302Header(DataOutputStream dos, String location, Optional<String> cookies) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: http://localhost:8080" + location + "\r\n");
            if (cookies.isPresent()) {
                log.debug("Set Cookie!" + cookies.get());
                dos.writeBytes("Set-Cookie: " + cookies.get() + "\r\n");
            }
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}

