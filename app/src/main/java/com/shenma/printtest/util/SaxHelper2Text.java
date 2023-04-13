package com.shenma.printtest.util;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/22 14:55
 * desc：这个是解析A4模板
 */
public class SaxHelper2Text extends DefaultHandler {
    private LabelBean mBean;
    private ArrayList<LabelBean> mBeanList;
    //当前解析的元素标签
    private String tagName = null;

    /**
     * 当读取到文档开始标志是触发，通常在这里完成一些初始化操作
     */
    @Override
    public void startDocument() throws SAXException {
        this.mBeanList = new ArrayList<LabelBean>();
        Log.i("SAX", "读取到文档头,开始解析xml");
    }


    /**
     * 读到一个开始标签时调用,第二个参数为标签名,最后一个参数为属性数组
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        if (localName.equals("Label")) {
            mBean = new LabelBean();
            Log.i("SAX", "开始处理person元素~");
        }
        this.tagName = localName;
    }


    /**
     * 读到到内容,第一个参数为字符串内容,后面依次为起始位置与长度
     */

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        //判断当前标签是否有效
        if (this.tagName != null) {
            String data = new String(ch, start, length);
            //读取标签中的内容
            if (this.tagName.equals("Order")) {
                this.mBean.setOrder(data);
                Log.e("SAX", "处理Order元素内容");
            } else if (this.tagName.equals("Left")) {
                this.mBean.setLeft(data);
                Log.e("SAX", "处理Left元素内容");
            } else if (this.tagName.equals("Right")) {
                this.mBean.setRight(data);
            } else if (this.tagName.equals("Top")) {
                this.mBean.setTop(data);
            } else if (this.tagName.equals("Bottom")) {
                this.mBean.setBottom(data);
            } else if (this.tagName.equals("Content")) {
                this.mBean.setContent(data);
            } else if (this.tagName.equals("Type")) {
                this.mBean.setType(data);
            } else if (this.tagName.equals("Font")) {
                this.mBean.setFont(data);
            } else if (this.tagName.equals("FontSize")) {
                this.mBean.setFontSize(data);
            } else if (this.tagName.equals("Bold")) {
                this.mBean.setBold(data);
            } else if (this.tagName.equals("Tilt")) {
                this.mBean.setTilt(data);
            } else if (this.tagName.equals("Color")) {
                this.mBean.setColor(data);
            } else if (this.tagName.equals("Alignment")) {
                this.mBean.setAlignment(data);
            }

        }

    }

    /**
     * 处理元素结束时触发,这里将对象添加到结合中
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (localName.equals("Label")) {
            this.mBeanList.add(mBean);
            mBean = null;
            Log.i("SAX", "处理person元素结束~");
        }
        this.tagName = null;
    }

    /**
     * 读取到文档结尾时触发，
     */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        Log.i("SAX", "读取到文档尾,xml解析结束");
    }

    //获取persons集合
    public ArrayList<LabelBean> getDataList() {
        return mBeanList;
    }
}
