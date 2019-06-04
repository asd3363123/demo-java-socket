package com.lyf.socket;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 只记录
 */
public class RecordSocket {
    /**
     * 日志文件目录
     */
    public static final String FILE_PATH = "D:\\log\\";

    public static final int PORT = 9999;

    public static void main(String[] args) throws Exception {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            cachedThreadPool.execute(() -> {
                FileOutputStream writer = null;
                try {
                    writer = new FileOutputStream(FILE_PATH + "record_" + System.currentTimeMillis() + ".log");
                    InputStream inputStream = socket.getInputStream();

                    byte[] aa = SocketUtil.testGetHttpInput(inputStream);
                    writer.write(aa);

                    socket.getOutputStream().write('\0');
                    socket.getOutputStream().flush();
                    socket.getOutputStream().close();
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
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


        }
    }
}
