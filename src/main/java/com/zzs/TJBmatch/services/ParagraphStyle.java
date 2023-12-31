package com.zzs.TJBmatch.services;

/**
 * @author JasonWu
 * @create 2020-06-27-14:53
 */
public class ParagraphStyle {

    //段落间距信息
    private boolean isSpace;  //是否缩进
    private boolean isLine;   //是否启用行距
    private String line;      //行距
    private String before;    //段前磅数
    private String after;     //段后磅数
    private String beforeLines; //段前行数
    private String afterLines;  //段后行数
    //段落缩进信息
    private String firstLine; //首行缩进厘米
    private String firstLineChar; //首行缩进字符
    private String hanging; //悬挂缩进厘米
    private String hangingChar; //悬挂缩进字符
    private String right; //右侧缩进厘米
    private String rightChar; //右侧缩进字符
    private String left; //左侧缩进厘米
    private String leftChar; //左侧缩进字符

    public boolean isSpace() {
        return isSpace;
    }

    public void setSpace(boolean space) {
        isSpace = space;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBeforeLines() {
        return beforeLines;
    }

    public void setBeforeLines(String beforeLines) {
        this.beforeLines = beforeLines;
    }

    public String getAfterLines() {
        return afterLines;
    }

    public void setAfterLines(String afterLines) {
        this.afterLines = afterLines;
    }

    public boolean isLine() {
        return isLine;
    }

    public void setLine(boolean line) {
        isLine = line;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getFirstLine() {
        return firstLine;
    }

    public void setFirstLine(String firstLine) {
        this.firstLine = firstLine;
    }

    public String getFirstLineChar() {
        return firstLineChar;
    }

    public void setFirstLineChar(String firstLineChar) {
        this.firstLineChar = firstLineChar;
    }

    public String getHanging() {
        return hanging;
    }

    public void setHanging(String hanging) {
        this.hanging = hanging;
    }

    public String getHangingChar() {
        return hangingChar;
    }

    public void setHangingChar(String hangingChar) {
        this.hangingChar = hangingChar;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public String getRightChar() {
        return rightChar;
    }

    public void setRightChar(String rightChar) {
        this.rightChar = rightChar;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getLeftChar() {
        return leftChar;
    }

    public void setLeftChar(String leftChar) {
        this.leftChar = leftChar;
    }


}
