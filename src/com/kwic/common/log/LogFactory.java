package com.kwic.common.log;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.kwic.common.util.CProperties;
import com.kwic.common.util.PluginUtil;
import com.kwic.math.Calculator;

/**
 * <pre>
 * Title		: LogFactory
 * Description	: Log 를 작성하기 위한 객체들의 생성 및 관리
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
public class LogFactory {
	/**
	 * default log name
	 * */
	public static final String		_DEFAULT_LOG_PROPERTY_PREFIX	= "default";
	/**
	 * classpath of logger properties
	 * */
	private static final String		_logProperties	= "com/kwic/common/config/log.properties";
	/**
	 * mapper of logger properties
	 * */
	private static Hashtable<String,LogProperties>		logPropertyMapper	= new Hashtable<String,LogProperties>();
	/**
	 * mapper of logger
	 * */
	private static Hashtable<String,Logger>		loggerMapper		= new Hashtable<String,Logger>();
	/**
	 * singlton instance
	 * */
	private static LogFactory instance;
	
	private String configPath;
	private Properties	props;
	
	/**<pre>
	 * 생성자
	 * log properties load
	 * </pre>
	 * */
	private LogFactory(){
		configPath	= System.getProperty("log.properties.path");

		if(configPath==null || !new File(configPath).exists())
			configPath	= "../config/log.properties";
		if(!new File(configPath).exists())
			configPath	= "./config/log.properties";
		
		loadProperties(configPath);
	}
	/**<pre>
	 * 서비스명에 맞는 logger 객체를 반환한다.
	 * </pre>
	 * @param serviceName String
	 * @return Logger
	 * */
	public static Logger getLogger(String serviceName){
		synchronized(LogFactory.class){
			if(instance==null){
				instance	= new LogFactory();
			}
		}
		return createLogger(serviceName);
	}
	/**<pre>
	 * 서비스명에 해당하는 새로운 Logger 객체를 생성한다.
	 * </pre>
	 * @param serviceName String
	 * @return Logger
	 * */
	private synchronized static Logger createLogger(String serviceName){
		if(serviceName==null || "".equals(serviceName.trim()) || logPropertyMapper.get(serviceName)==null)
			serviceName	= _DEFAULT_LOG_PROPERTY_PREFIX;
		
		if(loggerMapper.get(serviceName)!=null)
			return (Logger) loggerMapper.get(serviceName);
		
		loggerMapper.put(serviceName, new Logger((LogProperties) logPropertyMapper.get(serviceName)));

		return (Logger) loggerMapper.get(serviceName);
	}
	
	public static void reload(){
		synchronized(LogFactory.class){
			instance	= new LogFactory();
		}
	}
	
	public static Properties getProperties(){
		synchronized(LogFactory.class){
			if(instance==null){
				instance	= new LogFactory();
			}
		}
		return instance.props;
	}
	
	/**<pre>
	 * log properties 로 부터 로그객체의 특성들을 loading한다.
	 * </pre>
	 * @param propertyResource String
	 * */
	private void loadProperties(String propertyResource){
		if(propertyResource==null || "".equals(propertyResource))
			propertyResource	= _logProperties;

		props		= new CProperties(true);
		LogProperties logProp	= null;
		
		try{
			//props.load(ClassLoader.getSystemResourceAsStream(propertyResource));
			((CProperties) props).loadProperties(propertyResource);
			if(props.getProperty("console.encoding")!=null && !"".equals(props.getProperty("console.encoding")))
				System.setOut(new PrintStream(System.out,true,props.getProperty("console.encoding")));
			
			Set<Object> keys		= props.keySet();
			Iterator<Object> iter	= keys.iterator();
			String key				= null;
			String service			= null;
			String propName			= null;
			
			while(iter.hasNext()){
				key	= (String) iter.next();
				
				if(key==null || "".equals(key.trim()))
					continue;
				if(key.indexOf(".")<0)
					key	= _DEFAULT_LOG_PROPERTY_PREFIX+"."+key;

				service		= key.substring(0,key.indexOf("."));
				propName	= key.substring(key.indexOf(".")+1);
				
				if(logPropertyMapper.get(service)==null)
					logPropertyMapper.put(service, new LogProperties());

				logProp	= (LogProperties) logPropertyMapper.get(service);
				logProp.setConsoleEncoding(props.getProperty("console.encoding"));
				
				logProp.setServiceName(service);
				if("printConsole".equals(propName))
					logProp.setPrintConsole(props.getProperty(key));
				if("writeFile".equals(propName))
					logProp.setWriteFile(props.getProperty(key));
				if("maxFileSize".equals(propName))
					logProp.setMaxFileSize(props.getProperty(key));
				if("baseDir".equals(propName))
					logProp.setBaseDir(props.getProperty("log.home")+"/"+props.getProperty(key));
				if("debug".equals(propName))
					logProp.setDebug(props.getProperty(key));
				if("info".equals(propName))
					logProp.setInfo(props.getProperty(key));
				if("warn".equals(propName))
					logProp.setWarn(props.getProperty(key));
				if("error".equals(propName))
					logProp.setError(props.getProperty(key));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}

/**
 * <pre>
 * Title		: LogProperties
 * Description	:  로그 특성을 저장하는 record class
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
class LogProperties{
	/**
	 * 서비스명
	 * */
	private String		serviceName;
	/**
	 * 파일생성 여부
	 * */
	private boolean		writeFile;
	/**
	 * 콘솔(standard out) 출력여부
	 * */
	private boolean		printConsole;
	/**
	 * 로그 파일 최대 크기
	 * */
	private long		maxFileSize;
	/**
	 * debug log 활성화여부
	 * */
	private boolean		debug;
	/**
	 * info log 활성화여부
	 * */
	private boolean		info;
	/**
	 * warn log 활성화여부
	 * */
	private boolean		warn;
	/**
	 * error log 활성화여부
	 * */
	private boolean		error;
	/**
	 * 로그파일 생성 기준 디렉토리
	 * */
	private String		baseDir;
	/**
	 * 콘솔 인코딩
	 * */
	private String		consoleEncoding;
	
	/**<pre>
	 * 서비스명 지정
	 * </pre>
	 * @param serviceName String
	 * */
	public void setServiceName(String serviceName){
		this.serviceName	= serviceName;
	} 
	/**<pre>
	 * 파일작성여부 지정 (문자열 true,false)
	 * </pre>
	 * @param writeFile String
	 * */
	public void setWriteFile(String writeFile){
		if( writeFile==null || "".equals(writeFile.trim()) )
			writeFile	= "false";
		this.writeFile	= new Boolean(writeFile).booleanValue();
	}
	/**<pre>
	 * 콘솔(standard out)출력여부 지정 (문자열 true,false)
	 * </pre>
	 * @param printConsole String
	 * */
	public void setPrintConsole(String printConsole){
		if( printConsole==null || "".equals(printConsole.trim()) )
			printConsole	= "false";
		this.printConsole	= new Boolean(printConsole).booleanValue();
	}
	/**<pre>
	 * 로그파일 최대 크기 지정 (단위:byte)
	 * </pre>
	 * @param maxFileSize String
	 * */
	public void setMaxFileSize(String maxFileSize){
		if( maxFileSize==null || "".equals(maxFileSize.trim()) )
			maxFileSize	= "0";
		try{
			this.maxFileSize	= (long) Calculator.calculate(maxFileSize);
		}catch(Exception e){
			maxFileSize	= "0";
		}
	}
	/**<pre>
	 * debug log 사용여부 지정 (true/false)
	 * </pre>
	 * @param debug String
	 * */
	public void setDebug(String debug){
		if( debug==null || "".equals(debug.trim()) )
			debug	= "false";
		this.debug	= new Boolean(debug).booleanValue();
	}
	/**<pre>
	 * info log 사용여부 지정 (true/false)
	 * </pre>
	 * @param info String
	 * */
	public void setInfo(String info){
		if( info==null || "".equals(info.trim()) )
			info	= "true";
		this.info	= new Boolean(info).booleanValue();
	}
	/**<pre>
	 * warn log 사용여부 지정 (true/false)
	 * </pre>
	 * @param warn String
	 * */
	public void setWarn(String warn){
		if( warn==null || "".equals(warn.trim()) )
			warn	= "true";
		this.warn	= new Boolean(warn).booleanValue();
	}
	/**<pre>
	 * error log 사용여부 지정 (true/false)
	 * </pre>
	 * @param error String
	 * */
	public void setError(String error){
		if( error==null || "".equals(error.trim()) )
			error	= "true";
		this.error	= new Boolean(error).booleanValue();
	}
	/**<pre>
	 * 로그파일 생성 기본경로 지정
	 * </pre>
	 * @param baseDir String
	 * */
	public void setBaseDir(String baseDir){
		this.baseDir	= baseDir;
	}
	/**<pre>
	 * 콘솔 인코딩
	 * </pre>
	 * @param consoleEncoding String
	 * */
	public void setConsoleEncoding(String consoleEncoding){
		this.consoleEncoding	= consoleEncoding;
	}
	
	
	/**<pre>
	 * 로그 서비스명 반환
	 * </pre>
	 * @return String
	 * */
	public String getServiceName(){
		if(serviceName==null || "".equals(serviceName))
			return LogFactory._DEFAULT_LOG_PROPERTY_PREFIX;
		return serviceName;
	}
	/**<pre>
	 * 파일작성여부 반환
	 * </pre>
	 * @return boolean
	 * */
	public boolean getWriteFile(){
		return writeFile;
	}
	/**<pre>
	 * 콘솔(standard out) 출력여부 반환
	 * </pre>
	 * @return boolean
	 * */
	public boolean getPrintConsole(){
		return printConsole;
	}
	/**<pre>
	 * 파일생성 기본디렉토리 반환
	 * $날짜형식(java.text.SimpleDateFormat)$의 문자열을 실제 날짜로 변환하여 반환한다.
	 * </pre>
	 * @return String
	 * @throws PluginException 
	 * */
	public String getBaseDir() throws Exception{
		String dirPath	= baseDir;

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
		return dirPath;
	}
	/**<pre>
	 * 콘솔인코딩
	 * </pre>
	 * @return String
	 * */
	public String getConsoleEncoding() throws Exception{
		return consoleEncoding;
	}	
	/**<pre>
	 * 로그파일 최대 크기 반환
	 * </pre>
	 * @return long
	 * */
	public long getMaxFileSize(){
		return maxFileSize;
	}
	/**<pre>
	 * debug log 작성 여부 반환
	 * </pre>
	 * @return boolean
	 * */
	public boolean debug(){
		return debug;
	}
	/**<pre>
	 * info log 작성 여부 반환
	 * </pre>
	 * @return boolean
	 * */
	public boolean info(){
		return info;
	}
	/**<pre>
	 * warn log 작성 여부 반환
	 * </pre>
	 * @return boolean
	 * */
	public boolean warn(){
		return warn;
	}
	/**<pre>
	 * error log 작성 여부 반환
	 * </pre>
	 * @return boolean
	 * */
	public boolean error(){
		return error;
	}
}