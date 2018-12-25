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

public class FileClient {

    private final ExecutorService exec = Executors.newCachedThreadPool();
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
        saveFile(content);
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
        String targetFileName = fileDialog.getDirectory()
                + fileDialog.getFile();

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

    public void saveFile(byte[] content) throws IOException {
        FileDialog fileDialog = new FileDialog(new Frame(),"Choose Destination", FileDialog.SAVE);
        fileDialog.setDirectory(null);
        fileDialog.setFile("file_name_here.xxx");
        fileDialog.setVisible(true);

        String targetFileName = fileDialog.getDirectory()
                + fileDialog.getFile();

        if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            System.out.println("File will be saved to: " + targetFileName);
            Path file = Paths.get(targetFileName);
            Files.write(file, content);
        } else {
            System.out.println("File saving canceled.");
        }
    }
}
