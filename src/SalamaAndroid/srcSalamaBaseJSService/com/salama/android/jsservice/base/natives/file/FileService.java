package com.salama.android.jsservice.base.natives.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import MetoXML.XmlDeserializer;
import android.util.Log;

import com.salama.android.webcore.WebManager;

public class FileService {
	private final int TEMP_BUFFER_LEN = 1024;

	/**
	 * 取得实际物理存储上的路径
	 * @param virtualPath 虚拟路径(/xxx，根路径对应html目录)
	 * @return 实际物理存储上的路径
	 */
	public String getRealPathByVirtualPath(String virtualPath) {
		return WebManager.getWebController().toRealPath(virtualPath);
	}
	
	/**
	 * 文件是否存在
	 * @param filePath 文件路径
	 * @return 1:是  0:否
	 */
	public int isExistsFile(String filePath) {
		File file = new File(filePath);
		return file.exists()?1:0;
	}
	
	/**
	 * 路径是否存在，并且是否目录
	 * @param filePath 目录路径
	 * @return 1:是  0:否
	 */
	public int isExistsDir(String dirPath) {
		File file = new File(dirPath);
		
		return (file.exists() && file.isDirectory())?1:0;
	}
	
	/**
	 * 取得临时文件目录
	 * @return 临时文件目录路径
	 */
	public String getTempDirPath() {
		return WebManager.getWebController().getTempPath();
	}
	
	/**
	 * 文件拷贝
	 * @param from 源文件路径
	 * @param to 目标文件路径
	 * @return 目标文件路径
	 */
	public String copyFileFrom(String from, String to) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		try {
			fis = new FileInputStream(from);
			fos = new FileOutputStream(to);
			
			copyStream(fis, fos);
			
			return to;
		} catch (Exception e) {
			Log.e("FileService", "copyFileFrom()", e);
			return to;
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
	 * 文件移动
	 * @param from 源文件路径
	 * @param to 目标文件路径
	 * @return 目标文件路径
	 */
	public String moveFileFrom(String from, String to) {
		File fileFrom = new File(from);
		File fileTo = new File(to);
		
		if(fileTo.exists()) {
			fileTo.delete();
		}
		
		fileFrom.renameTo(fileTo);
		
		return to;
	}
	
	/**
	 * 文本方式读取文件内容(utf-8编码方式)
	 * @param filePath 文件路径
	 * @return 文件内容
	 */
	public String readAllText(String filePath) {
		InputStreamReader reader = null;
		FileInputStream fis = null;
		StringBuilder sb = new StringBuilder();
		char[] chBuff = new char[TEMP_BUFFER_LEN];
		int readCnt = 0;
		
		try {
			fis = new FileInputStream(filePath);
			reader = new InputStreamReader(fis, XmlDeserializer.DefaultCharset);
			
			while(true) {
				readCnt = reader.read(chBuff, 0, TEMP_BUFFER_LEN);
				if(readCnt < 0) {
					break;
				}
				
				if(readCnt > 0) {
					sb.append(chBuff, 0, readCnt);
				}
			}
			
			return sb.toString();
		} catch (Exception e) {
			Log.e("FileService", "readAllText()", e);
			return "";
		} finally {
			try {
				fis.close();
			} catch(Exception e) {
			}
			try {
				reader.close();
			} catch(Exception e) {
			}
		}
	}
	
	/**
	 * 写入文本文件(文件不存在的话，被创建。文件存在的话，原内容被冲掉)(utf-8编码方式)
	 * @param filePath 文件路径
	 * @return 文件路径
	 */
	public String writeTextToFile(String filePath, String text) {
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		
		try {
			fos = new FileOutputStream(filePath);
			writer = new OutputStreamWriter(fos, XmlDeserializer.DefaultCharset);
			
			writer.write(text);
			writer.flush();
			
			return filePath;
		} catch (Exception e) {
			Log.e("FileService", "writeTextToFile()", e);
			return filePath;
		} finally {
			try {
				fos.close();
			} catch(Exception e) {
			}
			try {
				writer.close();
			} catch(Exception e) {
			}
		}
	}

	/**
	 * 追加写入文本文件(文件不存在的话，被创建。文件存在的话，在原内容末尾追加)
	 * @param filePath 文件路径
	 * @return 文件路径
	 */
	public String appendTextToFile(String filePath, String text) {
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		
		try {
			fos = new FileOutputStream(filePath);
			writer = new OutputStreamWriter(fos, XmlDeserializer.DefaultCharset);
			
			writer.append(text);
			writer.flush();
			
			return filePath;
		} catch (Exception e) {
			Log.e("FileService", "writeTextToFile()", e);
			return filePath;
		} finally {
			try {
				fos.close();
			} catch(Exception e) {
			}
			try {
				writer.close();
			} catch(Exception e) {
			}
		}
	}

	/**
	 * 统计目录所有文件用量(单位byte)
	 * @return 目录所有文件用量(单位byte)
	 */
	public long calculateVolumeOfDir(String dirPath) {
		File dir = new File(dirPath);
		
		List<File> fileList = new ArrayList<File>();
		listFilesRecursivelyInDir(dir, fileList);
		
		long volumnBytes = 0;
		File file = null;
		for(int i = 0; i < fileList.size(); i++) {
			file = fileList.get(i);
			if(!file.isDirectory() && file.exists()) {
				volumnBytes += file.length();
			}
		}
		
		return volumnBytes;
	}
	
	/**
	 * 列出目录下所有文件名(不递归)
	 * @param dirPath 目录路径
	 * @param isIncludeSubDir 是否包含子目录
	 * @return 文件名列表
	 */
	public List<String> listFileNamesInDir(String dirPath, int isIncludeSubDir) {
		List<String> fileNameList = new ArrayList<String>();

		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		
		File file = null;
		for(int i = 0; i < files.length; i++) {
			file = files[i];
			
			if(file.isDirectory()) {
				if(isIncludeSubDir == 1) {
					fileNameList.add(file.getName());
				}
			} else {
				fileNameList.add(file.getName());
			}			
			
		}
		
		return fileNameList;
	}
	
	public List<String> listFilesInDir(String dirPath, int isIncludeSubDir) {
		List<String> filePathList = new ArrayList<String>();

		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		
		File file = null;
		for(int i = 0; i < files.length; i++) {
			file = files[i];
			
			if(file.isDirectory()) {
				if(isIncludeSubDir == 1) {
					filePathList.add(file.getAbsolutePath());
				}
			} else {
				filePathList.add(file.getAbsolutePath());
			}			
			
		}
		
		return filePathList;
	}
	
	/**
	 * 列出目录下所有文件路径(递归)
	 * @param dirPath 目录路径
	 * @return 文件路径列表
	 */
	public List<String> listFilesRecursivelyInDir(String dirPath) {
		List<String> filePathList = new ArrayList<String>();

		File dir = new File(dirPath);
		List<File> fileList = new ArrayList<File>();
		listFilesRecursivelyInDir(dir, fileList);
		
		File file = null;
		for(int i = 0; i < fileList.size(); i++) {
			file = fileList.get(i);
			
			filePathList.add(file.getAbsolutePath());
		}
		
		return filePathList;
	}
	
	/**
	 * 删除文件
	 * @param filePath 文件路径
	 * @return 文件路径
	 */
	public String deleteFile(String filePath) {
		File file = new File(filePath);
		
		file.delete();
		
		return filePath;
	}
	
	/**
	 * 删除目录(递归)
	 * @param dirPath 目录路径
	 * @return 目录路径
	 */
	public String deleteDir(String dirPath) {
		File dir = new File(dirPath);

		deleteDirRecursively(dir);
		
		return dirPath;
	}
	
	public String mkdir(String dirPath) {
		File dir = new File(dirPath);
		
		dir.mkdirs();
		
		return dirPath;
	}
	
	/**
	 * 压缩文件
	 * @param filePath
	 * @param zipPath
	 * @return zipPath
	 */
	public String compressZipFromFile(String filePath, String zipPath) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		ZipOutputStream zipOS = null;
		ZipEntry entry = null;
		
		File file = null;
		byte[] tempBuff = new byte[256];
		int readCnt = 0;
		
		try {
			fos = new FileOutputStream(zipPath, false);
			zipOS = new ZipOutputStream(fos);
			file = new File(filePath);
			
			//add an entry of a file
			entry = new ZipEntry(file.getName());
			zipOS.putNextEntry(entry);

			try {
				fis = new FileInputStream(filePath);
				while(true) {
					readCnt = fis.read(tempBuff, 0, tempBuff.length);
					
					if(readCnt < 0) break;
					
					zipOS.write(tempBuff, 0, readCnt);
					zipOS.flush();
				}
			} finally {
				if(fis != null) {
					try {
						fis.close();
					} catch(IOException ex) {
					}
				}
			}
			
			zipOS.closeEntry();
			
			return zipPath;
		} catch (Exception e) {
			Log.e("FileService", "compressZipFromFile()", e);
			return zipPath;
		} finally {
			if(zipOS != null) {
				try {
					zipOS.close();
				} catch(IOException ex) {}
			}
			if(fos != null) {
				try {
					fos.close();
				} catch(IOException ex) {}
			}
		}
		
	}
	
	/**
	 * 压缩文件
	 * @param dirPath
	 * @param zipPath
	 * @return zipPath
	 */
	public String compressZipFromDir(String dirPath, String zipPath) {
		File dir = new File(dirPath);
		List<File> fileList = new ArrayList<File>();
		listFilesRecursivelyInDir(dir, fileList);
				
		FileInputStream fis = null;
		FileOutputStream fos = null;
		ZipOutputStream zipOS = null;
		ZipEntry entry = null;
		
		File file = null;
		byte[] tempBuff = new byte[TEMP_BUFFER_LEN];
		int readCnt = 0;
		
		try {
			fos = new FileOutputStream(zipPath, false);
			zipOS = new ZipOutputStream(fos);
			
			int zipEntryNameBeginIndex = dirPath.length();
			if(!dirPath.endsWith("/")) {
				zipEntryNameBeginIndex++;
			}
			
			String zipEntryPath = null;
			
			for(int i = 0; i < fileList.size(); i++) {
				file = fileList.get(i);
				
				//add an entry of a file ----------------
				zipEntryPath = file.getAbsolutePath().substring(zipEntryNameBeginIndex);
				if(file.isDirectory()) {
					if(!zipEntryPath.endsWith("/")) {
						zipEntryPath = zipEntryPath.concat("/");
					}
					entry = new ZipEntry(zipEntryPath);
					zipOS.putNextEntry(entry);
				} else {
					entry = new ZipEntry(zipEntryPath);
					zipOS.putNextEntry(entry);

					try {
						fis = new FileInputStream(file);
						while(true) {
							readCnt = fis.read(tempBuff, 0, tempBuff.length);
							
							if(readCnt < 0) break;
							
							zipOS.write(tempBuff, 0, readCnt);
							zipOS.flush();
						}
					} finally {
						if(fis != null) {
							try {
								fis.close();
							} catch(IOException ex) {
							}
						}
					}
				}//if
			}//for
			
			zipOS.closeEntry();
			
			return zipPath;
		} catch (Exception e) {
			Log.e("FileService", "compressZipFromFile()", e);
			return zipPath;
		} finally {
			if(zipOS != null) {
				try {
					zipOS.close();
				} catch(IOException ex) {}
			}
			if(fos != null) {
				try {
					fos.close();
				} catch(IOException ex) {}
			}
		}
	}
	
	/**
	 * 解压缩文件
	 * @param zipPath
	 * @param toDir
	 * @return toDir
	 */
	public String decompressZip(String zipPath, String toDir) {
		File webBaseDir = new File(toDir);
		
		ZipInputStream zipInputS = null;
		FileInputStream zipFis = null;
		try {
			zipFis = new FileInputStream(zipPath);
			zipInputS = new ZipInputStream(zipFis);
			ZipEntry entry;
			File file = null;
			String entryName = null;
			byte[] tempBuf = new byte[TEMP_BUFFER_LEN];

			FileOutputStream fos = null;
			int readCnt;

			while(true) {
				entry = zipInputS.getNextEntry();
				if(entry == null) {
					break;
				}

				entryName = entry.getName();
				
				if(entry.isDirectory()) {
					//create dir
					file = new File(webBaseDir, entryName);
					file.mkdir();
				} else {
					file = new File(webBaseDir, entryName);
					
					//save the file
					fos = new FileOutputStream(file);
					try {
						while(true) {
							readCnt = zipInputS.read(tempBuf, 0, TEMP_BUFFER_LEN);
							
							if(readCnt <= 0) {
								break;
							}
							
							fos.write(tempBuf, 0, readCnt);
							fos.flush();
						}
					} finally {
						try {
							fos.close();
						} catch(Exception e) {
						}
					}
					
				}
				
				zipInputS.closeEntry();
			}
			
			return toDir;
		} catch (Exception e) {
			Log.e("FileService", "decompressZip()", e);
			return toDir;
		} finally {
			try {
				zipInputS.close();
			} catch(Exception e) {
			}
			try {
				zipFis.close();
			} catch(Exception e) {
			}
		}
	} 
	
	private void deleteDirRecursively(File dir) {
		File[] files = dir.listFiles();
		
		if(files != null && files.length > 0) {
			for(int i = 0; i < files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirRecursively(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		
		dir.delete();
	}
	
	private void listFilesRecursivelyInDir(File dir, List<File> fileList) {
		File[] files = dir.listFiles();
		
		if(files != null && files.length > 0) {
			for(int i = 0; i < files.length; i++) {
				if(files[i].isDirectory()) {
					fileList.add(files[i]);
					listFilesRecursivelyInDir(files[i], fileList);
				} else {
					fileList.add(files[i]);
				}
			}
		}
	}
	
	private void copyStream(InputStream input, FileOutputStream output) throws IOException {
		byte[] tempBuff = new byte[TEMP_BUFFER_LEN];
		int readCnt = 0;
		
		while(true) {
			readCnt = input.read(tempBuff, 0, TEMP_BUFFER_LEN);
			
			if(readCnt < 0) {
				break;
			}
			
			if(readCnt > 0) {
				output.write(tempBuff, 0, readCnt);
				output.flush();
			}
		}
	} 
}
