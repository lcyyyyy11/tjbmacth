package com.zzs.TJBmatch.services;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.zzs.TJBmatch.dbHelper.ZZSR2DBCService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.HttpCookie;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class Utilities {
   static Logger log = LoggerFactory.getLogger( Utilities.class );

	private static String OS = System.getProperty("os.name").toLowerCase();

	public static String getFullParameters(ServerRequest req) {
		return Joiner.on("&").withKeyValueSeparator("=").join(req.queryParams().toSingleValueMap());
	}

	public static Map<String,String> getMapFromString(String sPara){
		return  Splitter.on( "&" ).withKeyValueSeparator( "=" ).split(sPara);
	}

	public static HashMap<String,Object> getParaMapFromString(String sPara){

		return new HashMap<String,Object>( getMapFromString(sPara) );

	}
	
	public static List<HttpCookie> getAllCookiesArray(ServerRequest req){

		String[] ls = "I am A student".split(" ");

		MultiValueMap<String, HttpCookie> cookiesMap = req.cookies();

		return null;
	}

	public static String setEncodevalue(String oriValue){
		try {
			return URLEncoder.encode(oriValue,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public static String getDecodeValue(String encodeValue){
		try {
			return URLDecoder.decode(encodeValue,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
	public static boolean isInteger(String str) {
		if (null == str || str.isEmpty()) {
			return false;
		}
	    int size = str.length();
	    for (int i = 0; i < size; i++) {
	        if (!Character.isDigit(str.charAt(i))) {
	            return false;
	        }
	    }
	    return true;
	}

	public static boolean isInteger(Object obj) {
		if (Objects.isNull(obj)) {
			return false;
		}
		return isInteger(obj.toString());
	}
	
	public static Date StringToDatetime(String dtString){ 
		  SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		  try {
			return formatter.parse(dtString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			return null;
		} 
	} 
	
	public static Date StringToDate(String dtString){ 
		  SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd"); 
		  try {
			return formatter.parse(dtString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			return null;
		} 
	} 
	
	public static void pushDownloadFile(ServerResponse response, String file)
	{
		response.headers().setContentType( MediaType.APPLICATION_JSON);
		
		//Tell Browser Doneload to receive
		response.headers().add("Content-Type", "application/octet-stream");
		
		///client asked filename
		
		response.headers().add("Content-Disposition", "attachment; filename=" + file);
		
		//通过文件流读取
		
		InputStream ins = null;
		OutputStream outs = null;
		try {
			ins = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//getServletContext().getResourceAsStream("/download/" + file);
			
//		try {
//			//outs = response.getOutputStream();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = ins.read(buffer)) != -1) {
				outs.write(buffer,0,len);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static String[] findSqlTables(String sql){
		List<String> ltables =new ArrayList<String>();
		sql = sql.toUpperCase();
		ltables = Arrays.stream(sql.split("FROM|JOIN|INTO|UPDATE"))
				.map(s1-> s1.trim().split(" ")[0].trim())
				.filter(s2 -> !s2.equals("SELECT") && !s2.equals("") && !s2.equals("INSERT") && !s2.equals("DELETE"))
				.distinct()
				.collect(Collectors.toList());
		return ltables.toArray(new String[0]);

	}

	public static String getSqlType(String sqlTxt){
		String sqlCat = sqlTxt.trim().split(" ")[0].toUpperCase();
		switch (sqlCat){
			case "SELECT":
				sqlCat = "查询";
				break;
			case "INSERT":
				sqlCat = "新加";
				break;
			case "UPDATE":
				sqlCat = "修改";
				break;
			case "DELETE":
				sqlCat = "删除";
				break;
			default:
				sqlCat = "查询";
				break;
		}
		return sqlCat;
	}

	public static int IndexOfAny(String valueStr, char[] cArr) {
		int thisIndex = -1, cIndex = -1;

		try {
			thisIndex = Stream.of(cArr.toString()).map(s -> valueStr.indexOf(s))
					.filter(i -> i > -1)
					.min(Comparator.comparing(Integer::valueOf))
					.get();
		}catch (Exception e){
			thisIndex = -1;
		}

		return thisIndex;

	}

	public static int IndexOfAny(String valueStr, String[] sArr) {
		int thisIndex = -1, sIndex = -1;

		try {
			thisIndex = Arrays.stream(sArr).map(s1 -> valueStr.indexOf(s1))
					.filter(i -> i > -1)
					.min(Comparator.comparing(Integer::valueOf))
					.get();
		}catch (Exception e){
			thisIndex = -1;
		}

		return thisIndex;
	}


	public static Mono<String> GetSqlInList(DatabaseClient databaseClient, String sqlTxt, Map<String,Object> mapPara, String key){

       return ZZSR2DBCService.getListMap(databaseClient, sqlTxt, mapPara)
				.map(x -> {
				    String collect = "('";
                    collect += x.stream()
                            .map(p -> String.valueOf(p.getOrDefault(key, "")))
                            .collect(Collectors.joining(","));
                    collect += "')";
//                    log.info("collect："+collect);
                    return collect;
                })
               .switchIfEmpty( Mono.just("('')"));

	}

	public static String splitStrToFormatStr(String oriStr,int len,String slink){
		String strFormat="";
		if (oriStr.trim().length()<= len)
			return oriStr.trim()+slink;

		while (oriStr.length()>len){
			strFormat +=oriStr.substring(0,len)+slink;
			oriStr = oriStr.substring( len );
		}
		strFormat +=oriStr+slink;

        return strFormat;
	}

	public static boolean isWindows() {

		return (OS.indexOf("win") >= 0);

	}

	public static boolean isMac() {

		return (OS.indexOf("mac") >= 0);

	}

	public static boolean isUnix() {

		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

	}

	public static boolean isSolaris() {

		return (OS.indexOf("sunos") >= 0);

	}

	public static String[] searchLocation (String matid) {
		if (StringUtils.isEmpty(matid) || matid.trim().equals("")) {
			return null;
		}
		String[] lArr = new String[4];
		int len = matid.length();
		if (len == 1) {
			lArr[0] = "00"+matid;
			lArr[1] = "000";
			lArr[2] = "000";
			lArr[3] = matid;
		}
		else if (len == 2) {
			lArr[0] = "0"+matid;
			lArr[1] = "000";
			lArr[2] = "000";
			lArr[3] = matid;
		}
		else if (len == 3) {
			lArr[0] = matid;
			lArr[1] = "000";
			lArr[2] = "000";
			lArr[3] = matid;
		}
		else if (len == 4) {
			lArr[0] = matid.substring(1,4);
			lArr[1] = "00"+matid.substring(0,1);
			lArr[2] = "000";
			lArr[3] = matid;
		}
		else if (len == 5) {
			lArr[0] = matid.substring(2,5);
			lArr[1] = "0"+matid.substring(0,2);
			lArr[2] = "000";
			lArr[3] = matid;
		}
		else if (len == 6) {
			lArr[0] = matid.substring(3,6);
			lArr[1] = matid.substring(0,3);
			lArr[2] = "000";
			lArr[3] = matid;
		}
		else if (len == 7) {
			lArr[0] = matid.substring(4,7);
			lArr[1] = matid.substring(1,4);
			lArr[2] = "00"+matid.substring(0,1);
			lArr[3] = matid;
		}
		else if (len == 8) {
			lArr[0] = matid.substring(5,8);
			lArr[1] = matid.substring(2,5);
			lArr[2] = "0"+matid.substring(0,2);
			lArr[3] = matid;
		}
		else if (len == 9) {
			lArr[0] = matid.substring(6,9);
			lArr[1] = matid.substring(3,6);
			lArr[2] = matid.substring(0,3);
			lArr[3] = matid;
		}
		else {
			lArr[0] = matid.substring(len -3,len);
			lArr[1] = matid.substring(len -6, len-3);
			lArr[2] = matid.substring(len -9, len-6);
			lArr[3] = matid;
		}
		return lArr;
	}

	public static String getFileName() {
		Long lseed=0l;
		synchronized(Utilities.class){
			try {
				Thread.sleep( 1 ); //必须保留延时-- ZZS
				lseed = System.currentTimeMillis();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		String chkNo = Arrays.stream(lseed.toString().split( "" ))
				.map(Integer::parseInt)
				.reduce(0,(a,b) -> (a+b) )
				.toString();
		return (lseed + "00").substring( 0,13 ) + chkNo.substring( chkNo.length() - 1 );
	}


	public static int traverseByCommonsIOByRecursively(File srcFile, File desFile) {
		int result = 0;
		if (srcFile.isDirectory())
		{
			Collection<File> files = org.apache.commons.io.FileUtils.listFiles(srcFile,null,false);
			for (File file : files)
			{
				File desFileOrDir = new File(desFile.getAbsolutePath() + File.separator + file.getName());
				if(file.isDirectory())
				{
					if(desFileOrDir.exists())
						desFileOrDir.delete();
					desFileOrDir.mkdirs();
				}
				traverseByCommonsIO(file, desFileOrDir);
			}
		}
		else
		{
			result += randomAccessFileCopy(srcFile, desFile);
		}
		return result;
	}

	public static int traverseByCommonsIO(File srcDir, File desDir) {
		int result = 0;
		if (!srcDir.exists()) {
			return result;
		}
		if (!desDir.exists()) {
			desDir.mkdirs();
		}
		Collection<File> files = org.apache.commons.io.FileUtils.listFiles(srcDir,null,false);
		File desFile;
		String[] arr = {".MP4",".MOV",".M4V"};
		String format;
		for (File file : files)
		{
			format = StringUtils.getFilenameExtension(file.getName());
			if (StringUtils.isEmpty(format)) {
				continue;
			}
			format = "." + format.toUpperCase();
			if (!Arrays.asList(arr).contains(format)) {
				desFile = new File(desDir.getAbsolutePath() + File.separator + file.getName());
				result += copyFile(file, desFile);
			}
		}
		return result;
	}

	private static int copyFile(File src, File dest) {
		try {
			FileChannel srcChannel = new FileInputStream(src).getChannel();
			FileChannel desChannel = new FileOutputStream(dest).getChannel();
			srcChannel.transferTo(0,srcChannel.size(),desChannel);
			srcChannel.close();
			desChannel.close();
			return 1;
		}
		catch (Exception ex) {
			log.error("复制文件失败，复制原位置：{}，新位置：{}，错误原因：{}", src, dest, ex.getMessage());
			return 0;
		}
	}

	private static int randomAccessFileCopy(File src, File dest)  {
		try {
			// 获得输入输出流的文件通道
			FileChannel fcIn = new RandomAccessFile(src, "r").getChannel();
			FileChannel fcOut = new RandomAccessFile(dest, "rw").getChannel();
			// 输入流的字节大小
			long size = fcIn.size();
			// 输入输出流映射到缓冲区
			MappedByteBuffer inBuf = fcIn.map(FileChannel.MapMode.READ_ONLY, 0, size);
			MappedByteBuffer outBuf = fcOut.map(FileChannel.MapMode.READ_WRITE, 0, size);
			// 目的：将输入流缓冲区的内容写到输出流缓冲区就完成了文件的复制
			// 操作的是缓冲区
			for (int i = 0; i < size; i++) {
				outBuf.put(inBuf.get());
			}
			// 关闭（关闭通道时会写入数据块）
			fcIn.close();
			fcOut.close();
			return 1;
		}
		catch (Exception ex) {
			return 0;
		}
	}



}
