package com.zzs.TJBmatch.services;


import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


@Service
public class BytesSer {
	public BytesSer() {
	}

	public static void encryptFile(String fileUrl) throws Exception {
	    File file = new File(fileUrl);
	    String path = file.getPath();
	    if(!file.exists()){
	      return;
	    }
	    int index = path.lastIndexOf("\\");
	    String destFile = path.substring(0, index)+"\\"+"zzs";
	    File dest = new File(destFile);
	    InputStream inFile = new FileInputStream(fileUrl);
	    OutputStream outFile = new FileOutputStream(destFile);
	    byte[] buffer = new byte[1024];
	    int ir;
	    int headMark = 0;
	    byte[] buffer2=new byte[1024];
	    while (( ir= inFile.read(buffer)) > 0) {
	        for(int i=0;i<ir;i++)
	        {
	        	if(i==147)
	        		headMark = 1;
		        byte b=buffer[i];
		        if (headMark == 0)
		        	buffer2[i]=(byte) (255 - b);  //b==255?0:++b
		        else 
		        	buffer2[i] = b;
	        }
	        outFile.write(buffer2, 0, ir);
	        outFile.flush();
	    }
	    inFile.close();
	    outFile.close();
	    file.delete();
	    dest.renameTo(new File(fileUrl));
	}
	public static void encryptFile(String fileUrl, String key) throws Exception {
	    File file = new File(fileUrl);
	    String path = file.getPath();
	    if(!file.exists()){
	      return;
	    }
	    int index = path.lastIndexOf("\\");
	    String destFile = path.substring(0, index)+"\\"+"zzs";
	    File dest = new File(destFile);
	    InputStream inFile = new FileInputStream(fileUrl);
	    OutputStream outFile = new FileOutputStream(destFile);
	    byte[] buffer = new byte[1024];
	    int ir;
	    byte[] buffer2=new byte[1024];
	    while (( ir= inFile.read(buffer)) > 0) {
	        for(int i=0;i<ir;i++)
	        {
	          byte b=buffer[i];
	          buffer2[i]=(byte) (255 - b);  //b==255?0:++b
	        }
	        outFile.write(buffer2, 0, ir);
	        outFile.flush();
	    }
	    inFile.close();
	    outFile.close();
	    file.delete();
	    dest.renameTo(new File(fileUrl));
	    appendMethodA(fileUrl, key);
	}
	  /**
	   *
	   * @param fileName
	   * @param content ��Կ
	   */
	 private static void appendMethodA(String fileName, String content) {
	      try {
		      RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
		      long fileLength = randomFile.length();
		      randomFile.seek(fileLength);
		      randomFile.writeBytes(content);
		      randomFile.close();
	      } catch (IOException e) {
	    	  e.printStackTrace();
	      }
	 }

	 public static String decryptFile(String fileUrl, String tempUrl) throws Exception{
	      File file = new File(fileUrl);
	      if (!file.exists()) {
	        return null;
	      }
	      File dest = new File(tempUrl);
	      if (!dest.getParentFile().exists()) {
	        dest.getParentFile().mkdirs();
	      }
	      InputStream inFile = new FileInputStream(fileUrl);
	      OutputStream outFile = new FileOutputStream(tempUrl);
	      byte[] buffer = new byte[1024];
	      byte[] buffer2=new byte[1024];
	      int ir;
	      int headMark = 0;
	      while (( ir= inFile.read(buffer)) > 0) {
		        for(int i=0;i<ir;i++)
		        {
		        	if(i==147)
		        		headMark = 1;
			        byte b=buffer[i];
			        if (headMark == 0)
			        	buffer2[i]=(byte) (255 - b);  //b==255?0:++b
			        else 
			        	buffer2[i] = b;
		        }
		        outFile.write(buffer2, 0, ir);
		        outFile.flush();
		    }
	      
	      inFile.close();
	      outFile.close();
	      return tempUrl;
	  }
	 public static String decryptFile(String fileUrl, String tempUrl, int keyLength) throws Exception{
	      File file = new File(fileUrl);
	      if (!file.exists()) {
	        return null;
	      }
	      File dest = new File(tempUrl);
	      if (!dest.getParentFile().exists()) {
	        dest.getParentFile().mkdirs();
	      }
	      InputStream is = new FileInputStream(fileUrl);
	      OutputStream out = new FileOutputStream(tempUrl);
	      byte[] buffer = new byte[1024];
	      byte[] buffer2=new byte[1024];
	      byte bMax=(byte)255;
	      long size = file.length() - keyLength;
	      int mod = (int) (size%1024);
	      int div = (int) (size>>10);
	      int count = mod==0?div:(div+1);
	      int k = 1, r;
	      while ((k <= count && ( r = is.read(buffer)) > 0)) {
	        if(mod != 0 && k==count) {
	          r = mod;
	        }
	        for(int i = 0;i < r;i++)
	        {
	          byte b=buffer[i];
	          buffer2[i]=b==0?bMax:--b;
	        }
	        out.write(buffer2, 0, r);
	        k++;
	      }
	      out.close();
	      is.close();
	      return tempUrl;


	  }

	/**
	 *
	 * @param fileName
	 * @param keyLength
	 * @return
	 */
	  public static String readFileLastByte(String fileName, int keyLength) {
	     File file = new File(fileName);
	     if(!file.exists())return null;
	     StringBuffer str = new StringBuffer();
	      try {
	        // ��һ����������ļ���������д��ʽ
	        RandomAccessFile randomFile = new RandomAccessFile(fileName, "r");
	        // �ļ����ȣ��ֽ���
	        long fileLength = randomFile.length();
	        //��д�ļ�ָ���Ƶ��ļ�β��
	        for(int i = keyLength ; i>=1 ; i--){
	          randomFile.seek(fileLength-i);
	          str.append((char)randomFile.read());
	        }
	        randomFile.close();
	        return str.toString();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	      return null;
	  }

	//This MessageDigest class provides applications the functionality of a message digest algorithm,
	// such as SHA-1 or SHA-256. Message digests are secure one-way hash functions that take
	// arbitrary-sized data and output a fixed-length hash value.
	  public static String getMD5Str(String str) {
		  MessageDigest md = null;
		  try {
			  md = MessageDigest.getInstance("MD5");
		  } catch (NoSuchAlgorithmException e) {
			  e.printStackTrace();
		  }
		  md.update(str.getBytes());
		  byte[] digest = md.digest();
		  return DatatypeConverter.printHexBinary(digest).toUpperCase();
	  }
	  
	  public static byte[] reverseByte(byte[] bOri)
     {
		  byte[] Result = new byte[bOri.length];
			for (int i = 0; i < bOri.length; i++)
			{
				Result[i] = (byte)(255 - bOri[i]);
			}
			return Result;
     }
	  
	  public static byte[] inputStreamToByteArray(InputStream istrm){
		  ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		  try {
		      int read = istrm.read();
		      while (read != -1) {
		          byteArrayOutputStream.write(read);
		          read = istrm.read();
		      }
		  } catch (IOException e) {
		      e.printStackTrace();
		      return null;
		  }
		  return byteArrayOutputStream.toByteArray();
	  }
	  
	  public static boolean stringContainsItemFromList(String inputStr, String[] items) {
		    return Arrays.stream(items).anyMatch(inputStr::contains);
//		  return Arrays.stream(items).sequential().anyMatch(inputStr::contains);
//		  return Arrays.stream(items).parallel().anyMatch(inputStr::contains);
	 }

}
