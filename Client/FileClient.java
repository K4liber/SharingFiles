package client;

import java.awt.FileDialog;
import java.awt.Frame;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*; 
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import adds.FileContent;

// compile: javac -d . FileClient.java

public class FileClient {

    private ExecutorService executorService;
    private List<File> fileList;
    private String hostName;
    private int port;
    private Socket sock = null;

    public FileClient(String hostName, int port) {
        this.executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        );
        this.fileList = new ArrayList<File>();
    	this.hostName = hostName;
    	this.port = port;
    }

    public void connect() {
        try {
            this.sock = new Socket(hostName, port);
        } catch (IOException e) {
            System.out.println("UnknownHostException");
            e.printStackTrace();
        }
    }

    public Socket getNewConnection() {
        try {
            return new Socket(hostName, port);
        } catch (IOException e) {
            System.out.println("UnknownHostException");
            e.printStackTrace();
        }
        return null;
    }
    
    public String[] getFileList() {
        String fileList[] = null;
        try {
            OutputStream os = sock.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            String sendMessage = "list\n";
            bw.write(sendMessage);
            bw.flush();
            InputStream is = sock.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            fileList = (String[]) ois.readObject();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException");
            e.printStackTrace();
        }
        return fileList;
    }

    private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } 
    }
    
    public void receiveFileFromServer(ArrayList<String> stringList) throws Exception{
        FileDialog fileDialog = new FileDialog(new Frame(),"Choose Destination", FileDialog.LOAD);
        fileDialog.setDirectory(null);
        fileDialog.setFile(null);
        fileDialog.setVisible(true);
        String directory = fileDialog.getDirectory();
        
        this.sock.close();
        for (int i=0; i<stringList.size(); i++) {
            this.executorService.execute(
                new SaveFileRunnable(getNewConnection(), stringList.get(i), directory)
            );
        }
    }

    public void uploadFile() throws Exception{    
        OutputStream os = sock.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);
        String sendMessage = "upload";
        bw.write(sendMessage + "\n");
        bw.flush();

        FileDialog fileDialog = new FileDialog(new Frame(), "Choose a file", FileDialog.LOAD);
        fileDialog.setVisible(true);
        String targetFileName = fileDialog.getDirectory() + fileDialog.getFile();
        ObjectOutputStream oos = new ObjectOutputStream(os);

        if (fileDialog.getFile() != null) {
            File file = new File(targetFileName);
            System.out.println("Sending file: " + file.getName());
            oos.writeObject(file);
            oos.flush();
        } else {
            System.out.println("Sending file canceled.");
            oos.writeObject(null);
            oos.flush();
        }
    }
}
