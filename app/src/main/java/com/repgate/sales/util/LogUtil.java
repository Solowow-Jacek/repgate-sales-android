package com.repgate.sales.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogUtil {

	private static final boolean DBG_ENABLE = true;
	private static final boolean WRITE_FILE = false;

	public static void printDebugLog(String tag, String msg) {
		if (DBG_ENABLE)
			Log.d(tag, msg);
		writeTextFileToSD(tag,msg);
	}

	public static void printErrorLog(String tag, String msg) {
		if (DBG_ENABLE)
			Log.e(tag, msg);
		writeTextFileToSD(tag,msg);
	}

	public static void printInfoLog(String tag, String msg) {
		if (DBG_ENABLE)
			Log.i(tag, msg);
		writeTextFileToSD(tag,msg);
	}

	public static void printVerboseLog(String tag, String msg) {
		if (DBG_ENABLE)
			Log.v(tag, msg);
		writeTextFileToSD(tag,msg);
	}

	public static void printWarnLog(String tag, String msg) {
		if (DBG_ENABLE)
			Log.w(tag, msg);
		writeTextFileToSD(tag,msg);
	}

    final private static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    } 

	public static void writeTextFileToSD(String devAddress, byte[] scanRecord) {
		String sdstr = Environment.getExternalStorageDirectory().getPath();
		if (sdstr != null)
			sdstr += "/BLEHeaterController/";

		File sdcard = new File(sdstr);
		sdcard.mkdirs();
		File file = new File(sdcard, "data");

		if (file.exists() == false) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
			String strAddr = "Address : " + devAddress + "\n";
			buf.append(strAddr);
			String strData = "Data : " + bytesToHex(scanRecord) + "\n";
			buf.append(strData);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeTextFileToSD(String tag, String data) {
		if (WRITE_FILE) {
			String sdstr = Environment.getExternalStorageDirectory().getPath();
			if (sdstr != null)
				sdstr += "/RaceHero/";
	
			File sdcard = new File(sdstr);
			sdcard.mkdirs();
			File file = new File(sdcard, "log");
	
			if (file.exists() == false) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	
			try {
				// BufferedWriter for performance, true to set append to file flag
				BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
				String strAddr = "Tag : " + tag + "\n";
				buf.append(strAddr);
				String strData = "Data : " + data + "\n";
				buf.append(strData);
				buf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void deleteLogFile() {
		String sdstr = Environment.getExternalStorageDirectory().getPath();
		if (sdstr != null)
			sdstr += "/RaceHero/";

		File sdcard = new File(sdstr);
		sdcard.mkdirs();
		File file = new File(sdcard, "log");

		if (file.exists()) {
			try {
				file.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
