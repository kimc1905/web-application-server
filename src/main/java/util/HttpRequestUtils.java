package util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;

public class HttpRequestUtils {

    public static String getMethod(String header) {
        return header.substring(0, header.indexOf(" "));
    }

	public static String getEndpoint(String header){
		int start = header.indexOf(" ");
		if(start < 0)
			return "";
		return header.substring(start + 1, header.indexOf(" ", start + 1));
	}

	public static String getQueryString(String endPoint){
		String[] query = endPoint.split("\\?");
		if(query.length == 2)
			return endPoint.split("\\?")[1];
		return "";
	}

    public static Map<String, String> parseHeaders(String header) {
        String[] headersArray = header.split("\r\n");
        Map<String, String> headers = new HashMap<>();
        for (int i = 0; i < headersArray.length; i++) {
            headers.put(headersArray[i].split(": ")[0],
                    headersArray[i].split(": ")[1]);
        }

        return headers;
    }

	/**
	 * @param queryString URL에서 ? 이후에 전달되는 field1=value1&field2=value2 형식임
	 * @return
	 */
	public static Map<String, String> parseQueryString(String queryString) {
		return parseValues(queryString, "&");
	}

	/**
	 * @param cookies 쿠키 값은 name1=value1; name2=value2 형식임
	 * @return
	 */
	public static Map<String, String> parseCookies(String cookies) {
		return parseValues(cookies, ";");
	}

	private static Map<String, String> parseValues(String values, String separator) {
		if (Strings.isNullOrEmpty(values)) {
			return Maps.newHashMap();
		}

		String[] tokens = values.split(separator);
		return Arrays.stream(tokens)
					.map(t -> getKeyValue(t, "="))
					.filter(p -> p != null)
					.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
	}

	static Pair getKeyValue(String keyValue, String regex) {
		if (Strings.isNullOrEmpty(keyValue)) {
			return null;
		}

		String[] tokens = keyValue.split(regex);
		if (tokens.length != 2) {
			return null;
		}

		return new Pair(tokens[0], tokens[1]);
	}

	public static Pair parseHeader(String header) {

		return getKeyValue(header, ": ");
	}

    @Data
    public static class Pair {
		String key;
		String value;
		
		Pair(String key, String value) {
			this.key = key.trim();
			this.value = value.trim();
		}
	}
}
