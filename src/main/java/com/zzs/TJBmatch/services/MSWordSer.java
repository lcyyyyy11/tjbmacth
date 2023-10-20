package com.zzs.TJBmatch.services;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Description 导出word文档
 * @author JasonWu
 * @create 2020-06-27-14:35
 */
@Service
public class MSWordSer {

    private static String title1 = "标题 1";
    private static String title2 = "标题 2";
    private static String title3 = "标题 3";

    public enum ChartType {
        Bar, Line, Pie, Combination
    }

    /**
     * 初始化文档
     */
    public XWPFDocument createXWPFDocument() {
        XWPFDocument doc = XWPFHelper.createDocument();
        XWPFStyles styles = doc.createStyles();
        XWPFHelper.addCustomHeadingStyle(styles,title1,1,16,"000000","黑体");
        XWPFHelper.addCustomHeadingStyle(styles,title2,2,15,"000000","宋体");
        XWPFHelper.addCustomHeadingStyle(styles,title3,3,12,"000000","等线");
        return doc;
    }

    /**
     * 导入word文档模板
     */
    public XWPFDocument read(String path) throws IOException {
        XWPFDocument document = XWPFHelper.openDocument(path);
        return document;
    }

    /**
     * 导出word文档
     */
    public void save(XWPFDocument document,String path) throws IOException {
        XWPFHelper.saveDocument(document, path);
    }

    /**
     * 创建标题1
     */
    public XWPFParagraph createTitle1(XWPFDocument document,String title) {
        XWPFParagraph paragraph = document.createParagraph();    //新建一个标题段落对象
        paragraph.setAlignment(ParagraphAlignment.CENTER);//样式居中
        paragraph.setStyle(title1);
        //设置文本
        XWPFRun titleRun = paragraph.createRun();
        titleRun.setText(title);
        titleRun.setBold(true);      //加粗
        return paragraph;
    }

    /**
     * 创建标题2
     */
    public XWPFParagraph createTitle2(XWPFDocument document,String title) {
        XWPFParagraph paragraph = document.createParagraph();    //新建一个标题段落对象
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setStyle(title2);
        //设置文本
        XWPFRun titleRun = paragraph.createRun();
        titleRun.setText(title);
        titleRun.setBold(false);      //加粗
        return paragraph;
    }

    /**
     * 创建标题3
     */
    public XWPFParagraph createTitle3(XWPFDocument document,String title) {
        XWPFParagraph paragraph = document.createParagraph();    //新建一个标题段落对象
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setStyle(title3);
        //设置文本
        XWPFRun titleRun = paragraph.createRun();
        titleRun.setText(title);
        titleRun.setBold(true);      //加粗
        return paragraph;
    }

    /**
     * 创建图表题注
     */
    public XWPFParagraph createChartTitle(XWPFDocument document,String title) {
        XWPFParagraph paragraph = document.createParagraph();    //新建一个标题段落对象
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        //设置文本
        XWPFRun titleRun = paragraph.createRun();
        titleRun.setText(title);
        titleRun.setFontFamily("黑体");
        titleRun.setFontSize(10);
        return paragraph;
    }

    /**
     * 创建自然段
     */
    public XWPFParagraph createParagraph(XWPFDocument document,String text) {
        XWPFParagraph paragraph = document.createParagraph();    //新建一个段落对象
        //设置段落
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        //设置文本
        XWPFRun run = paragraph.createRun();
        paragraph.setSpacingBeforeLines(5);
        run.setText(text);      //设置内容
        run.setFontFamily("宋体");
        run.setColor("000000"); //设置颜色
        run.setFontSize(12);    //字体大小
        return paragraph;
    }


    /**
     * 创建自然段
     */
    public XWPFParagraph createParagraph(XWPFDocument document,String text, ParagraphAlignment align) {
        XWPFParagraph paragraph = document.createParagraph();    //新建一个段落对象
        //设置段落
        paragraph.setAlignment(align);
        //设置文本
        XWPFRun run = paragraph.createRun();
        paragraph.setSpacingBeforeLines(5);
        run.setText(text);      //设置内容
        run.setFontFamily("宋体");
        run.setColor("000000"); //设置颜色
        run.setFontSize(12);    //字体大小
        return paragraph;
    }

    public XWPFParagraph createParagraph(XWPFDocument document,String text, ParagraphAlignment align, int first ) {
        XWPFParagraph paragraph = document.createParagraph();    //新建一个段落对象
        //设置段落
        paragraph.setAlignment(align);
        //设置文本
        XWPFRun run = paragraph.createRun();
        paragraph.setSpacingBeforeLines(5);
        paragraph.setFirstLineIndent(first);
        paragraph.setSpacingAfterLines(10);
        run.setText(text);      //设置内容
        run.setFontFamily("宋体");
        run.setColor("000000"); //设置颜色
        run.setFontSize(12);    //字体大小
        return paragraph;
    }


    /**
     * 创建自然段，段前空行，段前空  hight
     */
    public XWPFParagraph createParagraphBeforeLines(XWPFDocument document,String text,int hight) {
        XWPFParagraph paragraph = document.createParagraph();    //新建一个段落对象
        //设置段落
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        //设置文本
        XWPFRun run = paragraph.createRun();
//        paragraph.setSpacingBefore(hight);
        paragraph.setSpacingBeforeLines(hight);
        run.setText(text);      //设置内容
        run.setFontFamily("宋体");
        run.setColor("000000"); //设置颜色
        run.setFontSize(12);    //字体大小
        return paragraph;
    }
    /**
     * 创建自然段，段后空行，段后空 hight
     */
    public XWPFParagraph createParagraphAfterLines(XWPFDocument document,String text,int hight) {
        XWPFParagraph paragraph = document.createParagraph();    //新建一个段落对象
        //设置段落
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        //设置文本
        XWPFRun run = paragraph.createRun();
//        paragraph.setSpacingAfter(hight);
        paragraph.setSpacingAfterLines(hight);
        run.setText(text);      //设置内容
        run.setFontFamily("宋体");
        run.setColor("000000"); //设置颜色
        run.setFontSize(12);    //字体大小
        return paragraph;
    }

    /**
     * 创建普通表格
     */
    public XWPFTable createTable(XWPFDocument document, int rows, int cols, String title) {
        //创建表格标题
        this.createChartTitle(document,title);
        //创建表格
        XWPFTable infoTable = document.createTable(rows, cols);
        //8500表格宽度
        XWPFHelper.setTableWidthAndHAlign(infoTable, "8500", STJc.CENTER);

        //设置表格样式
        List<XWPFTableRow> rowList = infoTable.getRows();
        for(int i = 0; i < rowList.size(); i++) {
            XWPFTableRow infoTableRow = rowList.get(i);
            List<XWPFTableCell> cellList = infoTableRow.getTableCells();
            for(int j = 0; j < cellList.size(); j++) {
                XWPFParagraph cellParagraph = cellList.get(j).getParagraphArray(0);
                cellParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun cellParagraphRun = cellParagraph.createRun();
                cellParagraphRun.setFontSize(10);
                cellParagraphRun.setFontFamily("宋体");
            }
        }
        //8500表格行高
        XWPFHelper.setTableHeight(infoTable, 270, STVerticalJc.CENTER);
        return infoTable;
    }




    /**
     * 创建没有标题的表格
     */
    public XWPFTable createTableNoTitle(XWPFDocument document, int rows, int cols) {
        //创建表格
        XWPFTable infoTable = document.createTable(rows, cols);
        XWPFHelper.setTableWidthAndHAlign(infoTable, "8500", STJc.CENTER);

        //设置表格样式
        List<XWPFTableRow> rowList = infoTable.getRows();
        for(int i = 0; i < rowList.size(); i++) {
            XWPFTableRow infoTableRow = rowList.get(i);
            List<XWPFTableCell> cellList = infoTableRow.getTableCells();
            for(int j = 0; j < cellList.size(); j++) {
                XWPFParagraph cellParagraph = cellList.get(j).getParagraphArray(0);
                cellParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun cellParagraphRun = cellParagraph.createRun();
                cellParagraphRun.setFontSize(11);
                cellParagraphRun.setFontFamily("宋体");
            }
        }
        XWPFHelper.setTableHeight(infoTable, 300, STVerticalJc.CENTER);
        return infoTable;
    }

    /**
     * 创建没有标题的表格
     */
    public XWPFTable createTableNoTitle_Style(XWPFDocument document, int rows, int cols) {
        //创建表格
        XWPFTable infoTable = document.createTable(rows, cols);
        XWPFHelper.setTableWidthAndHAlign(infoTable, "8500", STJc.CENTER);

        //设置表格样式
        List<XWPFTableRow> rowList = infoTable.getRows();
        List<ParagraphAlignment> paragraphAlignmentList = new ArrayList<>(3);
        paragraphAlignmentList.add(ParagraphAlignment.LEFT);
        paragraphAlignmentList.add(ParagraphAlignment.CENTER);
        paragraphAlignmentList.add(ParagraphAlignment.RIGHT);
        for(int i = 0; i < rowList.size(); i++) {
            XWPFTableRow infoTableRow = rowList.get(i);
            List<XWPFTableCell> cellList = infoTableRow.getTableCells();
            for(int j = 0; j < cellList.size(); j++) {
                XWPFParagraph cellParagraph = cellList.get(j).getParagraphArray(0);
                cellParagraph.setAlignment(paragraphAlignmentList.get(j));
                XWPFRun cellParagraphRun = cellParagraph.createRun();
                cellParagraphRun.setFontSize(10);
                cellParagraphRun.setFontFamily("宋体");
            }
        }
        XWPFHelper.setTableHeight(infoTable, 270, STVerticalJc.CENTER);
        return infoTable;
    }

    /**
     * 创建有合并项的表格
     */
    public XWPFTable createMergeTable(XWPFDocument document, int rows, int cols, JSONArray merge) {
        //创建表格标题
//        this.createChartTitle(document,title);
        //创建表格
        XWPFTable infoTable = document.createTable(rows, cols);
        XWPFHelper.setTableWidthAndHAlign(infoTable, "8500", STJc.CENTER);

        //合并表格
        JSONObject mergeInfo = null;
        int rowIndex = 0,fromCell = 0,toCell = 0;
        int colIndex = 0,fromRow = 0,toRow = 0;
        if (null != merge) {
            for (int i=0; i<merge.length(); i++){
                mergeInfo = merge.getJSONObject(i);
                rowIndex = mergeInfo.getInt("rowIndex");
                fromCell = mergeInfo.getInt("fromCell");
                toCell = mergeInfo.getInt("toCell");
                colIndex = mergeInfo.getInt("colIndex");
                fromRow = mergeInfo.getInt("fromRow");
                toRow = mergeInfo.getInt("toRow");
                XWPFHelper.mergeCellsHorizontal(infoTable, rowIndex, fromCell, toCell);
                XWPFHelper.mergeCellsVertically(infoTable, colIndex, fromRow, toRow);
            }
        }

//        XWPFHelper.mergeCellsHorizontal(infoTable, 1, 1, 5);
//        XWPFHelper.mergeCellsVertically(infoTable, 0, 3, 6);

        //设置表格样式
        List<XWPFTableRow> rowList = infoTable.getRows();
        for(int i = 0; i < rowList.size(); i++) {
            XWPFTableRow infoTableRow = rowList.get(i);
            List<XWPFTableCell> cellList = infoTableRow.getTableCells();
            for(int j = 0; j < cellList.size(); j++) {
                XWPFParagraph cellParagraph = cellList.get(j).getParagraphArray(0);
                cellParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun cellParagraphRun = cellParagraph.createRun();
                cellParagraphRun.setFontSize(10);
                cellParagraphRun.setFontFamily("宋体");
            }
        }
        XWPFHelper.setTableHeight(infoTable, 270, STVerticalJc.CENTER);
        return infoTable;
    }

    /**
     * 往表格中填充数据
     * 其中每一行数据为一个List
     */
    public void fillTableData(XWPFTable table, List<List<Object>> tableData) {
        List<XWPFTableRow> rowList = table.getRows();
        for(int i = 0; i < tableData.size(); i++) {
            List<Object> list = tableData.get(i);
            List<XWPFTableCell> cellList = rowList.get(i).getTableCells();
            for(int j = 0; j < list.size(); j++) {
                XWPFParagraph cellParagraph = cellList.get(j).getParagraphArray(0);
                XWPFRun cellParagraphRun = cellParagraph.getRuns().get(0);
                cellParagraphRun.setText(String.valueOf(list.get(j)));
            }
        }

        //设置表头背景色
        XWPFTableRow tableOneRowOne = table.getRow(0);
        List<Object> titleList = tableData.get(0);
        for(int i=0; i < titleList.size(); i++){
            XWPFHelper.setCellStyle(tableOneRowOne.getCell(i), "宋体", "11", 1, "", "", "#000000", "#ECECEC", 10, titleList.get(i).toString());
        }
    }

    public void fillTableData(XWPFTable table, List<List<Object>> tableData, List<ParagraphAlignment> paList) {
        List<XWPFTableRow> rowList = table.getRows();
        for(int i = 0; i < tableData.size(); i++) {
            List<Object> list = tableData.get(i);
            List<XWPFTableCell> cellList = rowList.get(i).getTableCells();
            for(int j = 0; j < list.size(); j++) {
                XWPFParagraph cellParagraph = cellList.get(j).getParagraphArray(0);
                cellParagraph.setAlignment(paList.get(j));
                XWPFRun cellParagraphRun = cellParagraph.getRuns().get(0);
                cellParagraphRun.setText(String.valueOf(list.get(j)));
            }
        }

        //设置表头背景色
        XWPFTableRow tableOneRowOne = table.getRow(0);
        List<Object> titleList = tableData.get(0);
        for(int i=0; i < titleList.size(); i++){
            XWPFHelper.setCellStyle(tableOneRowOne.getCell(i), "宋体", "11", 1, "center", "", "#000000", "#ECECEC", 10, titleList.get(i).toString());
        }
    }

    /**
     * 替换文本
     * @param doc
     * @param textMap 替换文本的k-v集合 #{key}
     */
    public void replaceText(XWPFDocument doc, Map<String, String> textMap){
        // 替换段落中的内容
        Iterator<XWPFParagraph> itPara = doc.getParagraphsIterator();
        while (itPara.hasNext()) {
            XWPFParagraph paragraph = (XWPFParagraph) itPara.next();
            List<XWPFRun> runs = paragraph.getRuns();
            for (XWPFRun run : runs) {
                String text = run.getText(0);
                if (text != null) {
                    // 替换文本信息
                    String tempText = text;
                    String key = tempText.replaceAll("#\\{", "").replaceAll("}", "");
                    if (!StringUtils.isEmpty(textMap.get(key))) {
                        run.setText(textMap.get(key), 0);
                    }
                }
            }
        }
        //替换表格中的内容
        Iterator<XWPFTable> itTable = doc.getTablesIterator();
        while (itTable.hasNext()) {
            XWPFTable table = (XWPFTable) itTable.next();
            int rcount = table.getNumberOfRows();
            for (int i = 0; i < rcount; i++) {
                XWPFTableRow row = table.getRow(i);
                List<XWPFTableCell> cells = row.getTableCells();
                for (XWPFTableCell cell : cells) {
                    //表格中处理段落（回车）
                    List<XWPFParagraph> cellParList= cell.getParagraphs();
                    for(int p=0; cellParList!=null&&p<cellParList.size();p++){ //每个格子循环
                        List<XWPFRun> runs = cellParList.get(p).getRuns(); //每个格子的内容都要单独处理
                        for (XWPFRun run : runs) {
                            String text = run.getText(0);
                            if (text != null) {
                                // 替换文本信息 #{}
                                String tempText = text;
                                String key = tempText.replaceAll("#\\{", "").replaceAll("}", "");
                                if (!StringUtils.isEmpty(textMap.get(key))) {
                                    run.setText(textMap.get(key), 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 替换段落中的图片
     * @param doc
     * @param imgName 图片名称 对应模板中的@{key}中的key值
     * @param imgData 图片流数据
     * @param width 图片宽度
     * @param height 图片高度
     */
    public void replaceParagraphImg(XWPFDocument doc, String imgName, FileInputStream imgData, String type, int width, int height) {
        // 替换段落中的图片
        Iterator<XWPFParagraph> itPara = doc.getParagraphsIterator();
        while (itPara.hasNext()) {
            XWPFParagraph paragraph = (XWPFParagraph) itPara.next();
            List<XWPFRun> runs = paragraph.getRuns();
            for (XWPFRun run : runs) {
                String text = run.getText(0);
                if ((null != text && !text.isEmpty()) && text.contains("@{"+imgName+"}")) {
                    try {
//                        run.setText("图片测试", 0);
                        run.setText("", 0);
                        run.addPicture(imgData, XWPFHelper.getPictureType(type), imgName, Units.toEMU(width), Units.toEMU(height));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
    public void replaceImg(XWPFDocument doc,String BeforeText,String imgName, FileInputStream imgData, String type, int width, int height) {
        // 替换段落中的图片
        Iterator<XWPFParagraph> itPara = doc.getParagraphsIterator();
        while (itPara.hasNext()) {
            XWPFParagraph paragraph = (XWPFParagraph) itPara.next();
            List<XWPFRun> runs = paragraph.getRuns();
            for (XWPFRun run : runs) {
                String text = run.getText(0);
                if ((null != text && !text.isEmpty()) && text.contains("@{"+imgName+"}")) {
                    try {
                        run.setText(BeforeText, 0);
                        run.addPicture(imgData, XWPFHelper.getPictureType(type), imgName, Units.toEMU(width), Units.toEMU(height));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
    /**
     * 替换表格中的图片
     * @param doc
     * @param imgName 图片名称 对应模板中的@{key}中的key值
     * @param imgData 图片流数据
     * @param width 图片宽度
     * @param height 图片高度
     */
    public void replaceTableImg(XWPFDocument doc,String imgName, FileInputStream imgData, String type, int width, int height) {
        //替换表格中的图片
        Iterator<XWPFTable> itTable = doc.getTablesIterator();
        while (itTable.hasNext()) {
            XWPFTable table = (XWPFTable) itTable.next();
            int rcount = table.getNumberOfRows();
            for (int i = 0; i < rcount; i++) {
                XWPFTableRow row = table.getRow(i);
                List<XWPFTableCell> cells = row.getTableCells();
                for (XWPFTableCell cell : cells) {
                    //表格中处理段落（回车）
                    List<XWPFParagraph> cellParList= cell.getParagraphs();
                    for(int p=0; cellParList!=null&&p<cellParList.size();p++){ //每个格子循环
                        List<XWPFRun> runs = cellParList.get(p).getRuns(); //每个格子的内容都要单独处理
                        for (XWPFRun run : runs) {
                            String text = run.getText(0);
                            if ((null != text && !text.isEmpty()) && text.contains("@{"+imgName+"}")) {
                                try {
                                    run.setText("", 0);
                                    run.addPicture(imgData, XWPFHelper.getPictureType(type), imgName, Units.toEMU(width), Units.toEMU(height));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 替换表格
     * @param doc
     * @param tableName  表格替换名 ${key}
     * @param titleList  表头字段名
     * @param dataList   数据
     */
    public void replaceTable(XWPFDocument doc,String tableName,List<String> titleList,List<List<Object>> dataList){
        List<XWPFParagraph> paragraphList = doc.getParagraphs();
        doc.getHeaderList();
        if (paragraphList != null && paragraphList.size() > 0) {
            for (XWPFParagraph paragraph : paragraphList) {
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                    String text = run.getText(0);
                    if (text != null) {
                        // 动态表格
                        if (text.contains("${"+tableName+"}")) {
                            run.setText("", 0);
                            XmlCursor cursor = paragraph.getCTP().newCursor();
                            XWPFTable tableOne = doc.insertNewTbl(cursor);

                            // 设置表格宽度
                            tableOne.setWidth(8500);
                            // 表头
                            XWPFTableRow tableOneRowOne = tableOne.getRow(0);
                            XWPFHelper.setCellStyle(tableOneRowOne.getCell(0), "宋体", "9", 1, "", "", "#000000", "#ECECEC", 10, titleList.get(0));
                            for(int i=1; i < titleList.size(); i++){
                                XWPFHelper.setCellStyle(tableOneRowOne.createCell(), "宋体", "9", 1, "", "", "#000000", "#ECECEC", 10, titleList.get(i));
                            }

                            // 数据
                            for (List<Object> list : dataList){
                                XWPFTableRow tableOneRowTwo = tableOne.createRow();
                                XWPFHelper.setCellStyle(tableOneRowTwo.getCell(0), "宋体", "10", 0, "", "", "#000000", "#FFFFFF", 10, list.get(0).toString());
                                for (int i=1; i<list.size(); i++){
                                    XWPFHelper.setCellStyle(tableOneRowTwo.getCell(i), "宋体", "10", 0, "", "", "#000000", "#FFFFFF", 10, list.get(i).toString());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public void replaceTableSJ(XWPFDocument doc,String tableName,List<String> titleList,List<List<Object>> dataList){
        List<XWPFParagraph> paragraphList = doc.getParagraphs();
        doc.getHeaderList();
        if (paragraphList != null && paragraphList.size() > 0) {
            for (XWPFParagraph paragraph : paragraphList) {
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                    String text = run.getText(0);
                    if (text != null) {
                        // 动态表格
                        if (text.contains("${"+tableName+"}")) {
//                        if (text.contains("${评卷}")) {
                            run.setText("", 0);
                            XmlCursor cursor = paragraph.getCTP().newCursor();
                            XWPFTable tableOne = doc.insertNewTbl(cursor);

                            // 设置表格宽度
                            tableOne.setWidth(2000);
                            tableOne.setLeftBorder(XWPFTable.XWPFBorderType.THICK,5,1,"000000");
                            // 表头
                            XWPFTableRow tableOneRowOne = tableOne.getRow(0);
                            tableOneRowOne.setHeight(500);
                            XWPFHelper.setCellStyle(tableOneRowOne.getCell(0), "宋体", "10", 1, "", "", "#000000", "#FFFFFF", 10, titleList.get(0));
                            for(int i=1; i < titleList.size(); i++){
                                XWPFHelper.setCellStyle(tableOneRowOne.createCell(), "宋体", "10", 1, "", "", "#000000", "#FFFFFF", 10, titleList.get(i));
                            }
                            // 数据
                            for (List<Object> list : dataList){
                                XWPFTableRow tableOneRowTwo = tableOne.createRow();
                                tableOneRowTwo.setHeight(500);
                                XWPFHelper.setCellStyle(tableOneRowTwo.getCell(0), "宋体", "10", 0, "", "", "#000000", "#FFFFFF", 10, list.get(0).toString());
                                for (int i=1; i<list.size(); i++){
                                    XWPFHelper.setCellStyle(tableOneRowTwo.getCell(i), "宋体", "10", 0, "", "", "#000000", "#FFFFFF", 10, list.get(i).toString());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 替换表格 不满足条件的数据变红
     * @param doc
     * @param tableName  表格替换名 ${key}
     * @param titleList  表头字段名
     * @param dataList   数据
     */
    public void replaceTableHasColor(XWPFDocument doc,String tableName,List<String> titleList,List<List<Object>> dataList,int startCol,Double yz){
        List<XWPFParagraph> paragraphList = doc.getParagraphs();
        doc.getHeaderList();
        if (paragraphList != null && paragraphList.size() > 0) {
            for (XWPFParagraph paragraph : paragraphList) {
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                    String text = run.getText(0);
                    if (text != null) {
                        // 动态表格
                        if (text.contains("${"+tableName+"}")) {
                            run.setText("", 0);
                            XmlCursor cursor = paragraph.getCTP().newCursor();
                            XWPFTable tableOne = doc.insertNewTbl(cursor);

                            // 设置表格宽度
                            tableOne.setWidth(8500);

                            // 表头
                            XWPFTableRow tableOneRowOne = tableOne.getRow(0);
                            XWPFHelper.setCellStyle(tableOneRowOne.getCell(0), "宋体", "9", 1, "", "", "#000000", "#ECECEC", 10, titleList.get(0));
                            for(int i=1; i < titleList.size(); i++){
                                XWPFHelper.setCellStyle(tableOneRowOne.createCell(), "宋体", "9", 1, "", "", "#000000", "#ECECEC", 10, titleList.get(i));
                            }

                            //表格数据
                            for (List<Object> list : dataList){
                                XWPFTableRow tableOneRowTwo = tableOne.createRow();
                                XWPFHelper.setCellStyle(tableOneRowTwo.getCell(0), "宋体", "10", 0, "", "", "#000000", "#FFFFFF", 10, list.get(0).toString());

                                for (int i=1; i<startCol; i++){
                                    XWPFHelper.setCellStyle(tableOneRowTwo.getCell(i), "宋体", "10", 0, "", "", "#000000", "#FFFFFF", 10, list.get(i).toString());
                                }

                                for (int i=startCol; i<list.size(); i++){
                                    if (ParseDouble(list.get(i).toString()) < yz){
                                        XWPFHelper.setCellStyle(tableOneRowTwo.getCell(i), "宋体", "10", 0, "", "", "#ff0000", "#FFFFFF", 10, list.get(i).toString());
                                    } else {
                                        XWPFHelper.setCellStyle(tableOneRowTwo.getCell(i), "宋体", "10", 0, "", "", "#000000", "#FFFFFF", 10, list.get(i).toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private double ParseDouble(String strNumber) {
        if (strNumber.lastIndexOf("%") > -1){
            NumberFormat f1 = NumberFormat.getPercentInstance();
            Number number = null;
            try {
                number = f1.parse(strNumber);//将百分数转换成Number类型
            } catch (ParseException e) {
                e.printStackTrace();
            }
            double doubleNumber = number.doubleValue();//通过调用nubmer类默认方法直接转换成double
            return doubleNumber;
        } else {
            return Double.parseDouble(strNumber);
        }
    }


    /**
     * 替换doc中的对应图表
     * @param doc
     * @param titleArr  标题
     * @param fldNameArr 字段名（用于绑定数据）
     * @param dataList   数据
     */
    public void replaceChart(XWPFDocument doc,List<String> titleArr,List<String> fldNameArr,List<Map<String,String>> dataList,int position,ChartType chartType) {
        // 获取word模板中的对应图表
        List<POIXMLDocumentPart> chartsList = new ArrayList<>();
        //动态刷新图表
        List<POIXMLDocumentPart> relations = doc.getRelations();
        for (POIXMLDocumentPart poixmlDocumentPart : relations) {
            if (poixmlDocumentPart instanceof XWPFChart) {  // 如果是图表元素
                chartsList.add(poixmlDocumentPart);
            }
        }


        POIXMLDocumentPart poixmlDocumentPart = chartsList.get(position);
        switch (chartType){
            case Line: //折线图
                XWPFHelper.replaceLineCharts(poixmlDocumentPart,titleArr,fldNameArr,dataList);
                break;
            case Bar: //条形图、柱状图
                XWPFHelper.replaceBarCharts(poixmlDocumentPart,titleArr,fldNameArr,dataList);
                break;
            case Pie: //饼图
                XWPFHelper.replacePieCharts(poixmlDocumentPart,titleArr,fldNameArr,dataList);
                break;
        }
    }


    public void createTableParagraph(XWPFDocument document, String tableName){
        XWPFParagraph paragraph = document.createParagraph();    //新建一个段落对象
        //设置段落
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        //设置文本
        XWPFRun run = paragraph.createRun();
        run.setText("${"+ tableName +"}");      //设置内容--表格占位符
    }

    public void createImgParagraph(XWPFDocument document, String imgName){
        XWPFParagraph paragraph = document.createParagraph();    //新建一个段落对象
        //设置段落
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        //设置文本
        XWPFRun run = paragraph.createRun();
        run.setText("@{"+ imgName +"}");      //设置内容--图片占位符
    }

    /**
     * 生成页脚段落
     * @param document
     * @param au 参考学院
     * @param am 参考专业
     * @param ExamName 试卷名称
     * @throws Exception
     */
    public void createFooter(XWPFDocument document, String au,String am, String ExamName) throws Exception {
        /*
         * 生成页脚段落
         * */
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        XWPFHeaderFooterPolicy headerFooterPolicy = new XWPFHeaderFooterPolicy(document, sectPr);
        XWPFHeaderFooter footer =  headerFooterPolicy.createFooter(STHdrFtr.DEFAULT);
        //学院专业段落
        XWPFParagraph paragraph = footer.createParagraph();
        //页码段落
        XWPFParagraph paragraph1 = footer.createParagraph();

        //设置页脚居中
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setVerticalAlignment(TextAlignment.CENTER);
        paragraph1.setAlignment(ParagraphAlignment.CENTER);
        paragraph1.setVerticalAlignment(TextAlignment.CENTER);

        //页脚上横线
//        paragraph.setBorderTop(Borders.THICK);

        CTTabStop tabStop = paragraph.getCTP().getPPr().addNewTabs().addNewTab();
        tabStop.setVal(STTabJc.CENTER);
        CTTabStop tabStop1 = paragraph1.getCTP().getPPr().addNewTabs().addNewTab();
        tabStop1.setVal(STTabJc.CENTER);
//        int twipsPerInch =  1440;
//        tabStop.setPos(BigInteger.valueOf(6 * twipsPerInch));

        /*
         * 给段落创建元素
         * */
        XWPFRun run = paragraph.createRun();
        run.setText((StringUtils.isEmpty(au) ? "" : au)+" " + (StringUtils.isEmpty(am) ? "" : am)+" "+(StringUtils.isEmpty(ExamName) ? "": ExamName));
        setXWPFRunStyle(run,"宋体",10);
        run.addTab();

        /*
         * 生成页码
         * 页码右对齐
         * */
        XWPFRun run1 = paragraph1.createRun();
        run1 = paragraph1.createRun();
        run1.setText("第");
        setXWPFRunStyle(run1,"宋体",10);

        run1 = paragraph1.createRun();
        CTFldChar fldChar = run1.getCTR().addNewFldChar();
        fldChar.setFldCharType(STFldCharType.Enum.forString("begin"));

        run1 = paragraph1.createRun();
        CTText ctText = run1.getCTR().addNewInstrText();
        ctText.setStringValue("PAGE  \\* MERGEFORMAT");
        ctText.setSpace(SpaceAttribute.Space.Enum.forString("preserve"));
        setXWPFRunStyle(run1,"宋体",10);

        fldChar = run1.getCTR().addNewFldChar();
        fldChar.setFldCharType(STFldCharType.Enum.forString("end"));

        run1 = paragraph1.createRun();
        run1.setText("页 (共");
        setXWPFRunStyle(run1,"宋体",10);

        run1 = paragraph1.createRun();
        fldChar = run1.getCTR().addNewFldChar();
        fldChar.setFldCharType(STFldCharType.Enum.forString("begin"));

        run1 = paragraph1.createRun();
        ctText = run1.getCTR().addNewInstrText();
        ctText.setStringValue("NUMPAGES  \\* MERGEFORMAT ");
        ctText.setSpace(SpaceAttribute.Space.Enum.forString("preserve"));
        setXWPFRunStyle(run1,"宋体",10);

        fldChar = run1.getCTR().addNewFldChar();
        fldChar.setFldCharType(STFldCharType.Enum.forString("end"));

        run1 = paragraph1.createRun();
        run1.setText("页)");
        setXWPFRunStyle(run1,"宋体",10);

    }
    /**
     * 设置页脚的字体样式
     * @param r1 段落元素
     */
    private void setXWPFRunStyle(XWPFRun r1,String font,int fontSize) {
        r1.setFontSize(fontSize);
        CTRPr rpr = r1.getCTR().isSetRPr() ? r1.getCTR().getRPr() : r1.getCTR().addNewRPr();
        CTFonts fonts = rpr.isSetRFonts() ? rpr.getRFonts() : rpr.addNewRFonts();
        fonts.setAscii(font);
        fonts.setEastAsia(font);
        fonts.setHAnsi(font);
    }


}
