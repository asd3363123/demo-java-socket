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

    /**
     * 精细式读流
     */
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
            byte[] rest = new byte[len];
            len = is.read(rest);
            if (len > 0) {
                System.out.println("len AAAAAAAAAAAAAAAAAAAAA = " + len);
                bd.write(rest, 0, len);
            }
        } else if (httpHead.contains("Transfer-Encoding: chunked")) {
            //TODO f3
            int first = -1;
            while ((first = is.read()) > 0) {
                byte[] chunked = getChunked(is, first);
                bd.write(chunked);
            }
            if (first == 0) {
                bd.append((byte) 0);
            }
            //{"timestamp":"2019-05-19T08:05:33.998+0000","status":415,"error":"Unsupported Media Type","message":"Content type 'multipart/form-data;boundary=1c96d49d-f784-4985-8709-9aa62154f859;charset=UTF-8' not supported","path":"/alf/serverConfig/time"}
            //0
        }
        //=======================================================


        return bd.toByteArray();
    }

    private static byte[] getChunked(InputStream is, int first) throws IOException {
        StringBuilder size_16 = new StringBuilder((char) first);
        while ((first = is.read()) != '\n') {
            size_16.append((char) first);
        }
        String size_str = size_16.toString().replaceAll("\r", "");
        System.out.println(size_str);
        int size = Integer.parseInt(size_str, 16);

        byte[] chunked = new byte[size];
        int a = is.read(chunked);
        if (a < 1) {
            return new byte[0];
        }
        ByteArrayBuilder bd = new ByteArrayBuilder();
        bd.write(size_str.getBytes(StandardCharsets.UTF_8));
        bd.write('\r');
        bd.write('\n');
        bd.write(chunked, 0, a);
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

    /**
     * 粗放式读流
     */
    public static byte[] testGetHttpInput(InputStream is) throws IOException {
        ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
        int len = -1;
        while ((len = is.read()) > -1) {
            byteArrayBuilder.append(len);
            if (len == 0) {
                break;
            }
        }
        return byteArrayBuilder.toByteArray();
    }
}
