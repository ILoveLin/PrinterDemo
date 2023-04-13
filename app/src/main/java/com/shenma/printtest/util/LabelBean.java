package com.shenma.printtest.util;

import android.graphics.fonts.Font;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/22 14:58
 * desc：
 */
public class LabelBean {
    /**
     * Order>2</Order>
     * <Left>50</Left>
     * <Right>110</Right>
     * <Top>33</Top>
     * <Bottom>91</Bottom>
     * <Content>徽标</Content>          //logo(02)
     * <Type>徽标</Type>
     * <Font>宋体</Font>
     * <FontSize>225</FontSize>
     * <Bold>1</Bold>
     * <Tilt>0</Tilt>
     * <Color>0</Color>
     * <Alignment>0</Alignment>
     */


    private String Order;
    private String Left;
    private String Right;
    private String Top;
    private String Bottom;
    private String Content;
    private String Type;
    private String Font;
    private String FontSize;
    private String Bold;
    private String Tilt;
    private String Color;
    private String Alignment;


    public String getOrder() {
        return Order;
    }

    public void setOrder(String order) {
        Order = order;
    }

    public String getLeft() {
        return Left;
    }

    public void setLeft(String left) {
        Left = left;
    }

    public String getRight() {
        return Right;
    }

    public void setRight(String right) {
        Right = right;
    }

    public String getTop() {
        return Top;
    }

    public void setTop(String top) {
        Top = top;
    }

    public String getBottom() {
        return Bottom;
    }

    public void setBottom(String bottom) {
        Bottom = bottom;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getFont() {
        return Font;
    }

    public void setFont(String font) {
        Font = font;
    }

    public String getFontSize() {
        return FontSize;
    }

    public void setFontSize(String fontSize) {
        FontSize = fontSize;
    }

    public String getBold() {
        return Bold;
    }

    public void setBold(String bold) {
        Bold = bold;
    }

    public String getTilt() {
        return Tilt;
    }

    public void setTilt(String tilt) {
        Tilt = tilt;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
    }

    public String getAlignment() {
        return Alignment;
    }

    public void setAlignment(String alignment) {
        Alignment = alignment;
    }

    @Override
    public String toString() {
        return "LabelBean{" +
                "Order='" + Order + '\'' +
                ", Left='" + Left + '\'' +
                ", Right='" + Right + '\'' +
                ", Top='" + Top + '\'' +
                ", Bottom='" + Bottom + '\'' +
                ", Content='" + Content + '\'' +
                ", Type='" + Type + '\'' +
                ", Font='" + Font + '\'' +
                ", FontSize='" + FontSize + '\'' +
                ", Bold='" + Bold + '\'' +
                ", Tilt='" + Tilt + '\'' +
                ", Color='" + Color + '\'' +
                ", Alignment='" + Alignment + '\'' +
                '}';
    }
}
