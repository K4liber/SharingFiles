package server;

import java.io.*; 
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import adds.FileContent;

// compile: javac -d . ClientRunnable.java

public class ClientRunnable implements Runnable {

    private Socket socket = null;

    public ClientRunnable(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream is = this.socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String message = br.readLine();

            if (message != null && message.equals("upload")) {
                ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());
                uploadingFile(ois);
            } else if (message != null && message.equals("list")) {
                sendFileNames();
            } else {
                sendFileContent(message);
            }

        } catch (IOException e) {
            System.out.println("IO Exception");
            e.printStackTrace();
        }
    }

    private void uploadingFile(ObjectInputStream ois) {
        try {
            Object object = ois.readObject();

            if (object == null)
                return;

            File file = (File) object;
            printInfo(
                Thread.currentThread().getId(), 
                "Saving file: " + file.getName(), 
                socket.getRemoteSocketAddress().toString()
            );
            byte[] content = Files.readAllBytes(file.toPath());
            String filePath = System.getProperty("user.dir") + "/Files/" + file.getName();
            FileOutputStream out = new FileOutputStream(filePath);
            out.write(content);
            out.close();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException");
            e.printStackTrace();
        }
    }

    private byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        } 
    }

    private void sendFileContent(String fileName) {
        String filePath = System.getProperty("user.dir") + "/Files/" + fileName;
        File file = new File(filePath);

        if (!file.exists()) {
            return;
        }

        printInfo(
            Thread.currentThread().getId(), 
            "Sending file: " + file.getAbsolutePath(), 
            this.socket.getRemoteSocketAddress().toString()
        );

        try {
            byte[] content = convertToBytes(new FileContent(file, fileName));
            ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());
            oos.writeObject(content);
            oos.flush();
        } catch (IOException e) {
            System.out.println("IOException Error");
            e.printStackTrace();
        }
    }
   
    private void sendFileNames() {
        String filesFolderPath = System.getProperty("user.dir") + "/Files";
        File folder = new File(filesFolderPath);
        File[] listOfFiles = folder.listFiles();
        String fileNames[] = new String[listOfFiles.length];

        for (int i = 0; i < listOfFiles.length; i++) {
            fileNames[i] = listOfFiles[i].getName();
        }

        if (fileNames.length < 1) {
            System.out.println("Server is out of files!");
            return;
        }
        
        printInfo(
            Thread.currentThread().getId(), 
            "Sending list of files.", 
            this.socket.getRemoteSocketAddress().toString()
        );

        try {
            ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());
            oos.writeObject(fileNames);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            System.out.println("IOException Error");
            e.printStackTrace();
        }
    }

    private void printInfo(long threadID, String event, String host) {
        System.out.println("############## SERVER LOG INFO ##############");
        System.out.println("Thread #" + threadID);
        System.out.println(event);
        System.out.println("Connection host: " + host);
        System.out.println("#############################################");
    }
}