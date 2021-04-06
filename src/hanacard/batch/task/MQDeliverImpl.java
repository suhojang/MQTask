package hanacard.batch.task;

import hanacard.batch.struct.MQStruct;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import com.kwic.common.log.LogFactory;
import com.kwic.common.log.Logger;
import com.kwic.config.Config;
import com.kwic.exception.DefinedException;
import com.kwic.math.Calculator;
import com.kwic.support.Crypto;
import com.kwic.telegram.tcp.JTcpManager;
import com.kwic.util.StringUtil;
import com.kwic.xml.parser.JXParser;

/**
 * deliver MQ request and response 
 * */
public abstract class MQDeliverImpl{
	protected Logger		logger	= LogFactory.getLogger("app");
	
	private static int seq	= 0;
	protected Config config;
	protected Map<String,String> dataMap;
	protected JXParser struct;
	protected JXParser responseXml;
	protected byte[] requestBytes;
	protected byte[] responseBytes;
	protected String rtnCode;
	protected boolean	isError;
	protected int threadNo;
	
	public MQDeliverImpl(int threadNo, Map<String,String> dataMap,byte[] requestBytes) throws Exception{
		try{
			this.threadNo	= threadNo;
			this.dataMap		= dataMap;
			this.requestBytes	= requestBytes;
			responseBytes		= new byte[requestBytes.length];
			System.arraycopy(requestBytes,0,responseBytes,0,requestBytes.length);
			config				= Config.getInstance();
			struct				= new JXParser(new File(config.getProperty("agent.struct.folder")+"/"+dataMap.get(config.getProperty("mq.request.code"))+".xml"));
			rtnCode				= config.getProperty("mq.returncode.normal");
		}catch(Exception e){
			logger.error("[error requestBytes] "+requestBytes);
			logger.error(e);
			e.printStackTrace();
		}
		
	}
	
	public void run(){
		String request	= null;
		String xml		= null;
		byte[] bytes	= null;
		try{
			try{
				//1. agent 요청메시지 생성
				try{
					request	= exec();
					request	= StringUtil.replace(request,"</INPUT>", "<TRANSKEY Value=\""+getTranseKey()+"\"/></INPUT>");
					
					logger.debug("[Thread_"+threadNo+"] ========== request xml ================");
					logger.debug(request);
				}catch(Exception e){
					logger.error(e);
					throw new DefinedException("Agent 요청전문 생성 중 오류가 발생하였습니다.");
				}
				//2. agent 요청메시지 송신 및 응답 수신
				try{
					xml		= deliver(request.getBytes(config.getProperty("agent.encoding")));
					logger.debug("[Thread_"+threadNo+"] ========== response xml ================");
					logger.debug(xml);
				}catch(Exception e){
					logger.error(e);
					throw new DefinedException("Agent에 연결할 수 없습니다.");
				}
				//3. agent 응답메시지 분석
				try{
					responseXml	= new JXParser(xml);
					response(responseXml);
				}catch(Exception e){
					logger.error(e);
					throw new DefinedException("Agent 응답전문 처리 중 오류가 발생하였습니다.",e);
				}
				
			}catch(Exception e){
				logger.error(e);
				isError	= true;
				//MQ 오류처리
				dataMap.put("RPC"		, config.getProperty("mq.RPC.ERR"));
				dataMap.put("RNM_CYN"	, config.getProperty("mq.RNM_CYN.NO"));
				dataMap.put("FILLER"	, e instanceof DefinedException ? getErrMessage(e.getMessage()):"처리 중 오류가 발생하였습니다.");
			}
			//Damo Encrypt
			if (!"29".equals(dataMap.get(config.getProperty("mq.request.code")))) {	//휴폐업 조회는 예외처리
				dataMap	= MQStruct.getInstance().getDamoEncryptDataMap(config, dataMap);
				logger.debug("[Damo Encrypt Data] " + dataMap);
			}
			
			//4. MQ 응답 전문 생성
			bytes	= MQStruct.getInstance().getResponseDatas(dataMap);
			logger.debug("[Thread_"+threadNo+"] response data string ["+new String(bytes)+"]");
			
			if(responseBytes.length>Integer.parseInt(config.getProperty("mq.data.length")))
				System.arraycopy(bytes, 0, responseBytes, Integer.parseInt(config.getProperty("mq.data.startindex")), Integer.parseInt(config.getProperty("mq.data.length")));
			
			else if(requestBytes.length<Integer.parseInt(config.getProperty("mq.data.length"))+Integer.parseInt(config.getProperty("mq.data.startindex")))
				throw new DefinedException("응답 전문의 크기가 정의된 ["+config.getProperty("mq.data.startindex")+"+"+config.getProperty("mq.data.length")+"] 보다 작습니다.");
			
			else
				responseBytes	= bytes;
			
		}catch(Exception e){
			logger.error(e);
			isError	= true;
			/*프로그램 오류 시 MQ 응답 처리*/
			rtnCode	= config.getProperty("mq.returncode.error");
		}
	}
	public String getRtnCode(){
		return rtnCode;
	}
	public boolean getIsError(){
		return isError;
	}
	public byte[] getResponseBytes(){
		return responseBytes;
	}
	//이전 버젼
	public String deliver2(byte[] bytes) throws Exception{
		byte[] response	= JTcpManager.getInstance().sendMessage(config.getProperty("agent.ip"), Integer.parseInt(config.getProperty("agent.port")), bytes,true,(int)Calculator.calculate(config.getProperty("agent.timeout")));
		return new String(response, config.getProperty("agent.encoding"));
	}
	//신규버젼
	public String deliver(byte[] bytes) throws Exception{
		byte[] rBodys	= null;
		try{
			int lenSize	= 10;
			//전문길이 10바이트 추가
			StringBuffer sb	= new StringBuffer();
			String len	= String.valueOf(bytes.length+lenSize);
			for(int i=0;i<lenSize-len.length();i++)
				sb.append("0");
			sb.append(len);
			byte[] hBytes	= sb.toString().getBytes();
			byte[] tBytes	= new byte[hBytes.length+bytes.length];
			System.arraycopy( hBytes, 0, tBytes, 0,hBytes.length);
			System.arraycopy( bytes, 0, tBytes, hBytes.length,bytes.length);
			
			byte[] response	= null;
			//connection error 발생 시 2회 재시도
			int retryCnt	= 3;
			for (int i = 0; i < retryCnt; i++) {
				try {
					response	= send(config, tBytes, lenSize);
					break;
				} catch (Exception e) {
					//오류 발생 시 1초 대기
					if (retryCnt != (i+1)) {
						logger.error("재시도 횟수 : ["+(i+1)+" 회] 1초 대기 후 connection 재시도 합니다. ===========================================>");
						Thread.sleep(1 * 1000);
					}
				}
			}
			/*
			byte[] response	= JTcpManager.getInstance().sendMessageWithoutEOF(
					config.getProperty("agent.ip")
					,Integer.parseInt(config.getProperty("agent.port"))
					,(int)Calculator.calculate(config.getProperty("agent.timeout"))
					,null
					,tBytes
					,lenSize,0,lenSize,JTcpManager._LENGTH_TYPE_FULLSIZE,true);*/
			//길이 10바이트 제거
			//byte[] rBodys	= new byte[response.length-lenSize];
			rBodys	= new byte[response.length-lenSize];
			System.arraycopy( response, lenSize, rBodys, 0,response.length-lenSize);
		}catch(Exception e){
			logger.error(e);
		}
		
		return new String(rBodys, config.getProperty("agent.encoding"));
	}
	
	private byte[] send(Config config, byte[] tBytes, int lenSize) throws Exception{
		byte[] response	= JTcpManager.getInstance().sendMessageWithoutEOF(
				config.getProperty("agent.ip")
				,Integer.parseInt(config.getProperty("agent.port"))
				,(int)Calculator.calculate(config.getProperty("agent.timeout"))
				,null
				,tBytes
				,lenSize,0,lenSize,JTcpManager._LENGTH_TYPE_FULLSIZE,true);
		
		return response;
	}
	
	public String getErrMessage(String msg){
		if(msg==null)
			return "";
		int size	= Integer.parseInt(config.getProperty("mq.MSG.length"));
		if(size>=msg.getBytes().length)
			return msg;
		
		byte[] bytes	= new byte[size];
		System.arraycopy(msg.getBytes(), 0, bytes, 0, size);
		
		return new String(bytes);
	}
	
	public String getParam(String fieldName, String val) throws Exception{
		if((","+config.getProperty("encrypt.fileds")+",").indexOf(","+fieldName+",")>=0)
			val	= new String(Crypto.encryptBytes(val, config.getProperty("encrypt.key"), config.getProperty("encrypt.encoding")));
		return val;
	}
	
	/**
	 * 요청 송신 및 응답 수신
	 * */
	public abstract String exec() throws Exception;
	/**
	 * 수신 응답 메시지 분석 후 정상처리여부,진위여부,응답메시지 추출
	 * */
	public abstract void response(JXParser response) throws Exception;

	public synchronized static final String getTranseKey(){
		SimpleDateFormat sf	= new SimpleDateFormat("HHmmssSSS");
		StringBuffer sb	= new StringBuffer();
		sb.append(sf.format(Calendar.getInstance().getTime()));
		
		if(seq>=9999999)
			seq	= 0;
		String sq	= String.valueOf(++seq);
		for(int i=0;i<(11-sq.length());i++)
			sb.append("0");
		
		sb.append(sq);
		
		return sb.toString();
	}
}
