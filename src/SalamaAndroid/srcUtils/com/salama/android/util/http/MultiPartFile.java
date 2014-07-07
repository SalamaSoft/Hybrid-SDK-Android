package com.salama.android.util.http;

import java.io.File;

public class MultiPartFile {
	private String name;
	private File file;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}

}
