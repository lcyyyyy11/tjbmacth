package com.zzs.TJBmatch.services;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Service
public class MSExcelSer {

	/*
	* file 文件路径
	* tableName 导入临时表表名
	* dbFields 临时表字段名
	* dbFieldsType 临时表字段数据类型
	* sCols Excel表列名
	* addFeilds 添加的字段名（不需要从Excel中读取，但需要写入临时表的字段名）
	* addValues 添加的字段值
	* addTypes 添加的字段的数据类型
	* */
    public static String[] getExcelToMSSqlSqlArray(String file,String tableName,String[] dbFields,String[] dbFieldsType,String[] sCols,String[] addFeilds,String[] addValues,String[] addTypes) throws Exception{
    	File excel = new File(file);
        InputStream fis = new FileInputStream(excel);
        Workbook wbook = WorkbookFactory.create(fis); //new XSSFWorkbook(fis);//
        Sheet wsheet = wbook.getSheetAt(0);

        wsheet.setForceFormulaRecalculation(true);

        int rowNum = wsheet.getLastRowNum() + 1;
        int colNum = wsheet.getRow(0).getLastCellNum();
        int[] indexCol = new int[sCols.length]; 
        //Read the headers first. Locate the ones you need
        Row rowHeader = wsheet.getRow(0);
        for (int j = 0; j < colNum; j++) {
            Cell cell = rowHeader.getCell(j);
            String cellValue = cellToString(cell);
            for (int k=0; k<sCols.length;k++){
            	if(sCols[k].equalsIgnoreCase(cellValue)){
            		indexCol[k] = j;
            		break;
            	}
            }
        }

        String[] sqlList = new String[rowNum - 1];
        String sqlConst = "",valueConst="";//"Insert Into " + tableName + " (";

        for (int i=0;i<dbFields.length - 1;i++){
        	sqlConst += dbFields[i] + ",";
        }
        sqlConst += dbFields[dbFields.length - 1];
        if (addFeilds.length > 0)
        	for (int i = 0;i<addFeilds.length;i++)
        		sqlConst += (","+ addFeilds[i]);
        
        sqlConst = "Insert Into " + tableName + " ("+ sqlConst + ") Values(";
        
        for (int i = 1; i < rowNum; i++) {
        	valueConst = "";
            Row row = wsheet.getRow(i);
            String[] colValue = new String[sCols.length];
            for (int j=0;j<sCols.length;j++){
            	try{
            		colValue[j] = cellToString(row.getCell(indexCol[j]));
            	}catch(Exception e){
            		colValue[j] = "";
            	}

				if (dbFieldsType[j].equalsIgnoreCase("STRING")){
					// valueConst += ("'" + colValue[j]+"',");
					if(colValue[j].trim().equals("")){
						valueConst += (null+",");
					}else{
						valueConst += ("'" + colValue[j]+"',");
					}
				}
				else if (dbFieldsType[j].equalsIgnoreCase("NUMBER")) {
					if(colValue[j].trim().equals("")){
						valueConst += (null+",");
					}else{
						valueConst += (colValue[j]+",");
					}
				}else if(dbFieldsType[j].equalsIgnoreCase("DATE")){
					if(colValue[j].trim().equals("")){
						valueConst += (null+",");
					}else{
						valueConst += ("'" + colValue[j]+"',");
					}
				}
				else{
					valueConst += ("'" + colValue[j]+"',");
				}
            }
            if (valueConst.charAt(valueConst.length() - 1) == ',') {
            	valueConst = valueConst.substring(0, valueConst.length() - 1);
            }
            if (addFeilds.length > 0){
            	for (int j=0;j<addFeilds.length;j++){
            		if (addTypes[j].equalsIgnoreCase("STRING")){
    	            		valueConst += (",'" + addValues[j]+"'");
                	}
                	else if (addTypes[j].equalsIgnoreCase("NUMBER")) {
    	            		valueConst += (","+ addValues[j]);
    				}
                	else{
    	            		valueConst += (",'" + addValues[j]+"'");
                	}
            	}
            }
            sqlList[i - 1] = sqlConst + valueConst+")";
        }
        fis.close();
        try{
        	wbook.close();
        }catch(Exception e){wbook = null;}
	   return sqlList;
  }

	public static String cellToString(Cell cell){
	    Object value = null;
	    DecimalFormat df = new DecimalFormat("0");  //格式化number String字符
	    DecimalFormat df2 = new DecimalFormat("0.00");  //格式化数字
		//cell.getCellTypeEnum()
	    switch (cell.getCellType()) {
	        case STRING:
	            value = cell.getRichStringCellValue().getString();
	            break;
	        case NUMERIC:
				if("m/d/yy".equals(cell.getCellStyle().getDataFormatString())) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					value = format.format(cell.getDateCellValue());
				}
	            else if("General".equals(cell.getCellStyle().getDataFormatString())) {
	                value = df.format(cell.getNumericCellValue());
	            } else {
	                value = df2.format(cell.getNumericCellValue());
	            }
	            break;
	        case BOOLEAN:
	            value = cell.getBooleanCellValue();
	            break;
	        case BLANK:
	            value = "";
	            break;
	        default:
	            break;
	    }
	    try{
	        return value.toString();
	    }catch (Exception e) {
	    	return "";
		}
	}

    public static void fillExcelHead(Sheet sheet, Map<String,Object> map){
    	try {
			Row row = sheet.createRow( (short) 0 );
			Object[] colName = map.keySet().toArray();
			for (int i = 0; i < colName.length; i++) {
				row.createCell( i ).setCellValue( colName[i].toString() );
			}
		}catch (Exception e){
		}
	}

	public static void zzsSqlListMapToExcelFile_Judge(String file, List<Map<String, Object>> listmap,String matchName, String mbtname, String judgename){
		Workbook wb = null;
		FileOutputStream fileOut = null;
		try{
			wb = new HSSFWorkbook();
			Sheet sheet = wb.createSheet("Sheet1");

			Row row = sheet.createRow( (short) 0 );
			Cell cell = row.createCell(0);
			cell.setCellValue(matchName+ "专家评分表");
			cell.setCellStyle(createCellStyle(wb, 14));
			mergeRegion(sheet, 0, 0, 0,4);

			row = sheet.createRow( (short) 1 );
			cell = row.createCell(0);
			cell.setCellValue("组别："+mbtname);
			cell.setCellStyle(createCellStyle(wb, 10));
			mergeRegion(sheet, 1, 1, 0,1);

			cell = row.createCell(3);
			cell.setCellValue("专家姓名："+judgename);
			cell.setCellStyle(createCellStyle(wb, 10));
			mergeRegion(sheet, 1, 1, 3,4);

			try {
				row = sheet.createRow( (short) 2 );
				Object[] colName = listmap.get(0).keySet().toArray();
				for (int i = 0; i < colName.length; i++) {
					row.createCell( i ).setCellValue( colName[i].toString() );
				}
			}catch (Exception e){
				;
			}

			int size = listmap.size();

			// zj  add   condition =  and  i-1
			for (int i = 1; i <= size; i++) {
				row = sheet.createRow(i + 2);
				Object[] rowval = listmap.get( i-1 ).values().toArray();
				for (int j=0;j<rowval.length;j++){
					try{
						row.createCell(j).setCellValue(rowval[j].toString().trim());
					}catch (Exception e) {
						// TODO: handle exceptions
					}
				}
			}

			row = sheet.createRow( (short) (4+size) );
			cell = row.createCell(3);
			cell.setCellValue("专家签名：");
			cell.setCellStyle(createCellStyle(wb, 10));
			mergeRegion(sheet, (4+size), (4+size), 3,4);

			row = sheet.createRow( (short) (5+size) );
			cell = row.createCell(3);
			cell.setCellValue("年   月   日");
			cell.setCellStyle(createCellStyle(wb, 10));
			mergeRegion(sheet, (5+size), (5+size), 3,4);

			fileOut = new FileOutputStream(file);
			try {
				wb.write(fileOut);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}catch (Exception e) {
			// TODO: handle exceptions
		}finally {
			if (null != fileOut){
				try {
					fileOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
				fileOut = null;
			}
			if (null != wb){
				try {
					wb.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
				wb = null;
			}
		}
	}

	/**
	 * 创建自定义单元格样式
	 *
	 * @param workbook 工作簿
	 */
	private static CellStyle createCellStyle(Workbook workbook, int size) {
		// 为单元格设置边框线
		CellStyle cellStyle = workbook.createCellStyle();
//		cellStyle.setBorderTop(BorderStyle.THIN);
//		cellStyle.setBorderBottom(BorderStyle.THIN);
//		cellStyle.setBorderLeft(BorderStyle.THIN);
//		cellStyle.setBorderRight(BorderStyle.THIN);
		// 居中显示
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		//创建一个字体
		Font font = workbook.createFont();
		font.setFontHeightInPoints((short) size);

		cellStyle.setFont(font);

		return cellStyle;
	}

	/**
	 * 合并区域
	 *
	 * @param sheet    sheet页
	 * @param firstRow 起始行
	 * @param lastRow  结束行
	 * @param firstCol 起始列
	 * @param lastCol  结束列
	 */
	private static void mergeRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
		if (firstRow == lastRow && firstCol == lastCol) {
			return;
		}
		CellRangeAddress cra = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
		sheet.addMergedRegion(cra);
	}

	public static void zzsSqlListMapToExcelFile(String file, List<Map<String, Object>> listmap){
		Workbook wb = null;
		FileOutputStream fileOut = null;
		try{
			wb = new HSSFWorkbook();
			Sheet sheet = wb.createSheet("Sheet1");
			fillExcelHead( sheet,listmap.get( 0 ) );

			// zj  add   condition =  and  i-1
			for (int i = 1; i <= listmap.size(); i++) {
				Row row = sheet.createRow(i);
				Object[] rowval = listmap.get( i-1 ).values().toArray();
				for (int j=0;j<rowval.length;j++){
					try{
						row.createCell(j).setCellValue(rowval[j].toString().trim());
					}catch (Exception e) {
						// TODO: handle exceptions
					}
				}
			}

			fileOut = new FileOutputStream(file);
			try {
				wb.write(fileOut);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}catch (Exception e) {
			// TODO: handle exceptions
		}finally {
			if (null != fileOut){
				try {
					fileOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
				fileOut = null;
			}
			if (null != wb){
				try {
					wb.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
				wb = null;
			}
		}
	}

	/**
	 * 20210528 bql添加
	 * fromRow 开始行
	 * toRow 结束行（若结束行不定，此参数写为0即可）
	 * */
//	public static String[] getExcelToMSSqlSqlArray(String file,String tableName,String[] dbFields,String[] dbFieldsType,String[] addFeilds,String[] addValues,String[] addTypes,int fromRow,int toRow,int fromCol,int toCol) throws Exception{
//		File excel = new File(file);
//		InputStream fis = new FileInputStream(excel);
//		Workbook wbook = WorkbookFactory.create(fis); //new XSSFWorkbook(fis);//
//		Sheet wsheet = wbook.getSheetAt(0);
//		wsheet.setForceFormulaRecalculation(true);
//
////		int rowNum = wsheet.getLastRowNum() + 1;
//		int rowNum = toRow == 0 ? (wsheet.getLastRowNum() - fromRow + 1) : toRow - fromRow + 1;//行数为开始行到结束行+1
////		System.out.println("rowNum=" + rowNum);
////		int colNum = wsheet.getRow(0).getLastCellNum();//列数
//		int colNum = toCol - fromCol + 1;//列数
////		System.out.println("colNum=" + colNum);
////		//Read the headers first. Locate the ones you need
////		Row rowHeader = wsheet.getRow(0);
////		for (int j = 0; j < colNum; j++) {
////			Cell cell = rowHeader.getCell(j);
////			String cellValue = cellToString(cell);
////			for (int k=0; k<sCols.length;k++){
////				if(sCols[k].equalsIgnoreCase(cellValue)){
////					indexCol[k] = j;
////					break;
////				}
////			}
////		}
//
//		String[] sqlList = new String[rowNum];
//		//sqlConst：字段名、valueConst：字段数据
//		String sqlConst = "",valueConst="";//"Insert Into " + tableName + " (";
//
//		for (int i = 0; i < dbFields.length - 1; i ++){
//			sqlConst += dbFields[i] + ",";
//		}
//		sqlConst += dbFields[dbFields.length - 1];
//
//		if (addFeilds.length > 0)
//			for (int i = 0; i < addFeilds.length; i ++)
//				sqlConst += ("," + addFeilds[i]);
//
//		sqlConst = "Insert Into " + tableName + " ("+ sqlConst + ") Values (";
//
//		for (int i = 1; i < rowNum + 1; i++) {
//			valueConst = "";
//			Row row = wsheet.getRow(fromRow + i - 1);
//			String[] colValue = new String[colNum];
//			for (int j = 0; j < colNum; j ++){
////				System.out.println("j=" + j);
////				System.out.println("row.getCell(indexCol[j])=" + row.getCell(indexCol[j]));
////				System.out.println("row.getCell(indexCol[j])=" + row.getCell(fromCol + j));
////				System.out.println("row.getCell(fromCol + j)==null " + row.getCell(fromCol + j) == null);
//				try{
////					colValue[j] = cellToString(row.getCell(indexCol[j]));
//					colValue[j] = cellToString(row.getCell(fromCol + j));
//				}catch(Exception e){
//					colValue[j] = "";
//				}
//
//				if (dbFieldsType[j].equalsIgnoreCase("STRING")){
//					valueConst += ("'" + colValue[j] + "',");
//				} else if (dbFieldsType[j].equalsIgnoreCase("NUMBER")) {
//					if(colValue[j].trim().equals("")){
//						valueConst += (null + ",");
//					}else{
//						valueConst += (colValue[j] + ",");
//					}
//				}else if(dbFieldsType[j].equalsIgnoreCase("DATE")){
//					if(colValue[j].trim().equals("")){
//						valueConst += (null + ",");
//					}else{
//						valueConst += ("'" + colValue[j] + "',");
//					}
//				} else{
//					valueConst += ("'" + colValue[j] + "',");
//				}
//			}
//
//			if (valueConst.charAt(valueConst.length() - 1) == ',') {
//				valueConst = valueConst.substring(0, valueConst.length() - 1);
//			}
//
//			if (addFeilds.length > 0){
//				for (int j = 0; j < addFeilds.length; j ++){
//					if (addTypes[j].equalsIgnoreCase("STRING")){
//						valueConst += (",'" + addValues[j] + "'");
//					} else if (addTypes[j].equalsIgnoreCase("NUMBER")) {
//						valueConst += ("," + addValues[j]);
//					} else{
//						valueConst += (",'" + addValues[j] + "'");
//					}
//				}
//			}
//
//			sqlList[i - 1] = sqlConst + valueConst + ")";
////			System.out.println("sqlList[" + (i -1) + "]===" + sqlList[i - 1]);
//		}
//		fis.close();
//		try{
//			wbook.close();
//		}catch(Exception e){wbook = null;}
//		return sqlList;
//	}

//	public static String[] getExcelToMSSqlSqlArray_Criteria (String file,String mproid) throws Exception{
//		File excel = new File(file);
//		InputStream fis = new FileInputStream(excel);
//		Workbook wbook = WorkbookFactory.create(fis); //new XSSFWorkbook(fis);//
//		Sheet wsheet = wbook.getSheetAt(0);
//		wsheet.setForceFormulaRecalculation(true);
//		List<String> list = new ArrayList<>();
//
//		int rowNum = wsheet.getLastRowNum() - 9;
//		int colNum = wsheet.getRow(9).getLastCellNum() - 1;
//
//		String sqlConst1 = "",valueConst1 = "";//"Insert Into " + tableName + " (";
//		String[] dbFieldsType1 = {"String","String","String","String","String","String","String","Number"};
//
//		String SubCriteriaID = "",SubCriteriaDes = "";
//		int countJ = 0;
//		for (int i = 10; i < rowNum + 10; i++) {
//			sqlConst1 = "Insert Into matchcaimporttmp (mccode,mscdes,mcajtype,mcades,mcaptype,mjgear,mcaextrades,maxscore,mproid) " +
//					"Values (";
//			Row row = wsheet.getRow(i);
//			String cell0 = null,cell1 = null,cell2 = null;
//			try {
//				cell0 = cellToString(row.getCell(0));
//				cell1 = cellToString(row.getCell(1));
//			} catch (Exception e) {
//				cell0 = "";
//				cell1 = "";
//			}
//			if (!StringUtils.isEmpty(cell0)) {
//				SubCriteriaID = cell0;
//				SubCriteriaDes = cell1;
//				countJ = 0;
//				i ++;
//			}
//
//			row = wsheet.getRow(i);
//			try {
//				cell2 = cellToString(row.getCell(2));
//			} catch (Exception e) {
//				cell2 = "";
//			}
//
//			cell2 = cell2.equals("测量") ? "M" : "J";
//
//			valueConst1 = "";
//			valueConst1 = valueConst1 + "'" + SubCriteriaID + "','" + SubCriteriaDes + "','" + cell2 +"',";
//
//			String val = "";
//			for (int k = 3; k < colNum; k ++) {
//				try {
//					val = cellToString(row.getCell(k));
//				} catch (Exception e) {
//					val = "";
//				}
//
//				//评判类型为J
//				if (k == 5 && cell2.equals("J")) {
//					countJ ++;
//
//					String mjgear = "",mjmarkdes = "";
//					for (int m = 0; m < 4; m ++) {
//
//						try {
//							mjgear = cellToString(wsheet.getRow(i + m + 1).getCell(5));
//							mjmarkdes = cellToString(wsheet.getRow(i + m + 1).getCell(6));
//						} catch (Exception e) {
//							mjgear = "";
//							mjmarkdes = "";
//						}
//
//						String theSql = "insert into matchcajmimporttmp (mccode,mcajtype,mjgear,mjmarkdes,mproid) " +
//								"values ('" + SubCriteriaID + "','" + cell2 + countJ + "','" + mjgear + "','" +
//								mjmarkdes + "'," + mproid + ")";
//						list.add(theSql);
//					}
//					val = "";
//				}
//
//				if (dbFieldsType1[k].equalsIgnoreCase("STRING")){
//					valueConst1 += ("'" + val + "',");
//				} else if (dbFieldsType1[k].equalsIgnoreCase("NUMBER")) {
//					if(val.trim().equals("")){
//						valueConst1 += (null + ",");
//					}else{
//						valueConst1 += (val + ",");
//					}
//				}else if(dbFieldsType1[k].equalsIgnoreCase("DATE")){
//					if(val.trim().equals("")){
//						valueConst1 += (null + ",");
//					}else{
//						valueConst1 += ("'" + val + "',");
//					}
//				} else {
//					valueConst1 += ("'" + val + "',");
//				}
//			}
//			String maxscore = "";
//			try {
//				maxscore = cellToString(wsheet.getRow(i).getCell(7));
//			} catch (Exception e) {
//				maxscore = "";
//			}
//			sqlConst1 += valueConst1 + maxscore + "," + mproid + ")";
////			sqlConst1 += valueConst1 + maxscore + mproid + ")";
//			list.add(sqlConst1);
//
//			if (cell2.equals("J"))
//				i += 4;//遇到评分类型为J的，向下加5行
//		}
//
////		System.out.println("----------------------------------------");
////		list.forEach(System.out::println);
//
//		try{
//			wsheet = null;
//			wbook.close();
//			fis.close();
//			excel = null;
//		}catch(Exception e){
//			wsheet = null;
//			wbook = null;
//			fis = null;
//			excel = null;
//		}
//		return list.toArray(new String[0]);
//	}


	/**
	 * 20211204 yt
	 * file 文件路径
	 * tableName 导入临时表表名
	 * dbFields 临时表字段名
	 * dbFieldsType 临时表字段数据类型
	 * sCols Excel表列名
	 * addFeilds 添加的字段名（不需要从Excel中读取，但需要写入临时表的字段名）
	 * addValues 添加的字段值
	 * addTypes 添加的字段的数据类型
	 * 将带有合并单元格的Excel表导入
	 * */
	public static String[] getExcelWithMergeSqlArray(String file,String tableName,String[] dbFields,String[] dbFieldsType,String[] sCols,String[] addFeilds,String[] addValues,String[] addTypes) throws Exception{
		File excel = new File(file);
		InputStream fis = new FileInputStream(excel);
		Workbook wbook = WorkbookFactory.create(fis); //new XSSFWorkbook(fis);//
		Sheet wsheet = wbook.getSheetAt(0);
		//合并单元格数量
		for(int i = wsheet.getNumMergedRegions() -1 ; i >= 0; i--) {
			//合并单元格位置(地址)
			CellRangeAddress range = wsheet.getMergedRegion(i);
			//拆分单元格
			wsheet.removeMergedRegion(i);
			int firstColumn = range.getFirstColumn();
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			Cell cfirst = wsheet.getRow(firstRow).getCell(firstColumn);
			//填充被拆分单元格的值
			for (int m = firstRow + 1; m <= lastRow; m++) {
				Cell tc = wsheet.getRow(m).getCell(firstColumn);
				tc.setCellStyle(cfirst.getCellStyle());
				tc.setCellValue(cfirst.getStringCellValue());
			}
		}
		wsheet.setForceFormulaRecalculation(true);

		int rowNum = wsheet.getLastRowNum() + 1;
		int colNum = wsheet.getRow(0).getLastCellNum();
		int[] indexCol = new int[sCols.length];
		//Read the headers first. Locate the ones you need
		Row rowHeader = wsheet.getRow(0);
		for (int j = 0; j < colNum; j++) {
			Cell cell = rowHeader.getCell(j);
			String cellValue = cellToString(cell);
			for (int k=0; k<sCols.length;k++){
				if(sCols[k].equalsIgnoreCase(cellValue)){
					indexCol[k] = j;
					break;
				}
			}
		}

		String[] sqlList = new String[rowNum - 1];
		String sqlConst = "",valueConst="";//"Insert Into " + tableName + " (";

		for (int i=0;i<dbFields.length - 1;i++){
			sqlConst += dbFields[i] + ",";
		}
		sqlConst += dbFields[dbFields.length - 1];
		if (addFeilds.length > 0)
			for (int i = 0;i<addFeilds.length;i++)
				sqlConst += (","+ addFeilds[i]);

		sqlConst = "Insert Into " + tableName + " ("+ sqlConst + ") Values(";

		for (int i = 1; i < rowNum; i++) {
			valueConst = "";
			Row row = wsheet.getRow(i);
			String[] colValue = new String[sCols.length];
			for (int j=0;j<sCols.length;j++){
				try{
					colValue[j] = cellToString(row.getCell(indexCol[j]));
				}catch(Exception e){
					colValue[j] = "";
				}

				if (dbFieldsType[j].equalsIgnoreCase("STRING")){
					valueConst += ("'" + colValue[j]+"',");
				}
				else if (dbFieldsType[j].equalsIgnoreCase("NUMBER")) {
					if(colValue[j].trim().equals("")){
						valueConst += (null+",");
					}else{
						valueConst += (colValue[j]+",");
					}
				}else if(dbFieldsType[j].equalsIgnoreCase("DATE")){
					if(colValue[j].trim().equals("")){
						valueConst += (null+",");
					}else{
						valueConst += ("'" + colValue[j]+"',");
					}
				}
				else{
					valueConst += ("'" + colValue[j]+"',");
				}
			}
			if (valueConst.charAt(valueConst.length() - 1) == ',') {
				valueConst = valueConst.substring(0, valueConst.length() - 1);
			}
			if (addFeilds.length > 0){
				for (int j=0;j<addFeilds.length;j++){
					if (addTypes[j].equalsIgnoreCase("STRING")){
						valueConst += (",'" + addValues[j]+"'");
					}
					else if (addTypes[j].equalsIgnoreCase("NUMBER")) {
						valueConst += (","+ addValues[j]);
					}
					else{
						valueConst += (",'" + addValues[j]+"'");
					}
				}
			}
			sqlList[i - 1] = sqlConst + valueConst+")";
		}
		fis.close();
		try{
			wbook.close();
		}catch(Exception e){wbook = null;}
		return sqlList;
	}


	//用于理论成绩导入
//	public static String[] getExcelToMSSqlSqlArray_Score (String file,String matid,String mproid,String[] mcid) throws Exception{
//		File excel = new File(file);
//		InputStream fis = new FileInputStream(excel);
//		Workbook wbook = WorkbookFactory.create(fis); //new XSSFWorkbook(fis);//
//		Sheet wsheet = wbook.getSheetAt(0);
//		wsheet.setForceFormulaRecalculation(true);
//		List<String> list = new ArrayList<>();
//
//		int rowNum = wsheet.getLastRowNum() + 1;//行数
//		int colNum = wsheet.getRow(0).getLastCellNum();//列数
//
//		String sqlConst1 = "",valueConst1 = "";//"Insert Into " + tableName + " (";
//
//		sqlConst1 = "Insert Into matchtheoryscoretmp (matid,mproid,mcid,membername,mppnum,munit,mcscore) " +
//				"Values (";
//		for (int i = 1; i < rowNum; i++) {
//			Row row = wsheet.getRow(i);
//			String membername = null,//选手姓名
//					mppnum = null,//选手编号
//					munit = null;//参赛单位
//			try {
//				membername = cellToString(row.getCell(0));
//				mppnum = cellToString(row.getCell(1));
//				munit = cellToString(row.getCell(2));
//			} catch (Exception e) {
//				membername = "";
//				mppnum = "";
//				munit = "";
//			}
//
//			String mcscore = "";
//			for (int j = 3; j < colNum; j ++){
//				valueConst1 = "";
//				valueConst1 = valueConst1 + matid + "," + mproid + "," + mcid[j - 3] + ",'" + membername + "','" + mppnum + "','" + munit + "',";
//				try {
//					mcscore = cellToString(row.getCell(j));
//				} catch (Exception e) {
//					mcscore = "";
//				}
//
//				if(mcscore.trim().equals("")){
//					mcscore = null;
//				}
//
//				valueConst1 += mcscore + ")";
//				list.add(sqlConst1 + valueConst1);
//			}
//		}
//
////		System.out.println("----------------------------------------");
////		list.forEach(System.out::println);
//
//		try{
//			wsheet = null;
//			wbook.close();
//			fis.close();
//			excel = null;
//		}catch(Exception e){
//			wsheet = null;
//			wbook = null;
//			fis = null;
//			excel = null;
//		}
//		return list.toArray(new String[0]);
//	}
}
