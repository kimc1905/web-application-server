package util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Moonchan on 2016. 7. 3..
 */

@ToString
public class HttpRequest {

    @Getter
    private HttpMethod method;
    private String endPoint;
    @Getter
    private String version;
    @Getter
    private Map<String, String> headers;
    @Getter
    private Optional<Map<String, String>> cookies;
    @Getter
    @Setter
    private Optional<String> body;

    public HttpRequest (String request) {
        int index = request.indexOf("\r\n");
        if(index > 0){
            String[] firstLine = request.substring(0, index).split(" ");
            method = HttpMethod.valueOf(firstLine[0]);
            endPoint = firstLine[1];
            version = firstLine[2];
            headers = HttpRequestUtils.parseHeaders(request.substring(index).trim());
            cookies = headers.get("Cookie") == null ?
                    Optional.empty() :
                    Optional.of(HttpRequestUtils.parseCookies(headers.get("Cookie")));

        }
        body = Optional.empty();
    }

    public String getEndPoint() {
        int index = endPoint.indexOf("?");
        if(index >= 0)
            return endPoint.substring(0, index);
        else
            return endPoint;
    }

    public String getEndPointWithParameter(){
        return endPoint;
    }

    public enum HttpMethod {
        GET("Get"), POST("Post"), DELETE("Delete"), PUT("Put");

        private String method;

        HttpMethod(String get) {
            this.method = method;
        }

        public String getValue(){
            return method;
        }

        public static HttpMethod getEnum(String value) {
            for(HttpMethod v : values())
                if(v.getValue().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException();
        }
    }
}


