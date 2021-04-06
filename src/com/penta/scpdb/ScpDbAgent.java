package com.penta.scpdb;

/**
 * Damo 암/복호화 솔루션 Class
 * @author 기웅정보통신(주)
 *
 */
public class ScpDbAgent {
	/**
	 * 
	 * @param iniFilePath - Agent 설정 파일의 전체 경로
	 * @param iniKeyName - 설정파일의 [KEYINFO]에 입력된 값 또는 ScpExportContext 에서 생성한 contextStr
	 * @param input - 입력값
	 * @return 입력값을 암호화하고 암호화값을 Base64Encode 한 결과를 출력한다
	 */
	public String ScpEncB64(String iniFilePath, String iniKeyName, String input){
		return input;
	}
	
	/**
	 * 
	 * @param iniFilePath - Agent 설정 파일의 전체 경로
	 * @param iniKeyName - 설정파일의 [KEYINFO] 에 입력된 값 또는 ScpExportContext 에서 생성한 contextStr
	 * @param input - 입력값
	 * @return 암호화 값이 Base64Encode 되어 있는 것을 입력 하여 복호화 한다
	 */
	public String ScpDecB64(String iniFilePath, String iniKeyName, String input){
		return input;
	}
}
