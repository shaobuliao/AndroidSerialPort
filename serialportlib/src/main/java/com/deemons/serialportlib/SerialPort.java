package com.deemons.serialportlib;

import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * author： deemons
 * date:    2018/5/18
 * desc:
 */
public class SerialPort {

    private static final String TAG = "SerialPort";

    private static final String DEFAULT_SU_PATH = "/system/bin/su";

    private static String sSuPath = DEFAULT_SU_PATH;

    /**
     * Set the su binary path, the default su binary path is {@link #DEFAULT_SU_PATH}
     *
     * @param suPath su binary path
     */
    public static void setSuPath(String suPath) {
        if (suPath == null) {
            return;
        }
        sSuPath = suPath;
    }

    /**
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor   mFd;
    private FileInputStream  mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec(sSuPath);
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    public SerialPort(String devicePath, int baudrate, int flags)
        throws SecurityException, IOException {
        this(new File(devicePath), baudrate, flags);
    }

    public SerialPort(File device, int baudrate) throws SecurityException, IOException {
        this(device, baudrate, 0);
    }

    public SerialPort(String devicePath, int baudrate) throws SecurityException, IOException {
        this(new File(devicePath), baudrate, 0);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI
    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    static {
        System.loadLibrary("serial-port");
    }
}
