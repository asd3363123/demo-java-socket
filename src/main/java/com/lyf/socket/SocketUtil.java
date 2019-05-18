package com.lyf.socket;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SocketUtil {

    public static final String DEMO_GET_HEAD = "GET / HTTP/1.1\n" +
            "Host: localhost:9090\n" +
            "Connection: keep-alive\n" +
            "Cache-Control: max-age=0\n" +
            "Upgrade-Insecure-Requests: 1\n" +
            "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36\n" +
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3\n" +
            "Accept-Encoding: gzip, deflate, br\n" +
            "Accept-Language: en,zh-CN;q=0.9,zh;q=0.8,ja;q=0.7\n" +
            "Cookie: _ga=GA1.1.358056180.1542283014\n";

    public static final String DEMO_RES_HEAD = "HTTP/1.1 200 \n" +
            "Content-Type: text/html;charset=UTF-8\n" +
            "Content-Length: 13\n" +
            "Date: Sat, 18 May 2019 10:00:56 GMT\n";

    public static byte[] getHttpInput(InputStream is) throws IOException {
        ByteArrayBuilder bd = new ByteArrayBuilder();
        int lastOne = -1, lastTwo = -1;

        //获取http头==========================================
        while (true) {
            int read = is.read();
            if (read == -1) {
                System.out.println("流已经关闭");
                break;
            }
            bd.append(read);
            if (read == 10 && lastOne == 13 && lastTwo == 10) {
                System.out.println("已经读取到结束符");
//                socket.close();
                break;
            }
            lastTwo = lastOne;
            lastOne = read;
        }
        String httpHead = new String(bd.toByteArray(), StandardCharsets.UTF_8);
        bd = new ByteArrayBuilder();
        bd.write(httpHead.getBytes(StandardCharsets.UTF_8));
        //=====================================================

        //获取body =============================================
        int len = getContentLength(httpHead);
        System.out.println("Content-Length: " + len);

        if (len > 0) {
            byte[] rest = new byte[len + 100];
            len = is.read(rest);
            if (len > 0) {
                System.out.println("len AAAAAAAAAAAAAAAAAAAAA = " + len);
                bd.write(rest, 0, len);
            }
        }
        //=======================================================


        return bd.toByteArray();
    }

    private static int getContentLength(String httpHead) {
        int index;
        if (httpHead != null && (index = httpHead.indexOf("Content-Length: ")) > -1) {
            int start = index + ("Content-Length: ".length());
            int end = httpHead.indexOf('\n', start);
            return Integer.parseInt(httpHead.substring(start, end).replaceAll("\r", ""));
        }
        return -1;
    }

    public static byte[] changePort(byte[] src, String oldHost, String newHost) {
        if (src == null || oldHost == null || newHost == null || oldHost.equals(newHost)) {
            return src;
        }
        String temp = new String(src, StandardCharsets.UTF_8);
        temp = temp.replaceAll("Host: " + oldHost, "Host: " + newHost);
        return temp.getBytes(StandardCharsets.UTF_8);
    }
}
