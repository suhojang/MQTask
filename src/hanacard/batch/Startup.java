package hanacard.batch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.kwic.common.log.LogFactory;
import com.kwic.common.log.Logger;
import com.kwic.common.util.CProperties;
import com.kwic.config.Config;
import com.kwic.exception.DefinedException;
import com.kwic.math.Calculator;

import hanacard.batch.task.MQTask;

/**
 * Control startup ,shutdown
 * Call gabage collector
 * 
 * */
public class Startup extends Thread{
	private Logger		logger			= LogFactory.getLogger("app");
	private Logger		statisticLogger	= LogFactory.getLogger("statistic");
	
	private CProperties	config;
	private File startFile;
	private List<MQTask> jobs;
	
	private static boolean restart;
	private static long restartTerm;
	
	public Startup() throws Exception{
		System.setProperty("Logger","app");
		logger.info("\n\n\n== Try to startup batch. ==");
		try{
			loadProperties();
			if ("ON".equals(config.getProperty("restart.power"))){
				logger.info("========= 재시작 스케쥴러를 시작 합니다. =========");
				restartProcessChecker(logger);
			}
			
			startFile	= new File(config.getProperty("marking.path"),"START");
			if(!startFile.getParentFile().exists())
				startFile.getParentFile().mkdirs();

			//구동중인지 확인
			if(startFile.exists() && (startFile.lastModified()+2000)>Calendar.getInstance().getTimeInMillis())
				throw new DefinedException("Batch aready started. An attempt will be ignored.");
			
			//구동중 임시파일 생성 
			startFile.createNewFile();
			startFile.deleteOnExit();//시스템 종료시 구동파일 삭제
			
			//종료파일이 있다면 삭제 후 무시 
			if(new File(config.getProperty("marking.path"),"STOP").exists())
				new File(config.getProperty("marking.path"),"STOP").delete();
			
			jobs	= new Vector<MQTask>();
			
		}catch(Exception e){
			logger.error(e);
			throw e;
		}
	}
	
	private void loadProperties() throws Exception{
		config		= new CProperties(true);
		
		String configPath	= System.getProperty("batch.properties.path");
		if(configPath==null || !new File(configPath).exists())
			configPath	= "../config/batch.properties";
		if(!new File(configPath).exists())
			configPath	= "./config/batch.properties";

		logger.info("Batch properties path = ["+configPath+"]");
		
		((CProperties)config).loadProperties(configPath);
		
//		logger.info("=============== Batch properties ===============");
//		logger.info(config.toString());
//		logger.info("=================================================");
		
		Config.getInstance(config);
		
		logger.info("Batch properties load successfully. --");
	}
	
	public void run(){
		MQTask	task	= null;
		try{
			long sleepTerm	= 1000L;
			long callGcTerm	= (long)(Calculator.calculate(config.getProperty("call.gc.interval"))/sleepTerm);
			long statisticTerm	= (long)(Calculator.calculate(config.getProperty("statistic.interval"))/sleepTerm);
			
			restartTerm	= (long) (Calculator.calculate(config.getProperty("restart.interval"))/sleepTerm);
			
			for(int i=1;i<=Integer.parseInt(config.getProperty("max.thread"));i++){
				//MQ작업 시작
				task	= new MQTask(i,jobs);
				task.start();
				jobs.add(task);
			}
			
			long gcturn	= 0;
			long tmpturn	= 0;
			//shutdown이 실행되었다면 종료
			while(!isShutDown()){
				try{
					if(callGcTerm>0)
						gcturn++;
					if(statisticTerm>0)
						tmpturn++;
					
					startFile.setLastModified(Calendar.getInstance().getTimeInMillis());
					
					//통계정보 작성
					if(statisticTerm>0 && tmpturn%statisticTerm==0){
						for(int i=0;i<jobs.size();i++){
							statisticLogger.debug(jobs.get(i).getStatistic());
						}
						tmpturn	= 0;
					}
					//gabage collector 호출
					if(callGcTerm>0 && gcturn%(callGcTerm)==0){
						System.gc();
						gcturn	= 0;
					}
					
					Thread.sleep(sleepTerm);
				}catch(Exception ex){
					logger.error(ex);
				}
			}
		}catch(Exception e){
			logger.error(e);
		}finally{
		}
		logger.info("Waiting for terminating threads.");
		while(jobs.size()>0)
			try{Thread.sleep(1000);}catch(Exception e){}
		if (!restart)
			System.exit(0);
		
		restart	= false;
	}
	
	public boolean isShutDown(){
		if(new File(config.getProperty("marking.path"),"STOP").exists()){
			new File(config.getProperty("marking.path"),"STOP").delete();
			
			logger.info("Terminat batch jobs .");

			for(int i=0;i<jobs.size();i++){
				//MQ 작업 종료
				jobs.get(i).shutdown();
			}
			return true;
		}
		return false;
	}
	
	public void restartProcessChecker(final Logger logger) throws Exception {
		final Map<String,Long> proc	= new HashMap<String,Long>();
		
		long initialDelay	= 0;
		long period			= 1;
		TimeUnit unit		= TimeUnit.SECONDS;
		
		final SimpleDateFormat sdf				= new SimpleDateFormat("yyyyMMddkkmmss");
		final ScheduledThreadPoolExecutor exec	= new ScheduledThreadPoolExecutor(1);
		
		final long sleepTerm	= 10 * 1000L;
		
		try {
			//requestBytes receive initial time set
			proc.put("initialTime", 0L);
			
			exec.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					byte[] requestBytes	= null;
					try {
						requestBytes	= MQTask.getRequestBytes();
						if(requestBytes==null || requestBytes.length==0) {
							if (proc.get("initialTime") > 0) {
								Date initialTime	= sdf.parse(String.valueOf(proc.get("initialTime")));
								Date current		= sdf.parse(sdf.format(Calendar.getInstance().getTime()));
								
								long diffTerm	= (current.getTime() - initialTime.getTime()) / 1000;
								if (diffTerm >= restartTerm) {
									logger.error("================= 데이터를 "+(restartTerm / 60)+"분 동안 받지 못해 재시작 합니다. ==================");
									restart	= true;
									
									try {
										logger.error("================= 어플리케이션을 종료 합니다. ==================");
										exec.shutdown();
										new Shutdown().start();
									} catch (Exception e) {
										logger.error(e);
									} finally {
										try {
											logger.error("================= 어플리케이션을 "+(sleepTerm / 1000)+" second 후 재시작 합니다. ==================");
											Thread.sleep(sleepTerm);
											new Startup().start();
											
											proc.put("initialTime", 0L);
										} catch (Exception e) {
											logger.error(e);
										}
									}
								}
							}
							else
								proc.put("initialTime", Long.parseLong(sdf.format(Calendar.getInstance().getTime())));
							
						} else {
							proc.put("initialTime", 0L);
						}
						
					} catch (Exception e) {
						logger.error(e);
					}
				}
			}, 
			initialDelay, 
			period, 
			unit);
			
		} catch (Exception e) {
			logger.error(e);
		} finally {
		}
	}
	
	public static void main(String[] args) throws Exception{
		new Startup().start();
	}
}
