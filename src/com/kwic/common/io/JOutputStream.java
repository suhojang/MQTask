package com.kwic.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class JOutputStream extends OutputStream {
	private List<byte[]>	byteArrList	= new ArrayList<byte[]>();
	private int		byteLength	= 0;
	private OutputStream os;
	public JOutputStream(){
		
	}
	public JOutputStream(OutputStream os){
		this.os	= os;
	}
	public void write(int b) throws IOException {
		if(os!=null)
			os.write(b);
	}
	public void write(byte[] b) throws IOException {
		if(os!=null)
			os.write(b);
		byteArrList.add(b);
		byteLength	+= b.length;
	}
    public void write(byte b[], int off, int len) throws IOException {
		if(os!=null)
			os.write(b, off, len);
    	byte[] a	= new byte[len];
    	System.arraycopy(b,off,a,0,a.length);
		byteArrList.add(a);
		byteLength	+= len;
    }
    
    public long getLength(){
    	return byteLength;
    }

    public byte[] getBytes(){
    	byte[]	b	= new byte[byteLength];
    	byte[]	a	= null;
    	int		idx	= 0;
    	for(int i=0;i<byteArrList.size();i++){
    		a	= (byte[])byteArrList.get(i);
    		System.arraycopy(a,0,b,idx,a.length);
    		idx	+= a.length;
    	}
    	return b;
    }
    public void clear(){
    	byteArrList.clear();
    	byteLength	= 0;
    }
}
