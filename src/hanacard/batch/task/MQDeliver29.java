package hanacard.batch.task;

import java.util.Map;

import org.dom4j.Element;

import com.kwic.config.Config;
import com.kwic.xml.parser.JXParser;

/**
 * 휴쳬업조회
 * HS_VD_PHC=29
 * SPECIALCODE=0063
 * */
public class MQDeliver29 extends MQDeliverImpl {

	public MQDeliver29(int threadNo, Map<String, String> dataMap,byte[] requestBytes) throws Exception{
		super(threadNo,dataMap,requestBytes);
	}
	
	@Override
	public String exec() throws Exception {
		struct.setAttribute("//SPECIALCODE"		, "Value", config.getProperty("29.SPECIALCODE")		);
		struct.setAttribute("//MODULE"			, "Value", config.getProperty("29.MODULE")		);
		struct.setAttribute("//CERTKEY"			, "Value", config.getProperty("29.CERTKEY")			);
		struct.setAttribute("//DEPARTMENTCODE"	, "Value", config.getProperty("29.DEPARTMENTCODE")	);
		struct.setAttribute("//REGNUMBER"		, "Value", dataMap.get("IDF_NO")	);
		struct.setAttribute("//SFTM"			, "Value", config.getProperty("29.SFTM")	);
		struct.setAttribute("//USERIP"			, "Value", getParam("USERIP", dataMap.get("IPV6_ADR"))	);
		struct.setAttribute("//USERPORT"		, "Value", config.getProperty("29.USERPORT")	);
		
		return struct.toString(null);
	}

	@Override
	public void response(JXParser response) throws Exception{
		String val	= null;
		
		Element INQNORMALPE	= response.getElement("//INQNORMALPE");
		Element CORPSTATE	= response.getElement("//CORPSTATE");
		Element ERRMSG		= response.getElement("//ERRMSG");
		
		if (INQNORMALPE==null || CORPSTATE==null) {
			dataMap.put("RPC"		, config.getProperty("mq.RPC.ERR"));
			dataMap.put("RNM_CYN"	, "9");	//기타
			dataMap.put("FILLER"	, ERRMSG==null ? "" : getErrMessage(response.getAttribute("//ERRMSG", "VALUE")));
			return;
		}
		
		val	= response.getAttribute("//INQNORMALPE", "VALUE");
		if(!config.getProperty("29.INQNORMALPE.YES").equals(val.trim())){
			dataMap.put("RPC"		, config.getProperty("mq.RPC.ERR"));
			dataMap.put("RNM_CYN"	, response.getAttribute("//CORPSTATE", "VALUE")==null?"":response.getAttribute("//CORPSTATE", "VALUE"));
			dataMap.put("FILLER"	, ERRMSG==null ? "" : getErrMessage(response.getAttribute("//ERRMSG", "VALUE")));
			return;
		}
		
		val	= response.getAttribute("//CORPSTATE", "VALUE");
		if( (val==null || "".equals(val)) && response.getAttribute("//ERRMSG", "VALUE")!=null && !"".equals(response.getAttribute("//ERRMSG", "VALUE"))){
			dataMap.put("RPC"		, config.getProperty("mq.RPC.ERR"));
			dataMap.put("RNM_CYN"	, response.getAttribute("//CORPSTATE", "VALUE")==null?"":response.getAttribute("//CORPSTATE", "VALUE"));
			dataMap.put("FILLER"	, ERRMSG==null ? "" : getErrMessage(response.getAttribute("//ERRMSG", "VALUE")));
			return;
		}
		
		StringBuffer sb	= new StringBuffer();
		
		String TAXKIND		= response.getElement("//TAXKIND")==null?"":response.getAttribute("//TAXKIND", 	"VALUE");
		String TAXDATE		= response.getElement("//TAXDATE")==null?"":response.getAttribute("//TAXDATE", 	"VALUE");
		String INFODATE		= response.getElement("//INFODATE")==null?"":response.getAttribute("//INFODATE", "VALUE");
		String CHANGEDATE	= response.getElement("//CHANGEDATE")==null?"":response.getAttribute("//CHANGEDATE", 	"VALUE");
		
		sb.append(appendSpace(TAXKIND, 1));
		sb.append(appendSpace(TAXDATE, 8));
		sb.append(appendSpace("".equals(INFODATE)?new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()):INFODATE, 8));
		sb.append(appendSpace(CHANGEDATE, 8));
		
		//과세유형(1) + 휴폐업일자(8) + 정보기준일자(8) + 전환일자(8)
		dataMap.put("IDF_ISU_BUR_NM", sb.toString());
		dataMap.put("RNM_CYN"	, response.getAttribute("//CORPSTATE", "VALUE"));
		dataMap.put("RPC"		, config.getProperty("mq.RPC.SUC"));
		dataMap.put("FILLER"	,"");
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
