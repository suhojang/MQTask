package hanacard.batch.struct;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import com.kwic.config.Config;
import com.kwic.security.aes.AESCipher;
import com.kwic.xml.parser.JXParser;

public class HometaxAccount {
	private static final String cipher_prefix				= "_$_CRYPT_$_";
	
	private static HometaxAccount instance;
	private static List<String[]> accountList	= new ArrayList<String[]>();
	private static int iter	= 0;
	
	private HometaxAccount() throws Exception{
		loadAccounts();
	}
	
	public static HometaxAccount getInstance() throws Exception{
		synchronized(HometaxAccount.class){
			if(instance==null){
				instance	= new HometaxAccount();
			}
			return instance;
		}
	}
	
	private void loadAccounts() throws Exception{
		String id	= null;
		String pwd	= null;
		String tmp	= null;
		
		JXParser jxp	= new JXParser(new File(Config.getInstance().getProperty("hometax.accounts")));
		Element[] accounts	= jxp.getElements("//account");
		
		for(int i=0;i<accounts.length;i++){
			id	= jxp.getValue(jxp.getElement(accounts[i],"id"));
			pwd	= jxp.getValue(jxp.getElement(accounts[i],"password"));
			
			try{
				tmp	= AESCipher.decode(id, AESCipher.DEFAULT_KEY,AESCipher.TYPE_256);
				if(tmp.startsWith(cipher_prefix))
					id	= tmp.substring(cipher_prefix.length());
				tmp	= AESCipher.decode(pwd, AESCipher.DEFAULT_KEY,AESCipher.TYPE_256);
				if(tmp.startsWith(cipher_prefix))
					pwd	= tmp.substring(cipher_prefix.length());
			}catch(Exception e){
			}
			
			jxp.setCData(jxp.getElement(accounts[i],"id"), AESCipher.encode(cipher_prefix+id, AESCipher.DEFAULT_KEY,AESCipher.TYPE_256) );
			jxp.setCData(jxp.getElement(accounts[i],"password"), AESCipher.encode(cipher_prefix+pwd, AESCipher.DEFAULT_KEY,AESCipher.TYPE_256) );
			
			if(id!=null && !"".equals(id) && pwd!=null && !"".equals(pwd))
				accountList.add(new String[]{id,pwd});
		}
		iter	= 0;
		
		FileWriter fw	= null;
		try{
			fw	= new FileWriter(new File(Config.getInstance().getProperty("hometax.accounts")));
			fw.write(jxp.toString(null));
			fw.flush();
		}catch(Exception e){
			throw e;
		}finally{
			try{if(fw!=null)fw.close();}catch(Exception e){}
		}
	}
	
	
	public synchronized String[] getAccount() throws Exception{
		if(accountList.size()==0)
			throw new Exception("There is not defined account in ["+Config.getInstance().getProperty("hometax.accounts")+"].");
		if(iter>=accountList.size())
			iter	= 0;
		
		return accountList.get(iter++);
	}
}
