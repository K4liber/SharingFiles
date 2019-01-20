package adds;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*; 
import java.io.Serializable;

// compile: javac -d . FileContent.java

public class FileContent implements Serializable{
    
    private String fileName;
    private byte[] fileBytes;

    public FileContent(File file, String fileName) {
        this.fileName = fileName;
        try {
            this.fileBytes = Files.readAllBytes(file.toPath());
        } catch (IOException ioException) {
            System.out.println("IOException!");
        }
    }

    public File getFile() {
        File file = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(this.fileBytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            file = (File) in.readObject();
        } catch (IOException ioException) {
            System.out.println("IOException!");
        } catch (ClassNotFoundException classNotFoundException) {
            System.out.println("ClassNotFoundException!");
        }
        return file;
    }

    public String getFileName() {
        return this.fileName;
    }

    public byte[] getFileBytes() {
        return this.fileBytes;
    }

}