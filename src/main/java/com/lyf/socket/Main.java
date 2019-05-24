package com.lyf.socket;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    /**
     * 日志文件目录
     */
    public static final String FILE_PATH = "D:\\test\\";

    public static final String OLD_HOST = "192.168.100.79";

    public static final String NEW_HOST = "192.168.100.79";

    public static final int OLD_PORT = 9090;

    public static final int NEW_PORT = 8080;

    public static void main(String[] args) throws Exception {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(OLD_PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            cachedThreadPool.execute(() -> {
                Socket s = null;
                FileOutputStream writer = null;
                try {
                    writer = new FileOutputStream(FILE_PATH + System.currentTimeMillis() + ".log");
                    InputStream inputStream = socket.getInputStream();

                    byte[] aa = SocketUtil.testGetHttpInput(inputStream);
                    writer.write(aa);
                    aa = SocketUtil.changePort(aa, OLD_HOST + ":" + OLD_PORT, NEW_HOST + ":" + NEW_PORT);

                    s = new Socket(NEW_HOST, NEW_PORT);
                    s.getOutputStream().write(aa);
                    s.getOutputStream().flush();

                    byte[] bb = SocketUtil.testGetHttpInput(s.getInputStream());
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
