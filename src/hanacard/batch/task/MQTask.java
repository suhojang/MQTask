package hanacard.batch.task;

import hanacard.batch.struct.MQStruct;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.kwic.common.log.LogFactory;
import com.kwic.common.log.Logger;
import com.kwic.config.Config;
import com.kwic.exception.DefinedException;
import com.pnp.eai.entity.EAIMessage;
import com.pnp.eai.service.EaiAPIImpl;

/**
 * Make delivering MQ thread 
 * */
public class MQTask extends Thread{
	
	private Logger		logger	= LogFactory.getLogger("app");
	private boolean		shutdown;
	private Config		config;
	
	private String totalStartTime;
	private long totalCompletedCount;
	private long totalErrorCount;
	private BigDecimal totalCompletedTime;
	private BigDecimal totalErrorTime;
	private long avarageCompletedTime;
	private long avarageErrorTime;
	
	private long intervalTotalCompletedCount;
	private long intervalTotalErrorCount;
	private BigDecimal intervalTotalCompletedTime;
	private BigDecimal intervalTotalErrorTime;
	private long intervalAvarageCompletedTime;
	private long intervalAvarageErrorTime;
	private String intervalStartTime;
	
	private EaiAPIImpl	eaiAPI;
	private boolean eaiInit;
	private SimpleDateFormat sf	= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS");
	private int threadNo;
	private List<MQTask> jobs;
	private String mqHeader;
	private int headerStartIndex;
	
	private static byte[] check_reqBytes;
	
	private static String ERR_REQ_DATA	= "";
	
	public MQTask(int threadNo,List<MQTask> jobs) throws Exception{
		this.threadNo	= threadNo;
		this.jobs = jobs;
		config	= Config.getInstance();
		
		mqHeader	= config.getProperty("mq.header");
		mqHeader	= mqHeader.substring(1,mqHeader.length()-1);
		headerStartIndex	= Integer.parseInt(config.getProperty("mq.header.startindex"));		
		logger.debug("mqHeader length : "+mqHeader.getBytes().length);
		totalStartTime		= sf.format(Calendar.getInstance().getTime());
		intervalStartTime	= sf.format(Calendar.getInstance().getTime());
	}
	
	public void shutdown(){
		shutdown	= true;
	}
	
	public void run(){
		logger.info("[Thread_"+threadNo+"] MQTask started..");
		
		Map<String,String> dataMap		= null;
		
		Class<?>		mqDeliverClass	= null;
		Constructor<?>	mqDeliverCst	= null;
		MQDeliverImpl	mqDeliver		= null;
		byte[]			requestBytes	= null;
		byte[]			dataBytes		= null;
		EAIMessage		recvEAIMsg		= null;
		int				mqDataLength	= Integer.parseInt(config.getProperty("mq.data.length"));
		int				mqDataStIndex	= Integer.parseInt(config.getProperty("mq.data.startindex"));
		String			mqRequestCd		= config.getProperty("mq.request.code");
		String			mqBFHErrCd		= config.getProperty("mq.BFH.errcode");
		String			mqRtnErrCd		= config.getProperty("mq.returncode.error");
		
		long			startTime		= 0;
		try{
			eaiAPI	= new EaiAPIImpl(config.getProperty("mq.eaiapi.param"));
			eaiAPI.MqInit();
			eaiInit	= true;
			logger.info("[Thread_"+threadNo+"] eaiAPI initialized.");
			
			while(!shutdown){
				
				try{Thread.sleep(10);}catch(Exception e){}
				recvEAIMsg	= null;
				requestBytes	= null;
				startTime	= 0;
				dataBytes	= null;
				dataMap		= null;
				mqDeliver	= null;
				mqDeliverCst	= null;
				mqDeliverClass	= null;
				try{
					/*------ MQ request 조회 ----------*/
					recvEAIMsg		= eaiAPI.mqMsgGet();
					if(recvEAIMsg==null)
						continue;
					
					requestBytes	= recvEAIMsg.getUserData();
					setRequestBytes(requestBytes);
					
					if(requestBytes==null || requestBytes.length==0)
						continue;
					
					recvEAIMsg.getBFH().getRecvSvc();
					
					startTime	= Calendar.getInstance().getTimeInMillis();
					
					logger.debug("[Thread_"+threadNo+"] request string ["+new String(requestBytes)+"]");
					
					ERR_REQ_DATA	= new String(requestBytes);
					
					if(requestBytes.length<mqDataLength+mqDataStIndex){
						throw new DefinedException("요청 전문의 byte 크기가 설정에 정의된 ["+mqDataStIndex+"+"+mqDataLength+"] 보다 작습니다.");
					}else if(requestBytes.length>mqDataLength){
						dataBytes	= new byte[mqDataLength];
						System.arraycopy(requestBytes, mqDataStIndex, dataBytes, 0, mqDataLength);
					}else{
						dataBytes	= requestBytes;
					}
					
					logger.debug("[Thread_"+threadNo+"] request data string ["+new String(dataBytes)+"]");
					dataMap	= MQStruct.getInstance().getRequestDatas(dataBytes);
					//Damo Data 복호화(RRNO,IDF_NO)
					if (!"29".equals(dataMap.get(mqRequestCd))) {	//휴폐업 조회는 예외처리
						dataMap	= MQStruct.getInstance().getDamoDecryptDataMap(config, dataMap);
						logger.debug("[Damo Decrypt Data] " + dataMap);
					}
					
					/*MQ 처리 class 생성*/
					mqDeliverClass	= Class.forName("hanacard.batch.task.MQDeliver"+dataMap.get(mqRequestCd));
					mqDeliverCst	= mqDeliverClass.getConstructor(new Class[]{int.class,Map.class,byte[].class});
					mqDeliver		= (MQDeliverImpl) mqDeliverCst.newInstance(new Object[]{threadNo,dataMap,requestBytes});
					mqDeliver.run();
					//정상처리 응답
					mqReply(recvEAIMsg,mqDeliver.getResponseBytes(),mqDeliver.getRtnCode(),startTime,mqDeliver.getIsError());
					
				}catch(Exception e){
					logger.error("[오류 전문 발생  Thread_"+threadNo+"] " + ERR_REQ_DATA);
					logger.error(e);
					
					/*프로그램 오류 시 MQ 응답 처리*/
					if(recvEAIMsg!=null){
						//오류
						recvEAIMsg.getBFH().setResultCode(mqBFHErrCd);
						//오류시 응답
						if (requestBytes==null) {
							requestBytes	= "  010001    0000001      010001    1234                                                                               00000000                                                                                                1000000757                                                                                                                                          00000000                                                                                                                                                                                                                                                                                                                                                                                 00000                                                                      Error request Data".getBytes();
						}
						mqReply(recvEAIMsg,requestBytes,mqRtnErrCd,startTime,true);
					}
				}
			}
			
		}catch(Exception e){
			logger.error(e);
			logger.error("[Thread_"+threadNo+"][Exception mqReply] error requestBytes string ["+new String(requestBytes)+"]");
		}finally{
			logger.error("[Thread_"+threadNo+"][finally] job remove.");
			try {
				Thread.sleep(1 * 1000);
			} catch (Exception e) {
				logger.error(" ============ eaiAPI.MqClose() 함수 호출 오류 발생 ============");
				logger.error(e);
			}

			try {
				if (eaiInit) {
					eaiAPI.MqClose();
				}
				logger.info("[Thread_" + threadNo + "] eaiAPI closed.");
			} catch (Exception ex) {
				logger.error(" ============ eaiAPI.MqClose() 함수 호출 오류 발생 ============");
				logger.error(ex);
			}
			logger.info("[Thread_" + threadNo + "] MQTask terminated.");
			jobs.remove(this);
			logger.error("[Thread_"+threadNo+"] job remove after count : " + jobs.size());
		}
	}
	
	public void mqReply(EAIMessage recvEAIMsg,byte[] responseBytes, String rtnCode,long startTime,boolean isError) throws Exception{
		byte[] header	= null;
		byte[] tBytes	= null;
		
		if (isError)
			logger.error("[Thread_"+threadNo+"][Method mqReply] error response string ["+new String(responseBytes)+"]");
		try{
			header	= mqHeader.getBytes();
			if (responseBytes == null) {
				responseBytes	= ERR_REQ_DATA.getBytes();
			}
			tBytes	= new byte[header.length+responseBytes.length];
			
			System.arraycopy(responseBytes, 0, tBytes, 0,headerStartIndex);
			System.arraycopy(header, 0, tBytes, headerStartIndex, header.length);
			System.arraycopy(responseBytes, headerStartIndex, tBytes, headerStartIndex+header.length, responseBytes.length-headerStartIndex);
		}catch(Exception e){
			throw new Exception("[MQ응답 전문 생성 중 오류 발생] " + responseBytes!=null ? new String(responseBytes) : ERR_REQ_DATA);
		}
		
		if(eaiInit) {
			try{
				eaiAPI.mqReplyPut(recvEAIMsg,tBytes,rtnCode);
			}catch(Exception e){
				logger.error(" ============ eaiAPI.mqReplyPut() 함수 호출 오류 발생 ============");
				logger.error("startTime : "+new java.text.SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(startTime));
				logger.error("byte lenth : "+tBytes.length);
				logger.error("rtnCode : "+rtnCode);
				logger.error(e);
			};
		}
		
		logger.debug("[Thread_"+threadNo+"] response string ["+new String(tBytes)+"]");
		
		try{
			long endTime	= Calendar.getInstance().getTimeInMillis();

			if(isError){
				totalErrorCount++;
				totalErrorTime		= totalErrorCount==1?new BigDecimal(endTime-startTime):(totalErrorTime.add(new BigDecimal(endTime-startTime)));
				avarageErrorTime	= totalErrorTime.divide(new BigDecimal(totalErrorCount),3,BigDecimal.ROUND_HALF_UP).longValue();
				intervalTotalErrorCount++;
				intervalTotalErrorTime		= intervalTotalErrorCount==1?new BigDecimal(endTime-startTime):(intervalTotalErrorTime.add(new BigDecimal(endTime-startTime)));
				intervalAvarageErrorTime	= intervalTotalErrorTime.divide(new BigDecimal(intervalTotalErrorCount),3,BigDecimal.ROUND_HALF_UP).longValue();
			
			}else{
				totalCompletedCount++;
				totalCompletedTime		= totalCompletedCount==1?new BigDecimal(endTime-startTime):(totalCompletedTime.add(new BigDecimal(endTime-startTime)));
				avarageCompletedTime	= totalCompletedTime.divide(new BigDecimal(totalCompletedCount),3,BigDecimal.ROUND_HALF_UP).longValue();
				
				intervalTotalCompletedCount++;
				intervalTotalCompletedTime		= intervalTotalCompletedCount==1?new BigDecimal(endTime-startTime):(intervalTotalCompletedTime.add(new BigDecimal(endTime-startTime)));
				intervalAvarageCompletedTime	= intervalTotalCompletedTime.divide(new BigDecimal(intervalTotalCompletedCount),3,BigDecimal.ROUND_HALF_UP).longValue();
			}
		}catch(Exception e){
			logger.error(e);
		}
	}
	
	public String getStatistic(){
		String intervalEndTime	= sf.format(Calendar.getInstance().getTime());
		
		StringBuffer sb	= new StringBuffer();
		try{
		sb.append(System.getProperty("line.separator"))
		.append("=============== [Thread_"+threadNo+"] Total statictic =======================").append(System.getProperty("line.separator"))
		.append("총 시작시간 = "+totalStartTime).append(System.getProperty("line.separator"))
		.append("총 종료시간 = "+intervalEndTime).append(System.getProperty("line.separator"))
		.append("총 오류 건수 = "+totalErrorCount).append(System.getProperty("line.separator"))
		.append("총 오류 건 평균 처리 시간 = "+avarageErrorTime+" ms").append(System.getProperty("line.separator"))
		.append("총 완료 건수 = "+totalCompletedCount).append(System.getProperty("line.separator"))
		.append("총 완료 건 평균 처리 시간 = "+avarageCompletedTime+" ms").append(System.getProperty("line.separator"))
		.append("-------------- [Thread_"+threadNo+"] Interval statictic ---------------------").append(System.getProperty("line.separator"))
		.append("구간 시작시간 = "+intervalStartTime).append(System.getProperty("line.separator"))
		.append("구간 종료시간 = "+intervalEndTime).append(System.getProperty("line.separator"))
		.append("구간 오류 건수 = "+intervalTotalErrorCount).append(System.getProperty("line.separator"))
		.append("구간 오류 건 평균 처리 시간 = "+intervalAvarageErrorTime+" ms").append(System.getProperty("line.separator"))
		.append("구간 완료 건수 = "+intervalTotalCompletedCount).append(System.getProperty("line.separator"))
		.append("구간 완료 건 평균 처리 시간 = "+intervalAvarageCompletedTime+" ms").append(System.getProperty("line.separator"))
		.append("=============================================================================").append(System.getProperty("line.separator"))
		;
		
		intervalTotalErrorCount			= 0;
		intervalTotalErrorTime			= null;
		intervalAvarageErrorTime		= 0;
		intervalTotalCompletedCount		= 0;
		intervalTotalCompletedTime		= null;
		intervalAvarageCompletedTime	= 0;
		intervalStartTime				= intervalEndTime;
		}catch(Exception e){
			logger.error(e);
		}
		return sb.toString();
	}

	public String appendSpace(String val,int size) throws Exception{
		StringBuffer sb	= new StringBuffer();
		sb.append(val);
		int len	= val.getBytes(Config.getInstance().getProperty("mq.encoding")).length;
		for(int i=len+1;i<=size;i++){
			sb.append(" ");
		}
		return sb.toString();
	}
	
	private void setRequestBytes(byte[] requestBytes){
		check_reqBytes	= requestBytes;
	}
	
	public static byte[] getRequestBytes(){
		return check_reqBytes==null?null:check_reqBytes;
	} 
}
