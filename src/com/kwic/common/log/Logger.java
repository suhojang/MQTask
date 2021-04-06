package com.kwic.common.log;

import java.io.File;
import java.io.Writer;
import java.text.SimpleDateFormat;

import com.kwic.common.io.LogWriter;

/**
 * <pre>
 * Title		: Logger
 * Description	:  Log의 처리 및 로그파일 작성
 * Date			: 2011.12.09
 * Copyright	: Copyright	(c)	2012
 * Company		: KWIC
 * 
 *    수정일                  수정자                      수정내용
 * -------------------------------------------
 * 
 * </pre>
 *
 * @author 장정훈
 * @version	1.0
 * @since 1.4
 */
public class Logger {
	/**
	 * log level debug
	 * */
	private static final int _LOG_LEVEL_DEBUG		= 1;
	/**
	 * log level info
	 * */
	private static final int _LOG_LEVEL_INFO		= 2;
	/**
	 * log level warn
	 * */
	private static final int _LOG_LEVEL_WARN		= 3;
	/**
	 * log level error
	 * */
	private static final int _LOG_LEVEL_ERROR		= 4;
	
	/**
	 * log name debug
	 * */
	private static final String _LOG_NAME_DEBUG		= "DEBUG";
	/**
	 * log name info
	 * */
	private static final String _LOG_NAME_INFO		= "INFO";
	/**
	 * log name warn
	 * */
	private static final String _LOG_NAME_WARN		= "WARN";
	/**
	 * log name error
	 * */
	private static final String _LOG_NAME_ERROR		= "ERROR";

	/**
	 * line separator
	 * */
	private static final String		_line			= System.getProperty("line.separator");
	/**
	 * log date format
	 * */
	private static SimpleDateFormat	sf				= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS");		
	/**
	 * log properties object
	 * */
	private LogProperties props;
	/**
	 * file writer
	 * */
	private LogWriter writer;
	/**<pre>
	 * constructor
	 * reset writer object & log file
	 * </pre>
	 * @param props LogProperties
	 * */
	public Logger(LogProperties props){
		this.props	= props;
		try{
			resetWriter();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**<pre>
	 * 로그 객체 및 로그 파일을 새로 생성한다.
	 * </pre>
	 * @throws Exception
	 * */
	private void resetWriter() throws Exception{
		if(!props.getWriteFile())
			return;

		if(props.getBaseDir()==null || "".equals(props.getBaseDir().trim()))
			throw new Exception("Define ["+props.getServiceName()+".baseDir] property or set property ["+props.getServiceName()+".writeFile=false].");
		
		if(this.writer!=null){
			try{writer.flush();}catch(Exception e){}
			try{writer.close();}catch(Exception e){}
		}

		final String today	= new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
		
		File subFolder	= new File(props.getBaseDir());
		if(!subFolder.exists())
			subFolder.mkdirs();

//		File subFolder	= new File(dirPath, today.substring(0,6));
//		if(!subFolder.exists() || !subFolder.isDirectory())
//			subFolder.mkdirs();

		//filename = >> stdout_20120101.130201.log
		if(new File(subFolder,props.getServiceName()+"_"+today.substring(0,8)+".log").exists())
			new File(subFolder,props.getServiceName()+"_"+today.substring(0,8)+".log").renameTo(new File(subFolder,props.getServiceName()+"_"+today.substring(0,8)+"_"+today.substring(8)+".log"));

		writer	= new LogWriter(new File(subFolder,props.getServiceName()+"_"+today.substring(0,8)+".log"));
	}
	/**<pre>
	 * 로그 properties 에 따라서 로그파일의 생명주기를 판별하고 
	 * 새로운 로그객체를 생성한다.
	 * 
	 * 로그파일 신규생성 조건 : 로그파일이 생성되지 않았을 때
	 * 						로그파일의 사이즈가 설정값을 초과하였을 때
	 * 						로그 작성일자에 변경이 발생하였을 때
	 * </pre>
	 * @throws Exception
	 * */
	private synchronized void checkWriterLifeCycle() throws Exception{
		if(writer==null)
			resetWriter();
		else if(props.getMaxFileSize()!=0 && writer.getFile().length()>=props.getMaxFileSize())
			resetWriter();
		else if(Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new java.util.Date()))!=writer.createDate())
			resetWriter();
	}
	/**<pre>
	 * 생성되어진 Log Writer를 반환한다.
	 * </pre>
	 * @param service String
	 * */
	public Writer getWriter(String service){
		return this.writer;
	}
	/**<pre>
	 * debug log를 작성한다.
	 * </pre>
	 * @param v boolean
	 * */
	public void debug(boolean v){
		debug(String.valueOf(v));
	}
	/**<pre>
	 * debug log를 작성한다.
	 * </pre>
	 * @param v char
	 * */
	public void debug(char v){
		debug(String.valueOf(v));
	}
	/**<pre>
	 * debug log를 작성한다.
	 * </pre>
	 * @param v short
	 * */
	public void debug(short v){
		debug(String.valueOf(v));
	}
	/**<pre>
	 * debug log를 작성한다.
	 * </pre>
	 * @param v int
	 * */
	public void debug(int v){
		debug(String.valueOf(v));
	}
	/**<pre>
	 * debug log를 작성한다.
	 * </pre>
	 * @param v long
	 * */
	public void debug(long v){
		debug(String.valueOf(v));
	}
	/**<pre>
	 * debug log를 작성한다.
	 * </pre>
	 * @param v float
	 * */
	public void debug(float v){
		debug(String.valueOf(v));
	}
	/**<pre>
	 * debug log를 작성한다.
	 * </pre>
	 * @param v double
	 * */
	public void debug(double v){
		debug(String.valueOf(v));
	}
	/**<pre>
	 * debug log를 작성한다.
	 * </pre>
	 * @param v Object
	 * */
	public void debug(Object v){
		debug(v.toString());
	}
	/**<pre>
	 * debug log를 작성한다.
	 * </pre>
	 * @param v String
	 * */
	public void debug(String str){
		if(!props.debug())
			return;
		write(str,_LOG_LEVEL_DEBUG);
	}
	/**<pre>
	 * info log를 작성한다.
	 * </pre>
	 * @param v boolean
	 * */
	public void info(boolean v){
		info(String.valueOf(v));
	}
	/**<pre>
	 * info log를 작성한다.
	 * </pre>
	 * @param v char
	 * */
	public void info(char v){
		info(String.valueOf(v));
	}
	/**<pre>
	 * info log를 작성한다.
	 * </pre>
	 * @param v short
	 * */
	public void info(short v){
		info(String.valueOf(v));
	}
	/**<pre>
	 * info log를 작성한다.
	 * </pre>
	 * @param v int
	 * */
	public void info(int v){
		info(String.valueOf(v));
	}
	/**<pre>
	 * info log를 작성한다.
	 * </pre>
	 * @param v long
	 * */
	public void info(long v){
		info(String.valueOf(v));
	}
	/**<pre>
	 * info log를 작성한다.
	 * </pre>
	 * @param v float
	 * */
	public void info(float v){
		info(String.valueOf(v));
	}
	/**<pre>
	 * info log를 작성한다.
	 * </pre>
	 * @param v double
	 * */
	public void info(double v){
		info(String.valueOf(v));
	}
	/**<pre>
	 * info log를 작성한다.
	 * </pre>
	 * @param v Object
	 * */
	public void info(Object v){
		info(v.toString());
	}
	/**<pre>
	 * info log를 작성한다.
	 * </pre>
	 * @param v String
	 * */
	public void info(String str){
		if(!props.info())
			return;
		write(str,_LOG_LEVEL_INFO);
	}
	/**<pre>
	 * warn log를 작성한다.
	 * </pre>
	 * @param v boolean
	 * */
	public void warn(boolean v){
		warn(String.valueOf(v));
	}
	/**<pre>
	 * warn log를 작성한다.
	 * </pre>
	 * @param v char
	 * */
	public void warn(char v){
		warn(String.valueOf(v));
	}
	/**<pre>
	 * warn log를 작성한다.
	 * </pre>
	 * @param v short
	 * */
	public void warn(short v){
		warn(String.valueOf(v));
	}
	/**<pre>
	 * warn log를 작성한다.
	 * </pre>
	 * @param v int
	 * */
	public void warn(int v){
		warn(String.valueOf(v));
	}
	/**<pre>
	 * warn log를 작성한다.
	 * </pre>
	 * @param v long
	 * */
	public void warn(long v){
		warn(String.valueOf(v));
	}
	/**<pre>
	 * warn log를 작성한다.
	 * </pre>
	 * @param v float
	 * */
	public void warn(float v){
		warn(String.valueOf(v));
	}
	/**<pre>
	 * warn log를 작성한다.
	 * </pre>
	 * @param v double
	 * */
	public void warn(double v){
		warn(String.valueOf(v));
	}
	/**<pre>
	 * warn log를 작성한다.
	 * </pre>
	 * @param v Object
	 * */
	public void warn(Object v){
		warn(v.toString());
	}
	/**<pre>
	 * warn log를 작성한다.
	 * </pre>
	 * @param v String
	 * */
	public void warn(String str){
		if(!props.warn())
			return;
		write(str,_LOG_LEVEL_WARN);
	}
	/**<pre>
	 * error log를 작성한다.
	 * </pre>
	 * @param v boolean
	 * */
	public void error(boolean v){
		error(String.valueOf(v));
	}
	/**<pre>
	 * error log를 작성한다.
	 * </pre>
	 * @param v char
	 * */
	public void error(char v){
		error(String.valueOf(v));
	}
	/**<pre>
	 * error log를 작성한다.
	 * </pre>
	 * @param v short
	 * */
	public void error(short v){
		error(String.valueOf(v));
	}
	/**<pre>
	 * error log를 작성한다.
	 * </pre>
	 * @param v int
	 * */
	public void error(int v){
		error(String.valueOf(v));
	}
	/**<pre>
	 * error log를 작성한다.
	 * </pre>
	 * @param v long
	 * */
	public void error(long v){
		error(String.valueOf(v));
	}
	/**<pre>
	 * error log를 작성한다.
	 * </pre>
	 * @param v float
	 * */
	public void error(float v){
		error(String.valueOf(v));
	}
	/**<pre>
	 * error log를 작성한다.
	 * </pre>
	 * @param v double
	 * */
	public void error(double v){
		error(String.valueOf(v));
	}
	/**<pre>
	 * error log를 작성한다.
	 * </pre>
	 * @param v Object
	 * */
	public void error(Object v){
		error(v.toString());
	}
	/**<pre>
	 * error log를 작성한다.
	 * </pre>
	 * @param v String
	 * */
	public void error(String str){
		if(!props.error())
			return;
		write(str,_LOG_LEVEL_ERROR);
	}
	/**<pre>
	 * error log를 작성한다.
	 * </pre>
	 * @param e Exception
	 * */
	public void error(Throwable e){
		if(!props.error())
			return;
		
		write(e.toString(),_LOG_LEVEL_ERROR);
		StackTraceElement[] se	= e.getStackTrace();
		for(int i=0;i<se.length;i++){
			write("\t"+se[i].toString(),_LOG_LEVEL_ERROR);
		}
		if(e.getCause()!=null){
			se	= e.getCause().getStackTrace();
			if(se!=null && se.length>0){
				write("Caused by ",_LOG_LEVEL_ERROR);
				for(int i=0;i<se.length;i++){
					write("\t"+se[i].toString(),_LOG_LEVEL_ERROR);
				}
			}
		}
	}
	/**<pre>
	 * log를 작성한다.
	 * 형식 : [작성일자][로그레벨] 로그내용
	 * </pre>
	 * @param str String
	 * @param level int
	 * */
	private synchronized void write(String str,int level){
		try{
			StringBuffer sb	= new StringBuffer();
			sb.append("[").append(getLogLevel(level)).append("] ").append(str);
			if(props.getPrintConsole())
				System.out.println("["+sf.format(new java.util.Date())+"] "+sb.toString());
			
			if(props.getWriteFile() && writer!=null && level==_LOG_LEVEL_ERROR){
			//if(props.getWriteFile() && writer!=null){
				checkWriterLifeCycle();
//				StackTraceElement[] stacks	= new Throwable().getStackTrace();
				sb.setLength(0);
				sb.append("[").append(sf.format(new java.util.Date())).append("] ")
				.append("[").append(getLogLevel(level)).append("] ")
				//.append("[").append(stacks[stacks.length>=4?3:2].toString()).append("] ")
				.append(str).append(_line);
				
				writer.write(sb.toString());
				writer.flush();
			}

			if(listener!=null)
				listener.write("{"+props.getServiceName()+"} "+sb.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**<pre>
	 * log level에 해당하는 로그분류 문자를 반환한다.
	 * </pre>
	 * @param level int
	 * @return String
	 * */
	private static String getLogLevel(int type){
		if(type==_LOG_LEVEL_DEBUG)
			return _LOG_NAME_DEBUG;
		else if(type==_LOG_LEVEL_INFO)
			return _LOG_NAME_INFO;
		else if(type==_LOG_LEVEL_WARN)
			return _LOG_NAME_WARN;
		else if(type==_LOG_LEVEL_ERROR)
			return _LOG_NAME_ERROR;
		else
			return "NORMAL";
	}

	private Listener listener;
	public void addListener(Listener listener){
		this.listener	= listener;
	}
	public void removeListener(){
		if(listener!=null)
			listener	= null;
	}
}

