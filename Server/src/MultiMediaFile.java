import java.io.Serializable;

public class MultiMediaFile implements Serializable {

    private String fileName;
    private byte[] fileBuffer;

    public MultiMediaFile(String fileName, byte[] fileBuffer){
        this.fileName = fileName;
        this.fileBuffer = fileBuffer;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileBuffer() {
        return fileBuffer;
    }
}
