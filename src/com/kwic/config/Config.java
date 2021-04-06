package com.kwic.config;

import com.kwic.common.util.CProperties;

public class Config {
	private static Config instance;
	CProperties config;
	
	private Config(CProperties config){
		this.config = config;
	}
	public static Config getInstance(CProperties config){
		synchronized(Config.class){
			if(instance==null){
				instance	= new Config(config);
			}
			return instance;
		}
	}
	public static Config getInstance() throws Exception{
		synchronized(Config.class){
			if(instance==null){
				throw new Exception("Config is not initialized.");
			}
			return instance;
		}
	}
	
	public String getProperty(String key){
		return config.getProperty(key);
	}
}
