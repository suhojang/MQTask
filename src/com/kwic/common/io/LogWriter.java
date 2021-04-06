package com.kwic.common.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class LogWriter extends FileWriter {
	
	private File 	file;
	private long 	creationTime;
	private int		createDate;
	
	public LogWriter(String file) throws IOException {
		this(new File(file));
	}
	public LogWriter(File file) throws IOException {
		super(file);
		this.file		= file;
		java.util.Date date	= new java.util.Date();
		creationTime	= date.getTime();
		createDate		= Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(date));
	}

	public long createTime(){
		return creationTime;
	}
	public int createDate(){
		return createDate;
	}
	public File getFile(){
		return file;
	}
}
