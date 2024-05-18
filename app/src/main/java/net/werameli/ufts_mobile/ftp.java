package net.werameli.ufts_mobile;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ftp {
    public static FTPClient loginFtp(String host, int port, String username, String password) throws Exception {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(host, port);
        ftpClient.login(username, password);
        return ftpClient;
    }

    public static ArrayList<String> ftpPrintFilesList(FTPClient ftpClient, String dir_path) throws IOException {
        ftpClient.enterLocalPassiveMode();
        ArrayList<String> listItems = new ArrayList<String>();
        FTPFile[] ftpFiles = ftpClient.listFiles(dir_path);
        for (FTPFile file : ftpFiles) {
            Log.i("TAG", file.getName());
            listItems.add(file.getName());
        }
        return listItems;
    }


    public static void uploadFile(String localPath, String remotePath, FTPClient ftpClient) throws Exception {
        ftpClient.enterLocalPassiveMode();
        FileInputStream fileInputStream = new FileInputStream(localPath);
        ftpClient.storeFile(remotePath, fileInputStream);
    }

    public static void renameFile(String oldPath, String newPath, FTPClient ftpClient) throws Exception {
        ftpClient.rename(oldPath, newPath);
    }

    public static boolean downloadFile(String filename, File localFile, FTPClient ftpClient) throws Exception {
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();

        OutputStream outputStream = null;
        boolean success = false;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(
                    localFile));
            success = ftpClient.retrieveFile(filename, outputStream);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

        return success;
    }

    public static void deleteFile(String path, FTPClient ftpClient) throws Exception {
        ftpClient.enterLocalPassiveMode();
        ftpClient.deleteFile(path);
    }
}