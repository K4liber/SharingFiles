package server;

import java.io.*; 
import java.net.Socket;
import java.nio.file.Files;

public class ClientRunnable implements Runnable {

    private Socket conn = null;

    public ClientRunnable(Socket s) {
        this.conn = s;
    }

    public void run() {
        try {
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String message = br.readLine();

            if (message != null && message.equals("upload")) {
                ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
                uploadingFile(conn, ois);
            } else if (message != null && message.equals("list")) {
                sendFileNames(conn);
            } else {
                sendFileContent(conn, message);
            }
        } catch (IOException e) {
            System.out.println("IO Exception");
            e.printStackTrace();
        }
    }

    private void uploadingFile(Socket conn, ObjectInputStream ois) {
        try {
            Object object = ois.readObject();
            if (object == null)
                return;

            File file = (File) object;
            printInfo(
                Thread.currentThread().getId(), 
                "Saving file: " + file.getName(), 
                conn.getRemoteSocketAddress().toString()
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

    private void sendFileContent(Socket conn, String fileName) {
        String filePath = System.getProperty("user.dir") + "/Files/" + fileName;
        File myFile = new File(filePath);
        if (!myFile.exists()) {
            System.out.println("File does not exist!");
            return;
        }
        printInfo(
            Thread.currentThread().getId(), 
            "Sending file: " + myFile.getAbsolutePath(), 
            conn.getRemoteSocketAddress().toString()
        );
        try {
            byte[] content = Files.readAllBytes(myFile.toPath());
            ObjectOutputStream oos = new ObjectOutputStream(
            conn.getOutputStream());
            oos.writeObject(content);
            oos.flush();
        } catch (IOException e) {
            System.out.println("IOException Error");
            e.printStackTrace();
        }
    }
   
    private void sendFileNames(Socket conn) {
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
            conn.getRemoteSocketAddress().toString()
        );
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
            conn.getOutputStream());
            oos.writeObject(fileNames);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            System.out.println("IOException Error");
            e.printStackTrace();
        }
    }

    private void printInfo(long threadID, String event, String host) {
        System.out.println("Thread #" + threadID);
        System.out.println(event);
        System.out.println("Connection host: " + host);
    }
}