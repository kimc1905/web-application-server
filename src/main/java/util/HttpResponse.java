package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static util.LambdaExceptionUtil.rethrowConsumer;

/**
 * Created by Moonchan on 2016. 7. 4..
 */
public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private DataOutputStream dataOutputStream;

    public HttpResponse(OutputStream out){
        dataOutputStream = new DataOutputStream(out);
        headers = new HashMap<>();
        cookies = new HashMap<>();
    }
    
    public void addHeader(String key, String value){
        headers.put(key, value);
    }

    public void addCookie(String key, String value) {
        cookies.put(key, value);
    }

    public String getHeader(String key) {
        if(headers == null)
            return "";
        return headers.getOrDefault(key, "");
    }

    /**
     * 정적페이지 포워딩 (text/html)
     * @param path
     */
    public void forward(String path) {
        forward("text/html", path);
    }

    /**
     * 정적페이지 포워딩
     * @param contentType
     * @param path
     */
    public void forward(String contentType, String path) {
        try {
            forward(contentType, Files.readAllBytes(Paths.get("webapp" + path)));
        }catch(IOException e){
            log.debug("Forward : " + e.getMessage());
        }
    }

    /**
     * 동적페이지 포워딩 (text/html)
     * @param body
     */
    public void forward(byte[] body) {
        forward("text/html", body);
    }

    /**
     * 동적페이지 포워딩
     * @param contentType
     * @param body
     */
    public void forward(String contentType, byte[] body) {
        try {
            writeStatusLine("200", "OK");
            create200Header(dataOutputStream, contentType, body.length);
            response(Optional.of(body));
        }catch(IOException e){
            log.debug("Forward : " + e.getMessage());
        }
    }

    public void sendRedirect(String path) {
        try {
            writeStatusLine("302", "Found");
            create302Header(path);
            response(Optional.empty());
        }catch(IOException e){
            log.debug("Redirect : " + e.getMessage());
        }
    }

    private void response(Optional<byte[]> body) throws IOException{
        String responseHeader = headers.entrySet()
                                       .stream()
                                       .map(v -> v.getKey() + ": " + v.getValue() + "\r\n")
                                       .collect(Collectors.joining("", "", "\r\n"));
        dataOutputStream.writeBytes(responseHeader);
        body.ifPresent(rethrowConsumer(v -> dataOutputStream.write(v, 0, v.length)));
        dataOutputStream.writeBytes("\r\n");
        dataOutputStream.flush();
    }

    private void writeStatusLine(String code, String message) throws IOException{
        dataOutputStream.writeBytes(String.format("HTTP/1.1 %s %s\r\n", code, message));
    }

    private void create200Header(DataOutputStream dos, String contentType, int lengthOfBodyContent) {
        addHeader("Content-Type", contentType + ";charset=utf-8");
        addHeader("Content-Length", String.valueOf(lengthOfBodyContent));
        addHeaderFromCookie();
    }

    private void create302Header(String path) {
        addHeader("Location", "http://localhost:8080" + path);
        addHeaderFromCookie();
    }

    private void addHeaderFromCookie() {
        if(!getCookieString().isEmpty())
            addHeader("Set-Cookie", getCookieString());
    }

    private String getCookieString(){
        if(cookies.isEmpty())
            return "";
        return cookies.entrySet()
                .stream()
                .map(v -> v.getKey() + "=" + v.getValue())
                .collect(Collectors.joining(";"));
    }
}
