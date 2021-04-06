package com.kwic.common.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <pre>
 * Title		: ZipStreamService
 * Description	: 압축파일 스트림 전송
 * Date			: 2011.12.09
 * Copyright	: Copyright	(c)	2012
 * Company		: KWIC
 * 
 *    수정일                  수정자                      수정내용
 * -------------------------------------------
 * 
 * </pre>
 *
 * @author 장정훈
 * @version	1.0
 * @since 1.4
 */
public class ZipStreamService{
	/**
	 * 압축률 HIGH - 10
	 * */
	public static final int _COMPRESS_LEVEL_HIGH	= 10;
	/**
	 * 압축률 COMMON - 5
	 * */
	public static final int _COMPRESS_LEVEL_COMMON	= 5;
	/**
	 * 압축률 LOW - 3
	 * */
	public static final int _COMPRESS_LEVEL_LOW		= 3;
	/**
	 * 압축률 DEFAULT - 8
	 * */
	public static final int _COMPRESS_LEVEL_DEFAULT	= 8;
	/**
	 * 압축파일 확장자
	 * */
	public static final String _EXTENSION			= ".zip";
	/**<pre>
	 * 하나의 정상적인 stream을 하나의 압축 stream으로 변환하여 전송한다.
	 * </pre>
	 * @param is InputStream
	 * @param entryName String
	 * @param os OutputStream
	 * @param compressLevel int
	 * @throws Exception
	 * */
	public static final void write(InputStream is,String entryName,OutputStream os, int compressLevel) throws Exception{
		write(new InputStream[]{is},new String[]{entryName},os,compressLevel);
	}
	/**<pre>
	 * 복수의 정상적인 stream을 하나의 압축 stream으로 변환하여 전송한다.
	 * </pre>
	 * @param isArr InputStream[]
	 * @param entryNameArr String[]
	 * @param os OutputStream
	 * @param compressLevel int
	 * @throws Exception
	 * */
	public static final void write(InputStream[] isArr,String[] entryNameArr,OutputStream os, int compressLevel) throws Exception{
		ZipOutputStream	zos			= null;
		try{
			zos			= new ZipOutputStream(os);
			zos.setLevel(compressLevel);
			
			for(int i=0;i<entryNameArr.length;i++){
				write(isArr[i],entryNameArr[i],zos);
			}
			zos.flush();
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			for(int i=0;i<isArr.length;i++){try{if(isArr[i]!=null)isArr[i].close();}catch(Exception e){}}
			try{if(zos!=null)zos.close();}catch(Exception e){}
			try{if(os!=null)os.close();}catch(Exception e){}
		}
	}
	/**<pre>
	 * 하나의 정상적인 byte array dta를 하나의 압축 stream으로 변환하여 전송한다.
	 * </pre>
	 * @param bytes byte[]
	 * @param entryName String
	 * @param os OutputStream
	 * @param compressLevel int
	 * @throws Exception
	 * */
	public static final void write(byte[] bytes,String entryName,OutputStream os, int compressLevel) throws Exception{
		write(new byte[][]{bytes},new String[]{entryName},os,compressLevel);
	}
	/**<pre>
	 * 복수의 정상적인 byte array dta를 하나의 압축 stream으로 변환하여 전송한다.
	 * </pre>
	 * @param bytesArr byte[][]
	 * @param entryNameArr String[]
	 * @param os OutputStream
	 * @param compressLevel int
	 * @throws Exception
	 * */
	public static final void write(byte[][] bytesArr,String[] entryNameArr,OutputStream os, int compressLevel) throws Exception{
		ZipOutputStream	zos			= null;
		try{
			zos			= new ZipOutputStream(os);
			zos.setLevel(compressLevel);
			
			for(int i=0;i<entryNameArr.length;i++){
				write(bytesArr[i],entryNameArr[i],zos);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			try{if(zos!=null)zos.close();}catch(Exception e){}
			try{if(os!=null)os.close();}catch(Exception e){}
		}
	}
	/**<pre>
	 * 하나의 정상적인 byte array dta를 하나의 압축 stream으로 변환하여 전송한다.
	 * </pre>
	 * @param bytes byte[]
	 * @param entryName String
	 * @param zos ZipOutputStream
	 * @throws Exception
	 * */
	private static final void write(byte[] bytes,String entryName,ZipOutputStream zos) throws Exception{
		ZipEntry		zentry		= null;
		
		if(bytes==null)
			throw new Exception("Contents is null.");
		if(entryName==null)
			throw new Exception("Entry name is null.");
		if(zos==null)
			throw new Exception("OutputStream is null.");

		try{
			zentry		= new ZipEntry(entryName);
			zos.putNextEntry(zentry);
			zos.write(bytes, 0, bytes.length);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			
		}
	}
	/**<pre>
	 * 하나의 정상적인 byte array dta를 하나의 압축 stream으로 변환하여 전송한다.
	 * </pre>
	 * @param is InputStream
	 * @param entryName String
	 * @param zos ZipOutputStream
	 * @throws Exception
	 * */
	private static final void write(InputStream is,String entryName,ZipOutputStream zos) throws Exception{
		ZipEntry		zentry		= null;
		
		if(is==null)
			throw new Exception("InputStream is null.");
		if(entryName==null)
			throw new Exception("Entry name is null.");
		if(zos==null)
			throw new Exception("OutputStream is null.");

		try{
			int size		= -1;
			byte[] buffer	= new byte[1024];
			zentry		= new ZipEntry(entryName);
			zos.putNextEntry(zentry);
			while ((size = is.read(buffer)) != -1) {
				zos.write(buffer, 0, size);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			
		}
	}
	
//	public static void main(String[] args) throws Exception{
//		FileInputStream[] isArr	= new FileInputStream[3];
//		isArr[0]	= new FileInputStream("C:\\build.xml");
//		isArr[1]	= new FileInputStream("C:\\DARInstaller.log");
//		isArr[2]	= new FileInputStream("C:\\hostname.txt");
//		
//		FileOutputStream os	= new FileOutputStream("C:\\test.zip");
//		
//		ZipStreamService.write(isArr, new String[]{"a.xml","DARInstaller.log","hostname.txt"}, os, ZipStreamService._COMPRESS_LEVEL_DEFAULT);
//	}

}
