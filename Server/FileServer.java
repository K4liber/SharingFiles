import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.ClientRunnable;

// compile: javac FileServer.java

public class FileServer {

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private int port = 9000;

    public void setPort(int p) {
        this.port = p;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(this.port);
        System.out.println("Socket server started on port " + this.port + " successfully.");
        while (!executorService.isShutdown()) {
            Socket socket = serverSocket.accept();
            executorService.execute(new ClientRunnable(socket));
        }
        serverSocket.close();
    }
 
    public void stop() {
        System.out.println("Shutting down server...");
        executorService.shutdown();
    }

    public static void main(String[] args) throws IOException {
        FileServer fileServer = new FileServer();
        if(args.length > 0)
            fileServer.setPort(Integer.parseInt(args[0]));

        fileServer.start();
    }
}