package com.pnp.eai.entity;

public class EAIMessage {
	/*private String[] sample = new String[] {
			"7507211347520장정훈                                  0120100604                                                                                               127.0.0.1                                                                                   ",
			"750721       장정훈                                  029361439831                                  강원                                                       192.167.0.167                                                                               ",
			"6410145100182                                        0320130307                                                                                                                                                                                           ",
			"                                                     296172722546                                                                                                                                                                                         ",
			"                                                     291244921412                                                                                                                                                                                         "
			// ," 291010223620 "
			// ," 296172722546 "
			// ," 291244921412 "
			// ," 291010223620 "
	};*/
	
	private String[] sample	= new String[] {
//		"7507211347520장정훈                                  0120100604                                                                                                                                                                                           @@",
//		"750721       장정훈                                  029361439831                                  강원                                                       10.137.210.103                                                                              @@"
//		"750721       장정훈                                  02 9361439831                                  강원                                                       10.137.210.103                                                                              @@"
//		"NTWKVJYESXOTX오화경                                  02IIJQAESTCL                                  서울                                                                                                                                                   @@"
		"4098654783   주식회사 더나은                         294098654783                                                                                             10.6.32.104                                                                                 @@"
//		"COTLHEKELBFAD권진범                                  01EPTUBAUI                                                                                                                                                                                           @@"
	};

	public BFH getBFH() {
		return new BFH();
	}

	int idx = 0;

	public byte[] getUserData() {
		String header	= "  010001    0000001      010001    1234                                                                               00000000                                                                                                1000000757                                                       00000000                                                                                                                                                                                                                                                                                                                                                                                 00000                                                                      ";
		
		if (idx >= sample.length) {
			return null;
		}
		return  (header + sample[idx++]).getBytes();
	}
}
