package util;

import lombok.Getter;
import lombok.ToString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Moonchan on 2016. 7. 3..
 */

@ToString
public class HttpRequest {

    @Getter
    private HttpMethod method;
    @Getter
    private String path;
    @Getter
    private String protocol;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private Map<String, String> cookies;
    private String body;

    public HttpRequest (InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String request = IOUtils.readHeader(br);

            int index = request.indexOf("\r\n");
            if (index > 0) {
                String[] requestLine = request.substring(0, index).split(" ");
                method = HttpMethod.valueOf(requestLine[0]);
                path = requestLine[1];
                protocol = requestLine[2];
                headers = HttpRequestUtils.parseHeaders(request.substring(index).trim());
                cookies = HttpRequestUtils.parseCookies(headers.get("Cookie"));
            }
            parseBody(br);

            switch(method){
                case GET:
                    parseParametersFromGetMethod();
                    break;
                case POST:
                    parseParametersFromPostMethod();
                    break;
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void parseBody(BufferedReader br) {
        String length = getHeader("Content-Length");
        if (!length.isEmpty())
            try {
                body = IOUtils.readData(br, Integer.parseInt(length.trim()));
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void parseParametersFromGetMethod() {
        String[] query = path.split("\\?");
        if(query.length == 2){
            path = query[0];
            parameters = HttpRequestUtils.parseQueryString(query[1]);
        }
    }

    private void parseParametersFromPostMethod() {
        if(!body.isEmpty())
            parameters = HttpRequestUtils.parseQueryString(body);
    }

    public String getHeader(String key) {
        if(headers == null)
            return "";
        return headers.getOrDefault(key, "");
    }

    public String getParameter(String key){
        if(parameters == null)
            return "";
        return parameters.getOrDefault(key, "");

    }

    public String getCookie(String key){
        if(cookies == null)
            return "";
        return cookies.getOrDefault(key, "");
    }
}


