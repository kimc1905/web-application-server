package http;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Moonchan on 2016. 7. 6..
 */

@ToString
public class RequestLine {
    @Getter
    private HttpMethod method;
    @Getter @Setter
    private String path;
    @Getter
    private String protocol;

    public RequestLine(String requestLineStr){
        String[] requestLine = requestLineStr.split(" ");
        method = HttpMethod.valueOf(requestLine[0]);
        path = requestLine[1];
        protocol = requestLine[2];
    }


}
