package com.zzs.TJBmatch.services;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @Description 操作docx文档的工具类
 * @author JasonWu
 * @create 2020-06-27-14:36
 */
public class XWPFHelper {

    private static Logger log  = LoggerFactory.getLogger( XWPFHelper.class );

    enum ChartType {
        Line,Bar,Pie
    }

    /**
     * 创建一个word对象
     */
    public static XWPFDocument createDocument() {
        XWPFDocument document = new XWPFDocument();
        return document;
    }

    /**
     * 打开word文档
     * @param path 文档所在路径
     */
    public static XWPFDocument openDocument(String path) throws IOException {
        InputStream is = new FileInputStream(path);
        return new XWPFDocument(is);
    }

    /**
     * 保存word文档
     * @param document 文档对象
     * @param savePath 保存路径
     */
    public static void saveDocument(XWPFDocument document, String savePath) throws IOException {
        File file = new File(savePath);

        OutputStream os = null;
        try {
            if (!file.exists()) {
                // 先得到文件的上级目录，并创建上级目录，在创建文件
                File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
//                file.getParentFile().mkdir();
                file.createNewFile();
            }

            //创建文件输出流
            os = new FileOutputStream(file);
            //将字符串转化为字节
            document.write(os);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        OutputStream os = new FileOutputStream(savePath);
//        document.write(os);
//        os.close();
    }

    /**
     * 设置段落间距信息（一行：100、一磅：20）
     */
    public static void setParagraphSpacingInfo(XWPFParagraph paragraph, ParagraphStyle paragraphStyle, STLineSpacingRule.Enum lineValue) {
        CTPPr pPPr = getParagraphCTPPr(paragraph);
        CTSpacing pSpacing = pPPr.getSpacing() != null ? pPPr.getSpacing() : pPPr.addNewSpacing();
        //段前段后设置
        if(paragraphStyle.isSpace()) {
            //段前磅数
            if(paragraphStyle.getBefore() != null) {
                pSpacing.setBefore(new BigInteger(paragraphStyle.getBefore()));
            }
            //段后磅数
            if(paragraphStyle.getAfter() != null) {
                pSpacing.setAfter(new BigInteger(paragraphStyle.getAfter()));
            }
            //段前行数
            if(paragraphStyle.getBeforeLines() != null) {
                pSpacing.setBeforeLines(new BigInteger(paragraphStyle.getBeforeLines()));
            }
            //段后行数
            if(paragraphStyle.getAfterLines() != null) {
                pSpacing.setAfterLines(new BigInteger(paragraphStyle.getAfterLines()));
            }
        }
        //行距设置
        if(paragraphStyle.isLine()) {
            if(paragraphStyle.getLine() != null) {
                pSpacing.setLine(new BigInteger(paragraphStyle.getLine()));
            }
            if(lineValue != null) {
                pSpacing.setLineRule(lineValue);
            }
        }
    }

    /**
     * 设置段落缩进信息（1厘米约等于 567）
     */
    public static void setParagraphIndInfo(XWPFParagraph paragraph, ParagraphStyle paragraphStyle) {
        CTPPr pPPr = getParagraphCTPPr(paragraph);
        CTInd pInd = pPPr.getInd() != null ? pPPr.getInd() : pPPr.addNewInd();
        //首行缩进
        if(paragraphStyle.getFirstLine() != null) {
            pInd.setFirstLine(new BigInteger(paragraphStyle.getFirstLine()));
        }
        if(paragraphStyle.getFirstLineChar() != null) {
            pInd.setFirstLineChars(new BigInteger(paragraphStyle.getFirstLineChar()));
        }
        //悬挂缩进
        if(paragraphStyle.getHanging() != null) {
            pInd.setHanging(new BigInteger(paragraphStyle.getHanging()));
        }
        if(paragraphStyle.getHangingChar() != null) {
            pInd.setHangingChars(new BigInteger(paragraphStyle.getHangingChar()));
        }
        //右侧缩进
        if(paragraphStyle.getRight() != null) {
            pInd.setRight(new BigInteger(paragraphStyle.getRight()));
        }
        if(paragraphStyle.getRightChar() != null) {
            pInd.setRightChars(new BigInteger(paragraphStyle.getRightChar()));
        }
        //左侧缩进
        if(paragraphStyle.getLeft() != null) {
            pInd.setLeft(new BigInteger(paragraphStyle.getLeft()));
        }
        if(paragraphStyle.getLeftChar() != null) {
            pInd.setLeftChars(new BigInteger(paragraphStyle.getLeftChar()));
        }
    }

    /**
     * 设置段落对齐方式
     */
    public static void setParagraphAlignInfo(XWPFParagraph paragraph, ParagraphAlignment pAlign, TextAlignment vAlign) {
        if(pAlign != null) {
            paragraph.setAlignment(pAlign);
        }
        if(vAlign != null) {
            paragraph.setVerticalAlignment(vAlign);
        }
    }

    /**
     * 设置自定义标题样式
     */
    public static void addCustomHeadingStyle(XWPFStyles styles, String strStyleId, int headingLevel, int pointSize, String hexColor, String fontFamily) {

        CTStyle ctStyle = CTStyle.Factory.newInstance();
        ctStyle.setStyleId(strStyleId);

        CTString styleName = CTString.Factory.newInstance();
        styleName.setVal(strStyleId);
        ctStyle.setName(styleName);

        CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
        indentNumber.setVal(BigInteger.valueOf(headingLevel));

        // lower number > style is more prominent in the formats bar
        ctStyle.setUiPriority(indentNumber);

        CTOnOff onoffnull = CTOnOff.Factory.newInstance();
        ctStyle.setUnhideWhenUsed(onoffnull);

        // style shows up in the formats bar
        ctStyle.setQFormat(onoffnull);

        // style defines a heading of the given level
        CTPPr ppr = CTPPr.Factory.newInstance();
        ppr.setOutlineLvl(indentNumber);
        ctStyle.setPPr(ppr);

        XWPFStyle style = new XWPFStyle(ctStyle);

        CTHpsMeasure size = CTHpsMeasure.Factory.newInstance();
        size.setVal(new BigInteger(String.valueOf(pointSize*2)));
        CTHpsMeasure size2 = CTHpsMeasure.Factory.newInstance();
        size2.setVal(new BigInteger("24"));

        CTFonts fonts = CTFonts.Factory.newInstance();
        fonts.setAscii(fontFamily);

        CTRPr rpr = CTRPr.Factory.newInstance();
        rpr.setRFonts(fonts);
        rpr.setSz(size);
        rpr.setSzCs(size2);

        CTColor color= CTColor.Factory.newInstance();
        color.setVal(hexToBytes(hexColor));
        rpr.setColor(color);
        style.getCTStyle().setRPr(rpr);
        // is a null op if already defined

        style.setType(STStyleType.PARAGRAPH);
        styles.addStyle(style);

    }

    /**
     * 跨列合并
     * @param table
     * @param row    所合并的行
     * @param fromCell  起始列
     * @param toCell    终止列
     */
    public static void mergeCellsHorizontal(XWPFTable table, int row, int fromCell, int toCell) {
        for(int cellIndex = fromCell; cellIndex <= toCell; cellIndex++ ) {
            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
            if(cellIndex == fromCell) {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * 跨行合并
     * @param table
     * @param col    合并的列
     * @param fromRow  起始行
     * @param toRow    终止行
     */
    public static void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for(int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            try {
                XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
                //第一个合并单元格用重启合并值设置
                if(rowIndex == fromRow) {
                    cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
                } else {
                    //合并第一个单元格的单元被设置为“继续”
                    cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
                }
            }
            catch (Exception ex) {
                continue;
            }
        }
    }

    /**
     * 设置表格总宽度与水平对齐方式
     */
    public static void setTableWidthAndHAlign(XWPFTable table, String width,
                                              STJc.Enum enumValue) {
        CTTblPr tblPr = getTableCTTblPr(table);
        // 表格宽度
        CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
        if (enumValue != null) {
            CTJc cTJc = tblPr.addNewJc();
            cTJc.setVal(enumValue);
        }
        // 设置宽度
        tblWidth.setW(new BigInteger(width));
        tblWidth.setType(STTblWidth.DXA);
    }

    /**
     * 设置表格行高
     * @param infoTable
     * @param heigth 高度
     * @param vertical 表格内容的显示方式：居中、靠右...
     */
    public static void setTableHeight(XWPFTable infoTable, int heigth, STVerticalJc.Enum vertical) {
        List<XWPFTableRow> rows = infoTable.getRows();
        for(XWPFTableRow row : rows) {
            CTTrPr trPr = row.getCtRow().addNewTrPr();
            CTHeight ht = trPr.addNewTrHeight();
            ht.setVal(BigInteger.valueOf(heigth));
            List<XWPFTableCell> cells = row.getTableCells();
            for(XWPFTableCell tableCell : cells ) {
                CTTcPr cttcpr = tableCell.getCTTc().addNewTcPr();
                cttcpr.addNewVAlign().setVal(vertical);
            }
        }
    }

    public static void setCellStyle(XWPFTableCell cell, String fontFamily, String fontSize, int fontBlod,
                                    String alignment, String vertical, String fontColor,
                                    String bgColor, long cellWidth, String content) {

        BigInteger bFontSize = new BigInteger("24");
        if (fontSize != null && !fontSize.equals("")) {
            BigDecimal fontSizeBD = new BigDecimal(fontSize);
            fontSizeBD = new BigDecimal("2").multiply(fontSizeBD);
            fontSizeBD = fontSizeBD.setScale(0, BigDecimal.ROUND_HALF_UP);//这里取整
            bFontSize = new BigInteger(fontSizeBD.toString());// 字体大小
        }
        //=====获取单元格
        CTTc tc = cell.getCTTc();
        //====tcPr开始====》》》》
        CTTcPr tcPr = tc.getTcPr();//获取单元格里的<w:tcPr>
        if (tcPr == null) {//没有<w:tcPr>，创建
            tcPr = tc.addNewTcPr();
        }

        //  --vjc开始-->>
        CTVerticalJc vjc = tcPr.getVAlign();//获取<w:tcPr>  的<w:vAlign w:val="center"/>
        if (vjc == null) {//没有<w:w:vAlign/>，创建
            vjc = tcPr.addNewVAlign();
        }
        //设置单元格对齐方式
        vjc.setVal(vertical.equals("top")? STVerticalJc.TOP:vertical.equals("bottom")? STVerticalJc.BOTTOM: STVerticalJc.CENTER); //垂直对齐

        CTShd shd = tcPr.getShd();//获取<w:tcPr>里的<w:shd w:val="clear" w:color="auto" w:fill="C00000"/>
        if (shd == null) {//没有<w:shd>，创建
            shd = tcPr.addNewShd();
        }
        // 设置背景颜色
        shd.setFill(bgColor.substring(1));
        //《《《《====tcPr结束====

        //====p开始====》》》》
        CTP p = tc.getPList().get(0);//获取单元格里的<w:p w:rsidR="00C36068" w:rsidRPr="00B705A0" w:rsidRDefault="00C36068" w:rsidP="00C36068">

        //---ppr开始--->>>
        CTPPr ppr = p.getPPr();//获取<w:p>里的<w:pPr>
        if (ppr == null) {//没有<w:pPr>，创建
            ppr = p.addNewPPr();
        }
        //  --jc开始-->>
        CTJc jc = ppr.getJc();//获取<w:pPr>里的<w:jc w:val="left"/>
        if (jc == null) {//没有<w:jc/>，创建
            jc = ppr.addNewJc();
        }
        //设置单元格对齐方式
        jc.setVal(alignment.equals("left")? STJc.LEFT:alignment.equals("right")? STJc.RIGHT: STJc.CENTER); //水平对齐
        //  <<--jc结束--
        //  --pRpr开始-->>
        CTParaRPr pRpr = ppr.getRPr(); //获取<w:pPr>里的<w:rPr>
        if (pRpr == null) {//没有<w:rPr>，创建
            pRpr = ppr.addNewRPr();
        }
        CTFonts pfont = pRpr.getRFonts();//获取<w:rPr>里的<w:rFonts w:ascii="宋体" w:eastAsia="宋体" w:hAnsi="宋体"/>
        if (pfont == null) {//没有<w:rPr>，创建
            pfont = pRpr.addNewRFonts();
        }
        //设置字体
        pfont.setAscii(fontFamily);
        pfont.setEastAsia(fontFamily);
        pfont.setHAnsi(fontFamily);

        CTOnOff pb = pRpr.getB();//获取<w:rPr>里的<w:b/>
        if (pb == null) {//没有<w:b/>，创建
            pb = pRpr.addNewB();
        }
        //设置字体是否加粗
        pb.setVal(fontBlod ==1? STOnOff.ON: STOnOff.OFF);

        CTHpsMeasure psz = pRpr.getSz();//获取<w:rPr>里的<w:sz w:val="32"/>
        if (psz == null) {//没有<w:sz w:val="32"/>，创建
            psz = pRpr.addNewSz();
        }
        // 设置单元格字体大小
        psz.setVal(bFontSize);
        CTHpsMeasure pszCs = pRpr.getSzCs();//获取<w:rPr>里的<w:szCs w:val="32"/>
        if (pszCs == null) {//没有<w:szCs w:val="32"/>，创建
            pszCs = pRpr.addNewSzCs();
        }
        // 设置单元格字体大小
        pszCs.setVal(bFontSize);
        //  <<--pRpr结束--
        //<<<---ppr结束---

        //---r开始--->>>
        List<CTR>  rlist = p.getRList(); //获取<w:p>里的<w:r w:rsidRPr="00B705A0">
        CTR r = null;
        if (rlist != null && rlist.size() > 0) {//获取第一个<w:r>
            r = rlist.get(0);
        }else {//没有<w:r>，创建
            r = p.addNewR();
        }
        //--rpr开始-->>
        CTRPr rpr =  r.getRPr();//获取<w:r w:rsidRPr="00B705A0">里的<w:rPr>
        if (rpr == null) {//没有<w:rPr>，创建
            rpr =  r.addNewRPr();
        }
        //->-
        CTFonts font = rpr.getRFonts();//获取<w:rPr>里的<w:rFonts w:ascii="宋体" w:eastAsia="宋体" w:hAnsi="宋体" w:hint="eastAsia"/>
        if (font == null) {//没有<w:rFonts>，创建
            font = rpr.addNewRFonts();
        }
        //设置字体
        font.setAscii(fontFamily);
        font.setEastAsia(fontFamily);
        font.setHAnsi(fontFamily);

        CTOnOff b = rpr.getB();//获取<w:rPr>里的<w:b/>
        if (b == null) {//没有<w:b/>，创建
            b = rpr.addNewB();
        }
        //设置字体是否加粗
        b.setVal(fontBlod ==1? STOnOff.ON: STOnOff.OFF);
        CTColor color = rpr.getColor();//获取<w:rPr>里的<w:color w:val="FFFFFF" w:themeColor="background1"/>
        if (color == null) {//没有<w:color>，创建
            color = rpr.addNewColor();
        }
        // 设置字体颜色
        if (content.contains("↓")) {
            color.setVal("43CD80");
        }else if (content.contains("↑")) {
            color.setVal("943634");
        }else {
            color.setVal(fontColor.substring(1));
        }
        CTHpsMeasure sz = rpr.getSz();
        if (sz == null) {
            sz = rpr.addNewSz();
        }
        sz.setVal(bFontSize);
        CTHpsMeasure szCs = rpr.getSzCs();
        if (szCs == null) {
            szCs = rpr.addNewSz();
        }
        szCs.setVal(bFontSize);
        //-<-
        //<<--rpr结束--
        List<CTText> tlist = r.getTList();
        CTText t =  null;
        if (tlist != null && tlist.size() > 0) {//获取第一个<w:r>
            t = tlist.get(0);
        }else {//没有<w:r>，创建
            t = r.addNewT();
        }

        t.setStringValue(content);
        //<<<---r结束---
    }

    private static byte[] hexToBytes(String hexString) {
        HexBinaryAdapter adapter = new HexBinaryAdapter();
        byte[] bytes = adapter.unmarshal(hexString);
        return bytes;
    }

    /**
     * 获取图片对应类型
     * @param picType
     * @return int
     */
    public static int getPictureType(String picType) {
        int res = Document.PICTURE_TYPE_PICT;
        if (picType != null) {
            if (picType.equalsIgnoreCase("png")) {
                res = Document.PICTURE_TYPE_PNG;
            } else if (picType.equalsIgnoreCase("dib")) {
                res = Document.PICTURE_TYPE_DIB;
            } else if (picType.equalsIgnoreCase("emf")) {
                res = Document.PICTURE_TYPE_EMF;
            } else if (picType.equalsIgnoreCase("jpg") || picType.equalsIgnoreCase("jpeg")) {
                res = Document.PICTURE_TYPE_JPEG;
            } else if (picType.equalsIgnoreCase("wmf")) {
                res = Document.PICTURE_TYPE_WMF;
            }
        }
        return res;
    }

    /**
     * 得到段落的CTPPr
     */
    public static CTPPr getParagraphCTPPr(XWPFParagraph paragraph) {
        CTPPr pPPr = null;
        if(paragraph.getCTP() != null) {
            if(paragraph.getCTP().getPPr() != null) {
                pPPr = paragraph.getCTP().getPPr();
            } else {
                pPPr = paragraph.getCTP().addNewPPr();
            }
        }
        return pPPr;
    }

    /**
     * 得到XWPFRun的CTRPr
     */
    public static CTRPr getRunCTRPr(XWPFParagraph paragraph, XWPFRun pRun) {
        CTRPr ctrPr = null;
        if(pRun.getCTR() != null) {
            ctrPr = pRun.getCTR().getRPr();
            if(ctrPr == null) {
                ctrPr = pRun.getCTR().addNewRPr();
            }
        } else {
            ctrPr = paragraph.getCTP().addNewR().addNewRPr();
        }
        return ctrPr;
    }

    /**
     * 得到Table的CTTblPr,不存在则新建
     */
    public static CTTblPr getTableCTTblPr(XWPFTable table) {
        CTTbl ttbl = table.getCTTbl();
        // 表格属性
        CTTblPr tblPr = ttbl.getTblPr() == null ? ttbl.addNewTblPr() : ttbl.getTblPr();
        return tblPr;
    }

    /**
     * 调用替换柱状图数据
     */
    public static void replaceBarCharts(POIXMLDocumentPart poixmlDocumentPart,
                                        List<String> titleArr, List<String> fldNameArr, List<Map<String, String>> listItemsByType) {
        XWPFChart chart = (XWPFChart) poixmlDocumentPart;
        chart.setTitleText(titleArr.get(0));
        //根据属性第一列名称切换数据类型
        CTChart ctChart = chart.getCTChart();
        CTPlotArea plotArea = ctChart.getPlotArea();

        CTBarChart barChart = plotArea.getBarChartArray(0);

        barChart.getSerList().clear();
        //刷新内置excel数据
        refreshExcel(chart, listItemsByType, fldNameArr, titleArr);
        //刷新页面显示数据
        refreshBarGraphContent(barChart,listItemsByType,fldNameArr,1,titleArr);

    }


    /**
     * 调用替换折线图数据
     */
    public static void replaceLineCharts(POIXMLDocumentPart poixmlDocumentPart,
                                         List<String> titleArr, List<String> fldNameArr, List<Map<String, String>> listItemsByType) {
        XWPFChart chart = (XWPFChart) poixmlDocumentPart;
        chart.setTitleText(titleArr.get(0));
        //根据属性第一列名称切换数据类型
        CTChart ctChart = chart.getCTChart();
        CTPlotArea plotArea = ctChart.getPlotArea();

        CTLineChart lineChart = plotArea.getLineChartArray(0);
        lineChart.getSerList().clear();

        //刷新内置excel数据
        refreshExcel(chart, listItemsByType, fldNameArr, titleArr);
        //刷新页面显示数据
        refreshLineGraphContent(lineChart,  listItemsByType, fldNameArr, 1,titleArr);

    }


    /**
     * 调用替换饼图数据
     */
    public static void replacePieCharts(POIXMLDocumentPart poixmlDocumentPart,
                                        List<String> titleArr, List<String> fldNameArr, List<Map<String, String>> listItemsByType) {
        XWPFChart chart = (XWPFChart) poixmlDocumentPart;
        chart.setTitleText(titleArr.get(0));
        //根据属性第一列名称切换数据类型
        CTChart ctChart = chart.getCTChart();
        CTPlotArea plotArea = ctChart.getPlotArea();

        CTPieChart pieChart = plotArea.getPieChartArray(0);
        List<CTPieSer> pieSerList = pieChart.getSerList();  // 获取饼图单位

        //刷新内置excel数据
        refreshExcel(chart, listItemsByType, fldNameArr, titleArr);
        //刷新页面显示数据
        refreshPieStrGraphContent(pieChart, pieSerList, listItemsByType, fldNameArr, 1);

    }

    /**
     * 刷新条形、柱状图图形方法
     * @param barChart
     * @param dataList     数据
     * @param fldNameArr   字段名（用于绑定数据）
     * @param position
     * @param titleArr     系列名称
     * @return
     */
    public static boolean refreshBarGraphContent(CTBarChart barChart,
                                                 List<Map<String, String>> dataList,
                                                 List<String> fldNameArr,
                                                 int position,
                                                 List<String> titleArr) {
        boolean result = true;
        int culomnNum = titleArr.size()-1;
        //更新数据区域
        for (int i = 0; i < culomnNum; i++) {
            CTBarSer ctBarSer = barChart.addNewSer();
            ctBarSer.addNewIdx().setVal(i);
            ctBarSer.addNewOrder().setVal(i);

            // 设置柱状图的系列名称
            // 设置标题
            CTSerTx tx = ctBarSer.addNewTx();
            CTStrRef ctStrRef = tx.addNewStrRef();
            CTStrData ctStrData = ctStrRef.addNewStrCache();
            ctStrData.addNewPtCount().setVal(1);
            CTStrVal ctStrVal = ctStrData.addNewPt();
            ctStrVal.setIdx(0);
            ctStrVal.setV(titleArr.get(i + 1));  // 设置系列的名称

            CTAxDataSource cat = ctBarSer.addNewCat();
            CTNumDataSource val = ctBarSer.addNewVal();

            CTStrData strData = cat.addNewStrRef().addNewStrCache();
            CTNumData numData = val.addNewNumRef().addNewNumCache();
            strData.setPtArray((CTStrVal[]) null); // unset old axis text
            numData.setPtArray((CTNumVal[]) null); // unset old values

            // set model
            long idx = 0;
            for (int j = 0; j < dataList.size(); j++) {
                //判断获取的值是否为空
                String value = "0";
                if (new BigDecimal(dataList.get(j).get(fldNameArr.get(i + position))) != null) {
                    value = new BigDecimal(dataList.get(j).get(fldNameArr.get(i + position))).toString();
                }
                if (!"0".equals(value)) {
                    CTNumVal numVal = numData.addNewPt();//序列值
                    numVal.setIdx(idx);
                    numVal.setV(value);
                }
                CTStrVal sVal = strData.addNewPt();//序列名称
                sVal.setIdx(idx);
                sVal.setV(dataList.get(j).get(fldNameArr.get(0)));
                idx++;
            }

            numData.addNewPtCount().setVal(idx);
            strData.addNewPtCount().setVal(idx);
        }
        return result;
    }

    /**
     * 刷新折线图数据方法
     * @param ctLineChart
     * @param dataList
     * @param fldNameArr
     * @param position
     * @param titleArr
     * @return
     */
    public static boolean refreshLineGraphContent(CTLineChart ctLineChart,
                                                  List<Map<String, String>> dataList,
                                                  List<String> fldNameArr,
                                                  int position,
                                                  List<String> titleArr) {
        int culomnNum = titleArr.size()-1;
        boolean result = true;
        //更新数据区域
        for (int i = 0; i < culomnNum; i++) {
            CTLineSer ctLineSer = ctLineChart.addNewSer();
            ctLineSer.addNewIdx().setVal(i);
            ctLineSer.addNewOrder().setVal(i);

            // 设置柱状图的系列名称
            // 设置标题
            CTSerTx tx = ctLineSer.addNewTx();
            CTStrRef ctStrRef = tx.addNewStrRef();
            CTStrData ctStrData = ctStrRef.addNewStrCache();
            ctStrData.addNewPtCount().setVal(1);
            CTStrVal ctStrVal = ctStrData.addNewPt();
            ctStrVal.setIdx(0);
            ctStrVal.setV(titleArr.get(i + 1));  // 设置系列的名称

            CTAxDataSource cat = ctLineSer.addNewCat();
            CTNumDataSource val = ctLineSer.addNewVal();

            CTStrData strData = cat.addNewStrRef().addNewStrCache();
            CTNumData numData = val.addNewNumRef().addNewNumCache();
            strData.setPtArray((CTStrVal[]) null); // unset old axis text
            numData.setPtArray((CTNumVal[]) null); // unset old values

            // set model
            long idx = 0;
            for (int j = 0; j < dataList.size(); j++) {
                //判断获取的值是否为空
                String value = "0";
                if (new BigDecimal(dataList.get(j).get(fldNameArr.get(i + position))) != null) {
                    value = new BigDecimal(dataList.get(j).get(fldNameArr.get(i + position))).toString();
                }
                if (!"0".equals(value)) {
                    CTNumVal numVal = numData.addNewPt();//序列值
                    numVal.setIdx(idx);
                    numVal.setV(value);
                }
                CTStrVal sVal = strData.addNewPt();//序列名称
                sVal.setIdx(idx);
                sVal.setV(dataList.get(j).get(fldNameArr.get(0)));
                idx++;
            }

            numData.addNewPtCount().setVal(idx);
            strData.addNewPtCount().setVal(idx);

        }
        return result;
    }

    /**
     * 刷新饼图数据方法
     *
     * @param typeChart
     * @param serList
     * @param dataList
     * @param fldNameArr
     * @param position
     * @return
     */
    private static boolean refreshPieStrGraphContent(Object typeChart,
                                                    List<?> serList, List<Map<String, String>> dataList, List<String> fldNameArr, int position) {

        boolean result = true;
        //更新数据区域
        for (int i = 0; i < serList.size(); i++) {
            //CTSerTx tx=null;
            CTAxDataSource cat = null;
            CTNumDataSource val = null;
            CTPieSer ser = ((CTPieChart) typeChart).getSerArray(i);
            //tx= ser.getTx();
            // Category Axis Data
            cat = ser.getCat();
            // 获取图表的值
            val = ser.getVal();
            // strData.set
            CTStrData strData = cat.getStrRef().getStrCache();
            CTNumData numData = val.getNumRef().getNumCache();
            strData.setPtArray((CTStrVal[]) null); // unset old axis text
            numData.setPtArray((CTNumVal[]) null); // unset old values
            // set model
            long idx = 0;
            for (int j = 0; j < dataList.size(); j++) {
                //判断获取的值是否为空
                String value = "0";
                if (new BigDecimal(dataList.get(j).get(fldNameArr.get(i + position))) != null) {
                    value = new BigDecimal(dataList.get(j).get(fldNameArr.get(i + position))).toString();
                }
                if (!"0".equals(value)) {
                    CTNumVal numVal = numData.addNewPt();//序列值
                    numVal.setIdx(idx);
                    numVal.setV(value);
                }
                CTStrVal sVal = strData.addNewPt();//序列名称
                sVal.setIdx(idx);
                sVal.setV(dataList.get(j).get(fldNameArr.get(0)));
                idx++;
            }
            numData.getPtCount().setVal(idx);
            strData.getPtCount().setVal(idx);
            //赋值横坐标数据区域
            String axisDataRange = new CellRangeAddress(1, dataList.size(), 0, 0)
                    .formatAsString("Sheet1", true);
            cat.getStrRef().setF(axisDataRange);
            //数据区域
            String numDataRange = new CellRangeAddress(1, dataList.size(), i + position, i + position)
                    .formatAsString("Sheet1", true);
            val.getNumRef().setF(numDataRange);
        }
        return result;
    }
    /**
     * 刷新内置excel数据
     *
     * @param chart
     * @param dataList
     * @param fldNameArr
     * @param titleArr
     * @return
     */
    private static boolean refreshExcel(XWPFChart chart,
                                        List<Map<String, String>> dataList, List<String> fldNameArr, List<String> titleArr) {
        boolean result = true;
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        //根据数据创建excel第一行标题行
        for (int i = 0; i < titleArr.size(); i++) {
            if (sheet.getRow(0) == null) {
                sheet.createRow(0).createCell(i).setCellValue(titleArr.get(i) == null ? "" : titleArr.get(i));
            } else {
                sheet.getRow(0).createCell(i).setCellValue(titleArr.get(i) == null ? "" : titleArr.get(i));
            }
        }
        //遍历数据行
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, String> row = dataList.get(i);//数据行
            //fldNameArr字段属性
            for (int j = 0; j < fldNameArr.size(); j++) {
                if (sheet.getRow(i + 1) == null) {
                    if (j == 0) {
                        try {
                            sheet.createRow(i + 1).createCell(j).setCellValue(row.get(fldNameArr.get(j)) == null ? "" : row.get(fldNameArr.get(j)));
                        } catch (Exception e) {
                            if (row.get(fldNameArr.get(j)) == null) {
                                sheet.createRow(i + 1).createCell(j).setCellValue("");
                            } else {
                                sheet.createRow(i + 1).createCell(j).setCellValue(row.get(fldNameArr.get(j)));
                            }
                        }
                    }
                } else {
                    BigDecimal b = new BigDecimal(row.get(fldNameArr.get(j)));
                    double value = 0d;
                    if (b != null) {
                        value = b.doubleValue();
                    }
                    if (value == 0) {
                        sheet.getRow(i + 1).createCell(j);
                    } else {
                        sheet.getRow(i + 1).createCell(j).setCellValue(b.doubleValue());
                    }
                }
            }
        }
        // 更新嵌入的workbook
        List<POIXMLDocumentPart> pxdList = chart.getRelations();
        if(pxdList!=null&&pxdList.size()>0){
            for(int i = 0;i<pxdList.size();i++){
                if(pxdList.get(i).toString().contains("sheet")){//判断为sheet再去进行更新表格数据
                    POIXMLDocumentPart xlsPart =  pxdList.get(i);
                    OutputStream xlsOut = xlsPart.getPackagePart().getOutputStream();

                    try {
                        wb.write(xlsOut);
                        xlsOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        result = false;
                    } finally {
                        if (wb != null) {
                            try {
                                wb.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                result = false;
                            }
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }
}
