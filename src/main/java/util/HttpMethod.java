package util;

/**
 * Created by Moonchan on 2016. 7. 4..
 */
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