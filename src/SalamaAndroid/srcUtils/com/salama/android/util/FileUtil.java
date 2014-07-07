package com.salama.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
	private FileUtil() {
		
	}
	
	/**
	 * 拷贝流
	 * @param src 源
	 * @param dest 目的
	 * @throws IOException
	 */
	public static void copyStream(InputStream src, OutputStream dest) throws IOException {
		byte[] tempBuff = new byte[1024];
		int iRead = 0;
		
		while(true) {
			iRead = src.read(tempBuff, 0, tempBuff.length);
			if(iRead < 0) {
				break;
			}
			
			dest.write(tempBuff, 0, iRead);
			dest.flush();
		}
	}

	/**
	 * 拷贝文件
	 * @param src 源文件
	 * @param dest 目的文件
	 * @throws IOException
	 */
	public static void copyFile(File src, File dest) throws IOException {
		if(!src.exists()) {
			return;
		}
		
		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			fis = new FileInputStream(src);
			fos = new FileOutputStream(dest);
			
			copyStream(fis, fos);
		} finally {
			try {
				fis.close();
			} catch(Exception e) {
			}
			try {
				fos.close();
			} catch(Exception e) {
			}
		}
	}
	
	/**
	 * 删除目录(递归)
	 * @param dir 目录
	 */
	public static void deleteDir(File dir) {
		if(!dir.exists()) {
			return;
		}
		
		File[] files = dir.listFiles();
		if(files != null && files.length > 0) {
			File file = null;
			for(int i = 0; i < files.length; i++) {
				file = files[i];
				
				if(file.isDirectory()) {
					deleteDir(file);
				} else {
					file.delete();
				}
			}
		}
		
		dir.delete();
	}
	
	/**
	 * 拷贝目录(递归)
	 * @param srcDir 源目录
	 * @param destDir 目的目录。如果不存在，则创建。
	 * @throws IOException
	 */
	public static void copyItemsInDir(File srcDir, File destDir) throws IOException {
		if(!destDir.exists()) {
			destDir.mkdirs();
		}
		
		File[] files = srcDir.listFiles();
		if(files != null && files.length > 0) {
			File srcFile = null;
			File destFile = null;
			for(int i = 0; i < files.length; i++) {
				srcFile = files[i];
				destFile = new File(destDir, srcFile.getName());
				
				if(srcFile.isDirectory()) {
					copyItemsInDir(srcFile, destFile);
				} else {
					copyFile(srcFile, destFile);
				}
			}
		}
	}
	
}
