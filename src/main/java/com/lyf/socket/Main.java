package com.lyf.socket;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        final String FILE_PAHT = "F:\\test\\";
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket socket = serverSocket.accept();
            cachedThreadPool.execute(() -> {
                Socket s = null;
                FileOutputStream writer = null;
                try {
                    writer = new FileOutputStream(FILE_PAHT + new Date().getTime() + ".log");
                    InputStream inputStream = socket.getInputStream();

                    byte[] aa = SocketUtil.getHttpInput(inputStream);
                    writer.write(aa);
                    aa = SocketUtil.changePort(aa, "localhost:8080", "localhost:9090");

                    s = new Socket("localhost", 9090);
                    s.getOutputStream().write(aa);
                    s.getOutputStream().flush();

                    byte[] bb = SocketUtil.getHttpInput(s.getInputStream());
                    writer.write(bb);
                    socket.getOutputStream().write(bb);
                    socket.getOutputStream().flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        if (s != null) {
                            s.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


        }
    }
}
