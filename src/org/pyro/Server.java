package org.pyro;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author ZYC MoronSlayer@outlook.com
 * @version 1.0
 * @date 2021/4/19 9:29
 */
public class Server {
    
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server Startup");
        for (;;) {
            Socket socket = serverSocket.accept();
            System.out.println("Connected from: " + socket.getRemoteSocketAddress());
            Thread subThread = new Handler(socket);
            subThread.start();
        }
    }
    
    private static class Handler extends Thread {
        
        Socket threadSocket;
    
        public Handler(Socket socket) {
            this.threadSocket = socket;
        }
    
        @Override
        public void run() {
            try (InputStream input = threadSocket.getInputStream()) {
                try(OutputStream output = threadSocket.getOutputStream()) {
                    this.handle(input, output);
                }
            } catch (Exception e) {
                try {
                    this.threadSocket.close();
                } catch (IOException ex) {
                    System.out.println("Client disconnected");
                }
            }
        }
    
        private void handle(InputStream input, OutputStream output) throws IOException {
            System.out.println("Process new http request...");
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        
            // 读取HTTP请求
            boolean requestOk = false;
            String first = reader.readLine();
            if (first.startsWith("GET / HTTP/1.")) {
                requestOk = true;
            }
            for (;;) {
                String header = reader.readLine();
                // 读取到空行时, HTTP Header读取完毕
                if (header.isEmpty()) {
                    break;
                }
                System.out.println(header);
            }
            System.out.println(requestOk ? "Response OK" : "Response Error");
            if (!requestOk) {
                // 发送错误响应
                writer.write("HTTP/1.0 404 Not Found\r\n");
                writer.write("Content-Length: 0\r\n");
                writer.write("\r\n");
                writer.flush();
            } else {
                // 发送成功响应
                String data = "<html><body><h1>Hello, world!</h1></body></html>";
                int length = data.getBytes(StandardCharsets.UTF_8).length;
                writer.write("HTTP/1.0 200 OK\r\n");
                writer.write("Connection: close\r\n");
                writer.write("Content-Type: text/html\r\n");
                writer.write("Content-Length: " + length + "\r\n");
                // 空行标识Header和Body的分隔
                writer.write("\r\n");
                writer.write(data);
                writer.flush();
            }
        }
        
    }
    
}
