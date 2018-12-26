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

public class FileServer {

    private final ExecutorService exec = Executors.newCachedThreadPool();
    private int port = 9000;

    public void setPort(int p) {
        this.port = p;
    }

    public void start() throws IOException {
        ServerSocket socket = new ServerSocket(port);
        while (!exec.isShutdown()) {
            System.out.println("Server is waiting for client ...");
            Socket conn = socket.accept();
            exec.execute(new ClientRunnable(conn));
        }
        socket.close();
    }
 
    public void stop() {
        System.out.println("Shutting down server...");
        exec.shutdown();
    }

    public static void main(String[] args) throws IOException {
        FileServer fs = new FileServer();
        if(args.length > 0)
            fs.setPort(Integer.parseInt(args[0]));

        fs.start();
    }
}