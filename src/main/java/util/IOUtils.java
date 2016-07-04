package util;

import java.io.BufferedReader;
import java.io.IOException;

public class IOUtils {
	/**
	 * @param br 는 Request Body를 시작하는 시점이어야
	 * @param contentLength 는 Request Header의 Content-Length 값이다.
	 * @return
	 * @throws IOException
	 */
	public static String readData(BufferedReader br, int contentLength) throws IOException {
		char[] body = new char[contentLength];
		br.read(body, 0, contentLength);
		return String.copyValueOf(body);
	}

	public static String readHeader(BufferedReader br) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = br.readLine()) != null) {
			sb.append(line + "\r\n");
			if (line.isEmpty()) {
				break;
			}
		}

		return sb.toString();
	}
}
