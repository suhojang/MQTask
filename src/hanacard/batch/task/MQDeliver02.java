package hanacard.batch.task;

import java.util.Map;

import com.kwic.xml.parser.JXParser;

/**
 * 운전면허증 진위여부
 * HS_VD_PHC=02
 * SPECIALCODE=0004
 * */
public class MQDeliver02 extends MQDeliverImpl {

	public MQDeliver02(int threadNo, Map<String, String> dataMap,byte[] requestBytes) throws Exception{
		super(threadNo,dataMap,requestBytes);
	}
	
	@Override
	public String exec() throws Exception {
		/*try {*/
			struct.setAttribute("//SEEDKEY"			, "Value", config.getProperty("encrypt.key")		);
			struct.setAttribute("//SPECIALCODE"		, "Value", config.getProperty("02.SPECIALCODE")		);
			struct.setAttribute("//MODULE"			, "Value", config.getProperty("02.MODULE")		);
			struct.setAttribute("//CERTKEY"			, "Value", config.getProperty("02.CERTKEY")			);
			struct.setAttribute("//DEPARTMENTCODE"	, "Value", config.getProperty("02.DEPARTMENTCODE")	);
			struct.setAttribute("//JUMIN"			, "Value", getParam("JUMIN", dataMap.get("RRNO").substring(0,6))	);
			struct.setAttribute("//NAME"			, "Value", getParam("NAME", dataMap.get("PN"))		);
			struct.setAttribute("//LNCAREANUMBER"	, "Value", getParam("LNCAREANUMBER", dataMap.get("IDF_ISU_BUR_NM"))	);
			struct.setAttribute("//LNCYEARNUMBER"	, "Value", getParam("LNCYEARNUMBER", dataMap.get("IDF_NO").substring(0,2))	);
			struct.setAttribute("//LNCSERIALNUMBER"	, "Value", getParam("LNCSERIALNUMBER", dataMap.get("IDF_NO").substring(2,8))	);
			struct.setAttribute("//LNCNUMBER"		, "Value", getParam("LNCNUMBER", dataMap.get("IDF_NO").substring(8,10))	);
			struct.setAttribute("//USERIP"			, "Value", getParam("USERIP", dataMap.get("IPV6_ADR"))	);
			struct.setAttribute("//USERPORT"		, "Value", config.getProperty("02.USERPORT")	);
			
		/*} catch (Exception e) {
			logger.error(e);
			throw e;
		}*/

		return struct.toString(null);
	}

	@Override
	public void response(JXParser response) throws Exception{
		String val	= response.getAttribute("//INQNORMALPE", "VALUE");
		
		if(!config.getProperty("02.INQNORMALPE.YES").equals(val.trim())){
			dataMap.put("RPC"		, config.getProperty("mq.RPC.ERR"));
			dataMap.put("RNM_CYN"	, config.getProperty("mq.RNM_CYN.NO"));
			dataMap.put("FILLER"	, getErrMessage(response.getAttribute("//ERRMSG", "VALUE")));
			return;
		}

		val	= response.getAttribute("//LNCAGREEMENTYN", "VALUE");
		if(!config.getProperty("02.LNCAGREEMENTYN.YES").equals(val)){
			dataMap.put("RPC"		, config.getProperty("mq.RPC.SUC"));
			dataMap.put("RNM_CYN"	, config.getProperty("mq.RNM_CYN.NO"));
			dataMap.put("FILLER"	, getErrMessage(response.getAttribute("//ERRMSG", "VALUE")));
			return;
		}
		
		dataMap.put("RPC"		, config.getProperty("mq.RPC.SUC"));
		dataMap.put("RNM_CYN"	, config.getProperty("mq.RNM_CYN.YES"));
		dataMap.put("FILLER"	, "");
	}
}
