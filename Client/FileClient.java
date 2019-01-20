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

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    public List<File> fileList = new ArrayList<File>();
    private String hostName;
    int port;
    private Socket sock = null;

    public FileClient(String hostName, int port) {
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

    public void askForFiles() {
        try {
            OutputStream os = sock.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            String sendMessage = "multipleDownload\n";
            bw.write(sendMessage);
            bw.flush();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
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

    public void receiveFileFromServer(String fileName) throws Exception{    
        InputStream socketInputStream = sock.getInputStream();
        System.out.println("Asking for file: " + fileName);
        OutputStream os = sock.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);
        String sendMessage = fileName;
        bw.write(sendMessage + "\n");
        bw.flush();
        ObjectInputStream ois = new ObjectInputStream(socketInputStream);
        byte[] content = (byte[]) ois.readObject();
        FileContent file = (FileContent) convertFromBytes(content);
        saveFile(file);
    }

    private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } 
    }
    
    public void receiveFilesFromServer(ArrayList<String> stringList) throws Exception{
        FileDialog fileDialog = new FileDialog(new Frame(),"Choose Destination", FileDialog.LOAD);
        fileDialog.setDirectory(null);
        fileDialog.setFile(null);
        fileDialog.setVisible(true);
        String directory = fileDialog.getDirectory();
        System.out.println("Asking for files:");

        for(int i=0; i<stringList.size(); i++) {
            System.out.println(stringList.get(i));
        }
        
        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
        oos.writeObject(stringList);
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
        byte[] content = (byte[]) ois.readObject();

        if (content == null)
            return;

        ArrayList<FileContent> fileList = (ArrayList<FileContent>) convertFromBytes(content);
        saveFiles(fileList, directory);
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

    public void saveFile(FileContent fileContent) throws IOException {
        FileDialog fileDialog = new FileDialog(new Frame(),"Choose Destination", FileDialog.SAVE);
        fileDialog.setDirectory(null);
        fileDialog.setFile(fileContent.getFileName());
        fileDialog.setVisible(true);

        if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            String targetFileName = fileDialog.getDirectory()
                + fileDialog.getFile();
            System.out.println("File will be saved to: " + targetFileName);
            Path path = Paths.get(targetFileName);
            Files.write(path, fileContent.getFileBytes());
        } else {
            System.out.println("File saving canceled.");
        }
    }

    public void saveFiles(ArrayList<FileContent> fileList, String directory) throws IOException {

        for (FileContent file: fileList) {
            String targetFileName = directory + file.getFileName();
            Path path = Paths.get(targetFileName);
            System.out.println("File will be saved to: " + targetFileName);
            Files.write(path, file.getFileBytes());
        }
    }
}
