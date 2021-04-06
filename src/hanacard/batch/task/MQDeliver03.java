package hanacard.batch.task;

import java.util.Map;

import com.kwic.xml.parser.JXParser;

/**
 * 외국인등록증 진위여부 <br/>
 * HS_VD_PHC=04 <br/>
 * SPECIALCODE=0039
 */
public class MQDeliver03 extends MQDeliverImpl {

	public MQDeliver03(int threadNo, Map<String, String> dataMap, byte[] requestBytes) throws Exception {
		super(threadNo, dataMap, requestBytes);
	}

	@Override
	public String exec() throws Exception {
		struct.setAttribute("//SEEDKEY",        "Value", config.getProperty("encrypt.key"));
		struct.setAttribute("//SPECIALCODE",    "Value", config.getProperty("03.SPECIALCODE"));
		struct.setAttribute("//MODULE", 		"Value", config.getProperty("03.MODULE"));
		struct.setAttribute("//CERTKEY",        "Value", config.getProperty("03.CERTKEY"));
		struct.setAttribute("//DEPARTMENTCODE", "Value", config.getProperty("03.DEPARTMENTCODE"));
		struct.setAttribute("//FOREIGNJUMIN",   "Value", getParam("FOREIGNJUMIN", dataMap.get("RRNO")));
		struct.setAttribute("//ISSUEDATE",      "Value", getParam("ISSUEDATE",    dataMap.get("IDF_NO")));
		struct.setAttribute("//USERIP",         "Value", getParam("USERIP",       dataMap.get("IPV6_ADR")));
		struct.setAttribute("//USERPORT",       "Value", config.getProperty("01.USERPORT"));

		return struct.toString(null);
	}

	@Override
	public void response(JXParser response) throws Exception {
		String val = response.getAttribute("//INQNORMALPE", "VALUE");

		if (!config.getProperty("03.INQNORMALPE.YES").equals(val.trim())) {
			dataMap.put("RPC",     config.getProperty("mq.RPC.ERR"));
			dataMap.put("RNM_CYN", config.getProperty("mq.RNM_CYN.NO"));
			dataMap.put("FILLER",  getErrMessage(response.getAttribute("//ERRMSG", "VALUE")));
			return;
		}

		val = response.getAttribute("//REGCHECKYN", "VALUE");
		if (!config.getProperty("03.REGCHECKYN.YES").equals(val)) {
			dataMap.put("RPC",     config.getProperty("mq.RPC.SUC"));
			dataMap.put("RNM_CYN", config.getProperty("mq.RNM_CYN.NO"));
			dataMap.put("FILLER",  getErrMessage(response.getAttribute("//ERRMSG", "VALUE")));
			return;
		}

		dataMap.put("RPC",     config.getProperty("mq.RPC.SUC"));
		dataMap.put("RNM_CYN", config.getProperty("mq.RNM_CYN.YES"));
		dataMap.put("FILLER",  "");
	}
}
