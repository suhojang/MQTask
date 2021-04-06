package hanacard.batch.struct;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;

import com.kwic.common.log.LogFactory;
import com.kwic.common.log.Logger;
import com.kwic.config.Config;
import com.kwic.xml.parser.JXParser;
import com.penta.scpdb.ScpDbAgent;

public class MQStruct {
	private Logger		logger	= LogFactory.getLogger("app");
	
	private static MQStruct instance;
	private ScpDbAgent agent;
	private JXParser struct;
	
	private MQStruct() throws Exception{
		agent	= new ScpDbAgent();
		struct	= new JXParser(new File(Config.getInstance().getProperty("mq.struct.path")));
	}
	
	public static MQStruct getInstance() throws Exception{
		synchronized(MQStruct.class){
			if(instance==null){
				instance	= new MQStruct();
			}
			return instance;
		}
	}
	
	public Map<String,String> getRequestDatas(byte[] rcvBytes) throws Exception{
		Map<String,String> dataMap	= new HashMap<String,String>();
		
		Element[] params	= struct.getChilds(struct.getRootElement());
		
		int idx		= 0;
		int size	= -1;
		byte[] bytes	= null;
		
		for(Element param:params){
			size	= Integer.parseInt(struct.getAttribute(param, "size"));
			
			bytes	= new byte[size];
			System.arraycopy(rcvBytes, idx, bytes, 0, size);

			idx	+= size;
			
			dataMap.put(struct.getAttribute(param, "name"), removeXSS(new String(bytes).trim()));
			
			logger.debug(struct.getAttribute(param, "name")+" : ["+removeXSS(new String(bytes).trim())+"]");			
		}
		
		return dataMap;
	}

	public byte[] getResponseDatas(Map<String,String> dataMap) throws Exception{
		
		Element[] params	= struct.getChilds(struct.getRootElement());
		StringBuffer sb	= new StringBuffer();
		int size	= -1;
		String val	= null;
		for(Element param:params){
			size	= Integer.parseInt(struct.getAttribute(param, "size"));
			
			val		= dataMap.get(struct.getAttribute(param, "name"));
			val		= appendSpace(val,size);
			
			sb.append(val);
			
			logger.debug(struct.getAttribute(param, "name")+" : ["+val+"]");			
		}
		
		return sb.toString().getBytes(Config.getInstance().getProperty("mq.encoding"));
	}

	public Map<String,String> getDamoEncryptDataMap(Config config, Map<String,String> dataMap){
		/*Iterator<String> iter	= dataMap.keySet().iterator();
		while (iter.hasNext()) {
			String key	= iter.next();
			if((","+config.getProperty("damo.encrypt.fileds")+",").indexOf(","+key+",")>=0)
				dataMap.put(key, agent.ScpEncB64(config.getProperty("damo.iniFilePath"), config.getProperty("damo.iniKeyName"), dataMap.get(key)));
		}
*/
		return dataMap;
	}
	
	public Map<String,String> getDamoDecryptDataMap(Config config, Map<String,String> dataMap){
		/*Iterator<String> iter	= dataMap.keySet().iterator();
		while (iter.hasNext()) {
			String key	= iter.next();
			if((","+config.getProperty("damo.encrypt.fileds")+",").indexOf(","+key+",")>=0)
				dataMap.put(key, agent.ScpDecB64(config.getProperty("damo.iniFilePath"), config.getProperty("damo.iniKeyName"), dataMap.get(key)));
		}
*/
		return dataMap;
	}	
	
	
	public String removeXSS(String str) {
        str = str.replaceAll("\"","&quot;");
        str = str.replaceAll("&", "&amp;");
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll(">", "&gt;");
        str = str.replaceAll("%00", null);
        str = str.replaceAll("\"", "&#34;");
        str = str.replaceAll("\'", "&#39;");
        str = str.replaceAll("%", "&#37;");    
        str = str.replaceAll("../", "");
        str = str.replaceAll("..\\\\", "");
        str = str.replaceAll("./", "");
        str = str.replaceAll("%2F", "");
        
	    return str;
	}
	
	public String appendSpace(String val,int size) throws Exception{
		StringBuffer sb	= new StringBuffer();
		sb.append(val);
		int len	= val.getBytes(Config.getInstance().getProperty("mq.encoding")).length;
		if(len>size){
			byte[] bytes	= new byte[size];
			System.arraycopy(val.getBytes(Config.getInstance().getProperty("mq.encoding")),0,bytes,0,size);
			return new String(bytes);
		}else{
			for(int i=len+1;i<=size;i++){
				sb.append(" ");
			}
		}
		return sb.toString();
	}
}
