package client;

import adds.FileContent;

import java.net.Socket;
import java.nio.file.Files;
import java.io.*; 
import java.nio.file.Path;
import java.nio.file.Paths;

// compile: javac -d . SaveFileRunnable.java

public class SaveFileRunnable implements Runnable{

    private Socket socket;
    private String fileName;
    private String directory;

    public SaveFileRunnable(Socket socket, String fileName, String directory) {
        this.socket = socket;
        this.fileName = fileName;
        this.directory = directory;
    }

    public void run() {
        try {
            System.out.println("Asking for file: " + fileName);
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(fileName + "\n");
            bw.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            byte[] content = (byte[]) ois.readObject();

            if (content == null)
                return;

            FileContent fileContent = (FileContent) convertFromBytes(content);
            String targetFileName = directory + fileContent.getFileName();
            System.out.println("File will be saved to: " + targetFileName);
            Path path = Paths.get(targetFileName);
            Files.write(path, fileContent.getFileBytes());
            this.socket.close();
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        } catch (ClassNotFoundException classNotFoundException) {
            System.out.println(classNotFoundException.getMessage());
        }
    }

    private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } 
    }

}