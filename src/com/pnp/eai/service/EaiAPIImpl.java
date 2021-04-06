package com.pnp.eai.service;

import com.pnp.eai.entity.EAIMessage;

public class EaiAPIImpl {
	EAIMessage eai = new EAIMessage();

	public EaiAPIImpl(String s) {

	}

	public void MqInit() {

	}

	public EAIMessage mqMsgGet() {
		return eai;
	}

	public void mqReplyPut(EAIMessage recvEAIMsg, byte[] requestBytes, String rtnCode) {

	}

	public void MqClose() {

	}
}
