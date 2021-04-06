package hanacard.batch.task;

import java.util.Map;

import com.kwic.xml.parser.JXParser;

/**
 * 주민등록증 진위여부
 * HS_VD_PHC=01
 * SPECIALCODE=0005
 * */
public class MQDeliver01 extends MQDeliverImpl {
	public MQDeliver01(int threadNo, Map<String, String> dataMap,byte[] requestBytes) throws Exception{
		super(threadNo,dataMap,requestBytes);
	}

	@Override
	public String exec() throws Exception {
		struct.setAttribute("//SEEDKEY"			, "Value", config.getProperty("encrypt.key")		);
		struct.setAttribute("//SPECIALCODE"		, "Value", config.getProperty("01.SPECIALCODE")		);
		struct.setAttribute("//MODULE"			, "Value", config.getProperty("01.MODULE")		);
		struct.setAttribute("//CERTKEY"			, "Value", config.getProperty("01.CERTKEY")			);
		struct.setAttribute("//DEPARTMENTCODE"	, "Value", config.getProperty("01.DEPARTMENTCODE")	);
		struct.setAttribute("//JUMIN"			, "Value", getParam("JUMIN", dataMap.get("RRNO"))	);
		struct.setAttribute("//NAME"			, "Value", getParam("NAME", dataMap.get("PN"))		);
		struct.setAttribute("//ISSUEDATE"		, "Value", getParam("ISSUEDATE", dataMap.get("IDF_NO"))	);
		struct.setAttribute("//USERIP"			, "Value", getParam("USERIP", dataMap.get("IPV6_ADR"))	);
		struct.setAttribute("//USERPORT"		, "Value", config.getProperty("01.USERPORT")	);
		return struct.toString(null);
	}

	/**
	 * 수신 응답 메시지 분석 후 정상처리여부,진위여부,응답메시지 추출
	 * */
	@Override
	public void response(JXParser response) throws Exception{
		String val	= response.getAttribute("//INQNORMALPE", "VALUE");
		
		if(!config.getProperty("01.INQNORMALPE.YES").equals(val.trim())){
			dataMap.put("RPC"		, config.getProperty("mq.RPC.ERR"));
			dataMap.put("RNM_CYN"	, config.getProperty("mq.RNM_CYN.NO"));
			dataMap.put("FILLER"	, getErrMessage(response.getAttribute("//ERRMSG", "VALUE")));
			return;
		}

		val	= response.getAttribute("//AGREEMENTYNE", "VALUE");
		if(!config.getProperty("01.AGREEMENTYNE.YES").equals(val)){
			dataMap.put("RPC"		, config.getProperty("mq.RPC.SUC"));
			dataMap.put("RNM_CYN"	, config.getProperty("mq.RNM_CYN.NO"));
			
			val	= response.getAttribute("//NOTAGREEDETAIL", "VALUE");
			if("1".equals(val)){
				val	= getErrMessage("성명/주민번호가 불일치 합니다.");
			}else if("2".equals(val)){
				val	= getErrMessage("발급일자가 불일치 합니다.");
			}else{
				val	= getErrMessage(response.getAttribute("//ERRMSG", "VALUE"));
				if(val==null||"".equals(val))
					val	= getErrMessage("기타사항이 불일치 합니다.");
			}
			dataMap.put("FILLER"	, val);
			return;
		}
		
		dataMap.put("RPC"		, config.getProperty("mq.RPC.SUC"));
		dataMap.put("RNM_CYN"	, config.getProperty("mq.RNM_CYN.YES"));
		dataMap.put("FILLER"	, "");
	}
}
