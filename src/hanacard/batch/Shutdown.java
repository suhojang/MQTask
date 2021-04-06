package hanacard.batch;

import java.io.File;

import com.kwic.common.log.LogFactory;
import com.kwic.common.log.Logger;
import com.kwic.common.util.CProperties;
import com.kwic.config.Config;

public class Shutdown extends Thread{
	private Logger		logger	= LogFactory.getLogger("shutdown");
	
	private CProperties	config;
	
	public Shutdown() throws Exception{
		logger.info("\n\n\n== Try to shutdown batch. ==");
		System.setProperty("Logger","shutdown");
		loadProperties();
	}
	
	public void run(){
		try{
			if(!new File(config.getProperty("marking.path")).exists())
				new File(config.getProperty("marking.path")).mkdirs();
			new File(config.getProperty("marking.path"),"STOP").createNewFile();
			
		}catch(Exception e){
			logger.error(e);
		}finally{
		}
	}
	
	private void loadProperties() throws Exception{
		config		= new CProperties(true);
		
		String configPath	= System.getProperty("batch.properties.path");
		if(configPath==null || !new File(configPath).exists())
			configPath	= "../config/batch.properties";
		if(!new File(configPath).exists())
			configPath	= "./config/batch.properties";

		logger.info("Client properties path = ["+configPath+"]");
		
		((CProperties)config).loadProperties(configPath);
		
//		logger.info("=============== Batch properties ===============");
//		logger.info(config.toString());
//		logger.info("=================================================");
		
		Config.getInstance(config);
		
		logger.info("Batch properties load successfully. --");
	}
	
	
	public static void main(String[] args) throws Exception{
		new Shutdown().start();
	}
}
