package com.kwic.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.kwic.security.aes.AESCipher;

public class CProperties extends Properties{
	private static final long serialVersionUID	= 1L;
	
	private static final String keyValueSeparators			= "=: \t\r\n\f";
    private static final String strictKeyValueSeparators	= "=:";
    private static final String whiteSpaceChars				= " \t\r\n\f";

	private static final String cipher_prefix				= "_$_CRYPT_$_";
	private static final String _line						= System.getProperty("line.separator");
	
	private String resourcePath;
	private boolean cryptoEnable;
	
	public CProperties(){
		this(false);
	}
	public CProperties(boolean cryptoEnable){
		super();
		this.cryptoEnable	= cryptoEnable;
	}
	
	public synchronized Object setProperty(String key,String value){
		String enc	= value;

		if(cryptoEnable){
			try{
				enc	= AESCipher.encode(cipher_prefix+value, AESCipher.DEFAULT_KEY,AESCipher.TYPE_256);
			}catch(Exception e){
			}
		}
		
		return super.setProperty(key, enc);
	}
	
	public String getProperty(String key){
		String value	= super.getProperty(key);
		String dec		= value;

		if(cryptoEnable){
			if(value==null || "".equals(value))
				return value;
			try{
				dec	= AESCipher.decode(value, AESCipher.DEFAULT_KEY,AESCipher.TYPE_256);
			}catch(Exception e){
			}
			
			if(!dec.startsWith(cipher_prefix)){
				return value;
			}
			dec	= dec.substring(cipher_prefix.length());
		}
		
		try {
			List<String> params	= PluginUtil.getConditions(dec, PluginUtil._REPLACE_VARIABLE_CHAR);
			for(int i=0;i<params.size();i++){
				if(getProperty(params.get(i))!=null)
					dec	= PluginUtil.replaceAll(dec, PluginUtil._REPLACE_VARIABLE_CHAR+params.get(i)+PluginUtil._REPLACE_VARIABLE_CHAR, getProperty(params.get(i)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dec;
	}

	public String getDecodeProperties(){
		StringBuffer sb	= new StringBuffer();
		Set<Object> keys	= keySet();
		Iterator<Object> iter	= keys.iterator();
		String key		= "";
		String value	= "";
		while(iter.hasNext()){
			key		= (String) iter.next();
			value	= getProperty(key);
			sb.append(key+" = "+value).append(_line);
		}
		return sb.toString();
	}

	public String getResourcePath(){
		return resourcePath;
	}
	
	public synchronized void loadProperties(String propertiesPath) throws IOException{
		
		URL url	= null;
		try{
			url	= ClassLoader.getSystemResource(propertiesPath);
		}catch(Exception e){
		}
		if(url==null)
			url	= getClass().getClassLoader().getResource(propertiesPath);

		if(url==null)
			resourcePath	= propertiesPath;
		else
			resourcePath	= url.getFile();
		
		InputStream is	= null;
		try{
			if(url!=null){
				try{
					is	= ClassLoader.getSystemResourceAsStream(propertiesPath);
				}catch(Exception e){
				}
				if(is==null)
					is	= getClass().getClassLoader().getResourceAsStream(propertiesPath);
			}else{
				is	= new FileInputStream(new File(resourcePath));
			}
			String encProperties	= loadProperties(is);

			if(cryptoEnable){
				FileWriter fw	= null;
				if(encProperties!=null){
					try{
						fw	= new FileWriter(new File(resourcePath));
						
						fw.write(encProperties);
						fw.flush();
					}catch(Exception e){
						e.printStackTrace();
					}finally{
						try{if(fw!=null)fw.close();}catch(Exception ex){}
					}
					if(getProperty("SRC_PATH")!=null && !"".equals(getProperty("SRC_PATH"))){
						try{
							fw	= new FileWriter(new File(getProperty("SRC_PATH")+"/"+propertiesPath));
							
							fw.write(encProperties);
							fw.flush();
						}catch(Exception e){
							e.printStackTrace();
						}finally{
							try{if(fw!=null)fw.close();}catch(Exception ex){}
						}
					}
				}
			}
		}catch(IOException ie){
			throw ie;
		}finally{
			try{if(is!=null)is.close();}catch(Exception e){}
		}
	}
	
	private synchronized String loadProperties(InputStream inStream) throws IOException {
		StringBuffer sb			= new StringBuffer();
		boolean hasPlainValue	= false;
		
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
        String line	= null;
    	while ((line = in.readLine())!=null) {
            if (line.length() > 0) {
                // Find start of key
                int len = line.length();
                int keyStart;
                for (keyStart=0; keyStart<len; keyStart++)
                    if (whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)
                        break;
                // Blank lines are ignored
                if (keyStart == len){
                    sb.append(line).append(_line);
                    continue;
                }
                // Continue lines that end in slashes if they are not comments
                char firstChar = line.charAt(keyStart);
                if ((firstChar != '#') && (firstChar != '!')) {
                    while (continueLine(line)) {
                        String nextLine = in.readLine();
                        if (nextLine == null)
                            nextLine = "";
                        String loppedLine = line.substring(0, len-1);
                        // Advance beyond whitespace on new line
                        int startIndex;
                        for (startIndex=0; startIndex<nextLine.length(); startIndex++)
                            if (whiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1)
                                break;
                        nextLine = nextLine.substring(startIndex,nextLine.length());
                        line = new String(loppedLine+nextLine);
                        len = line.length();
                    }

                    // Find separation between key and value
                    int separatorIndex;
                    for (separatorIndex=keyStart; separatorIndex<len; separatorIndex++) {
                        char currentChar = line.charAt(separatorIndex);
                        if (currentChar == '\\')
                            separatorIndex++;
                        else if (keyValueSeparators.indexOf(currentChar) != -1)
                            break;
                    }

                    // Skip over whitespace after key if any
                    int valueIndex;
                    for (valueIndex=separatorIndex; valueIndex<len; valueIndex++)
                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                            break;

                    // Skip over one non whitespace key value separators if any
                    if (valueIndex < len)
                        if (strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)
                            valueIndex++;

                    // Skip over white space after other separators if any
                    while (valueIndex < len) {
                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                            break;
                        valueIndex++;
                    }
                    String key = line.substring(keyStart, separatorIndex);
                    String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

                    // Convert then store key and value
                    if(cryptoEnable){
                        key		= loadConvert(key);
                        value	= loadConvert(value);
                    }
                    put(key, value);
                    
                    if(cryptoEnable){
                        if(value!=null && !"".equals(value)){
                            String dec	= value;
                            
    						try{
    							dec	= AESCipher.decode(value, AESCipher.DEFAULT_KEY,AESCipher.TYPE_256);
    						}catch(Exception e){}
                    		if(!dec.startsWith(cipher_prefix)){
    							try {
    								value	= AESCipher.encode(cipher_prefix+value, AESCipher.DEFAULT_KEY,AESCipher.TYPE_256);
    								hasPlainValue	= true;
    							} catch (Exception e) {
    								e.printStackTrace();
    							}
                    		}else{
                    			
                    		}
                    		line	= line.substring(0,valueIndex)+value;
                        }
                    }
                }
            }
            if(cryptoEnable)
            	sb.append(line).append(_line);
    	}
    	return (cryptoEnable&&hasPlainValue)?sb.toString():null;
    }

	private boolean continueLine(String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while ((index >= 0) && (line.charAt(index--) == '\\'))
            slashCount++;
        return (slashCount % 2 == 1);
    }
	
    private String loadConvert(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);

        for (int x=0; x<len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value=0;
		    for (int i=0; i<4; i++) {
		        aChar = theString.charAt(x++);
		        switch (aChar) {
		          case '0': case '1': case '2': case '3': case '4':
		          case '5': case '6': case '7': case '8': case '9':
		             value = (value << 4) + aChar - '0';
			     break;
			  case 'a': case 'b': case 'c':
                          case 'd': case 'e': case 'f':
			     value = (value << 4) + 10 + aChar - 'a';
			     break;
			  case 'A': case 'B': case 'C':
                          case 'D': case 'E': case 'F':
			     value = (value << 4) + 10 + aChar - 'A';
			     break;
			  default:
                              throw new IllegalArgumentException(
                                           "Malformed \\uxxxx encoding.");
                        }
                    }
                    outBuffer.append((char)value);
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }
	
    public String toString(){
    	StringBuffer sb	= new StringBuffer();
    	Enumeration<?> keys	= keys();
    	String key	= null;
    	while(keys.hasMoreElements()){
    		key	= (String) keys.nextElement();
    		sb.append(key+"\t= "+getProperty(key)).append(_line);
    	}
    	return sb.toString();
    }
    
	public static void main(String[] args) throws IOException{
		CProperties m	= new CProperties(true);
		m.loadProperties("D:/j2ee_workspace/SHCD_IMG_CLIENT/config/log.properties");
		System.out.println("===========================================");
		System.out.println(m);
		System.out.println("===========================================");
	}
}
