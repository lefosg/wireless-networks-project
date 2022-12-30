package com.wirelessnetworks.multimediafile;

import java.io.Serializable;

/**
 * Wrapper class for sending files to the client
 */
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
