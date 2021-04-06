package com.kwic.common.io;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.kwic.common.log.Listener;
import com.kwic.common.util.CProperties;
import com.kwic.common.util.PluginUtil;

public class JPrintStream extends PrintStream{
	public static final String		_STANDARD_OUT_LOG__PROPERTY_PREFIX	= "stdout.";
	
	private static final String		_line			= System.getProperty("line.separator");
	private static final String		_logProperties	= "../config/log.properties";
	private static SimpleDateFormat	sf				= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS");		
	
	private boolean		writeFile;
	private boolean		printConsole;
	
	private long		maxFileSize;
	private LogWriter	writer;
	private Properties	properties	= new Properties();
	
	public JPrintStream() throws Exception{
		super(System.out);

		
		String configPath	= System.getProperty("log.properties.path");

		if(configPath==null || !new File(configPath).exists())
			configPath	= "../config/log.properties";
		if(!new File(configPath).exists())
			configPath	= "./config/log.properties";
		
		loadProperties(configPath);

		resetWriter();
	}
	
	public JPrintStream( String propertyResource ) throws Exception{
		super(System.out);
		loadProperties(propertyResource);
		resetWriter();
	}
	
	private Properties loadProperties(String propertyResource){
		if(propertyResource==null || "".equals(propertyResource))
			propertyResource	= _logProperties;

		CProperties	props	= new CProperties(true);
		try{
			//props.load(ClassLoader.getSystemResourceAsStream(propertyResource));
			props.loadProperties(propertyResource);
			
			Set<Object> keys		= props.keySet();
			Iterator<Object> iter	= keys.iterator();
			String key				= null;
			String propName			= null;
			
			while(iter.hasNext()){
				key	= (String) iter.next();
				
				if(!key.startsWith(_STANDARD_OUT_LOG__PROPERTY_PREFIX))
					continue;
				
				propName	= key.substring(key.indexOf(".")+1);
				properties.setProperty(propName, props.getProperty(key));
			}
			
			writeFile			= new Boolean(properties.getProperty("writeFile")).booleanValue();
			printConsole		= new Boolean(properties.getProperty("printConsole")).booleanValue();
			try{maxFileSize		= Long.parseLong(properties.getProperty("maxFileSize"));}catch(Exception ex){}
			
		}catch(Exception e){
			err(e);
		}
		return properties;
	}
	
	private void resetWriter() throws Exception{
		if(!writeFile)
			return;

		if(properties.getProperty("baseDir")==null || "".equals(properties.getProperty("baseDir").trim()))
			throw new Exception("Define [stdout.baseDir] property or set property [stdout.writeFile=false].");
		
		if(this.writer!=null){
			try{writer.flush();}catch(Exception e){}
			try{writer.close();}catch(Exception e){}
		}

		final String today	= new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
		
		String dirPath	= properties.getProperty("baseDir");
		List<String> varList	= PluginUtil.getConditions(dirPath, PluginUtil._REPLACE_PARAM_CHAR);
		HashMap<String,String> map	= new HashMap<String,String>();
		for(int i=0;i<varList.size();i++){
			try{map.put(varList.get(i), new java.text.SimpleDateFormat((String)varList.get(i)).format(new java.util.Date()));}catch(Exception ex){}
		}
		Iterator<String> iter	= map.keySet().iterator();
		String key	= null;
		while(iter.hasNext()){
			key	= (String) iter.next();
			dirPath	= PluginUtil.replaceAll(dirPath, PluginUtil._REPLACE_PARAM_CHAR+key+PluginUtil._REPLACE_PARAM_CHAR, (String)map.get(key));
		}

		File subFolder	= new File(dirPath);
		if(!subFolder.exists())
			subFolder.mkdirs();

//		File subFolder	= new File(dirPath, today.substring(0,6));
//		if(!subFolder.exists() || !subFolder.isDirectory())
//			subFolder.mkdirs();

		//filename = >> stdout_20120101.130201.log
		if(new File(subFolder,"stdout_"+today.substring(0,8)+".log").exists())
			new File(subFolder,"stdout_"+today.substring(0,8)+".log").renameTo(new File(subFolder,"stdout_"+today.substring(0,8)+"_"+today.substring(8)+".log"));

		writer	= new LogWriter(new File(subFolder,"stdout_"+today.substring(0,8)+".log"));
	}
	
	private synchronized void checkWriterLifeCycle() throws Exception{
		int date	= Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new java.util.Date()));
		
		if(date!=writer.createDate())
			resetWriter();

		if(maxFileSize!=0 && writer.getFile().length()>=maxFileSize)
			resetWriter();
	}
	
	public Writer getWriter(String service){
		return this.writer;
	}

	public OutputStream getOutputStream(){
		return super.out;
	}
	
	public void print(boolean flag){
		if(printConsole)
			super.print(flag);
		writeLog(String.valueOf(flag));
	}

	public void print(char c){
		if(printConsole)
			super.print(c);
		writeLog(String.valueOf(c));
	}

	public void print(int i){
		if(printConsole)
			super.print(i);
		writeLog(String.valueOf(i));
	}

	public void print(long l){
		if(printConsole)
			super.print(l);
		writeLog(String.valueOf(l));
	}

	public void print(float f){
		if(printConsole)
			super.print(f);
		writeLog(String.valueOf(f));
	}

	public void print(double d){
		if(printConsole)
			super.print(d);
		writeLog(String.valueOf(d));
	}

	public void print(char ac[]){
		if(printConsole)
			super.print(ac);
		writeLog(String.valueOf(ac));
	}

	public void print(String s){
		if(printConsole)
			super.print(s);
		writeLog(s);
	}

	public void print(Object obj){
		if(printConsole)
			super.print(obj);
		writeLog(String.valueOf(obj));
	}

	public void println(){
		if(printConsole)
			super.println();
		writelnLog("");
	}

	public void println(boolean flag){
		if(printConsole){
			super.print(flag);
			super.println();
		}
		writelnLog(String.valueOf(flag));
	}

	public void println(char c){
		if(printConsole){
			super.print(c);
			super.println();
		}
		writelnLog(String.valueOf(c));
	}

	public void println(int i){
		if(printConsole){
			super.print(i);
			super.println();
		}
		writelnLog(String.valueOf(i));
	}

	public void println(long l){
		if(printConsole){
			super.print(l);
			super.println();
		}
		writelnLog(String.valueOf(l));
	}

	public void println(float f){
		if(printConsole){
			super.print(f);
			super.println();
		}
		writelnLog(String.valueOf(f));
	}

	public void println(double d){
		if(printConsole){
			super.print(d);
			super.println();
		}
		writelnLog(String.valueOf(d));
	}

	public void println(char ac[]){
		if(printConsole){
			super.print(ac);
			super.println();
		}
		writelnLog(String.valueOf(ac));
	}

	public void println(String s){
		if(printConsole){
			super.print(s);
			super.println();
		}
		writelnLog(s);
	}

	public void println(Object obj){
		if(printConsole){
			super.print(obj);
			super.println();
		}
		writelnLog(String.valueOf(obj));
	}

	public void write(int i){
		if(printConsole)
			super.write(i);
	}
	public void writelnLog(String str){
		writeLog(str+_line);
	}
	public void writeLog(String str){
		try{
			if(writeFile && writer!=null){
				checkWriterLifeCycle();
				StringBuffer sb	= new StringBuffer();
				
				sb.append("[").append(sf.format(new java.util.Date())).append("] ")
				//.append("[").append(getStackTrace()).append("] ")
				;
					
				sb.append(str);
				writer.write(sb.toString());
				writer.flush();
				
				if(listener!=null)
					listener.write(sb.toString());
			}
		}catch(Exception e){
			err(e);
		}
	}

	private Listener listener;
	public void addListener(Listener listener){
		this.listener	= listener;
	}
	public void removeListener(){
		if(listener!=null)
			listener	= null;
	}

	protected String getStackTrace(){
		StackTraceElement[] arr	= new Throwable().getStackTrace();
//		StringBuffer sb	= new StringBuffer();
//		for(int i=1;i<arr.length;i++){
//			sb.append("["+arr[i]+"]");
//		}
//		return sb.toString();
		
		String stack	= null;
		for(int i=1;i<arr.length;i++){
			if(arr[i].toString().indexOf("JPrintStream")<0 && arr[i].toString().indexOf("Log")<0){
				stack	= arr[i].toString();
			}
		}
		if(stack==null)
			stack	= arr[arr.length-1].toString();
		return stack;
	}
	
	private void err(Exception e){
		System.setOut((PrintStream) super.out);
		System.setErr((PrintStream) super.out);
		e.printStackTrace();
	}

	public static void replaceSystemOut(){
		try{
			JPrintStream	ps	= new JPrintStream();
			if(System.out.getClass()!=JPrintStream.class)
				System.setOut(ps);
			if(System.err.getClass()!=JPrintStream.class)
				System.setErr(ps);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void replaceSystemOut(String propertyResource){
		try{
			JPrintStream	ps	= new JPrintStream(propertyResource);
			System.setOut(ps);
			System.setErr(ps);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
