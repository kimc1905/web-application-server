package http;

import com.google.common.collect.Maps;
import lombok.ToString;
import util.HttpRequestUtils;
import util.IOUtils;

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

    private RequestLine requestLine;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private Map<String, String> cookies;
    private Optional<String> body;

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String requestLineStr = br.readLine();
            String headerStr = IOUtils.readHeader(br);

            requestLine = new RequestLine(requestLineStr);
            headers = HttpRequestUtils.parseHeaders(headerStr.trim());
            cookies = HttpRequestUtils.parseCookies(headers.get("Cookie"));
            body = readBody(br);
            parameters = parseParameter(requestLine.getPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> parseParameter(String path) {
        switch (requestLine.getMethod()) {
            case GET:
                return parseParametersAsGetMethod(path);
            case POST:
                return parseParametersAsPostMethod();
        }
        return Maps.newHashMap();
    }

    private Map<String, String> parseParametersAsGetMethod(String path) {
        String[] query = path.split("\\?");
        if (query.length == 2) {
            requestLine.setPath(query[0]);
            return HttpRequestUtils.parseQueryString(query[1]);
        }
        return Maps.newHashMap();
    }

    private Map<String, String> parseParametersAsPostMethod() {
        if(body.isPresent())
            return HttpRequestUtils.parseQueryString(body.get());
        return Maps.newHashMap();
    }

    private Optional<String> readBody(BufferedReader br) {
        String length = getHeader("Content-Length");
        if (!length.isEmpty())
            try {
                return Optional.of(IOUtils.readData(br, Integer.parseInt(length.trim())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        return Optional.empty();
    }

    public String getPath(){
        return requestLine.getPath();
    }

    public HttpMethod getMethod(){
        return requestLine.getMethod();
    }

    public String getProtocol(){
        return requestLine.getProtocol();
    }

    public String getHeader(String key) {
        if (headers == null)
            return "";
        return headers.getOrDefault(key, "");
    }

    public String getParameter(String key) {
        if (parameters == null)
            return "";
        return parameters.getOrDefault(key, "");

    }

    public String getCookie(String key) {
        if (cookies == null)
            return "";
        return cookies.getOrDefault(key, "");
    }
}


