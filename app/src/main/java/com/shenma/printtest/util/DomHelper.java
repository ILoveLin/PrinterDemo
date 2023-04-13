//package com.shenma.printtest.util;
//
//import android.content.Context;
//import android.sax.Element;
//import android.util.Log;
//
//import org.bouncycastle.util.test.Test;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
//import java.util.ArrayList;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//
///**
// * company：江西神州医疗设备有限公司
// * author： LoveLin
// * time：2023/3/22 16:26
// * desc：
// */
//public class DomHelper {
//    public static ArrayList<LabelBean> queryXML(Context context) {
//        ArrayList<LabelBean> mDataList = new ArrayList<LabelBean>();
//        try {
//            //①获得DOM解析器的工厂示例:
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            //②从Dom工厂中获得dom解析器
//            DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
//            //③把要解析的xml文件读入Dom解析器
//            Document doc = dbBuilder.parse(context.getAssets().open("一图报表.xml"));
//            Log.e("SAX", "处理该文档的DomImplemention对象~" + doc.getImplementation());
//
//            //④得到文档中名称为person的元素的结点列表
//            NodeList nList = doc.getElementsByTagName("Label");
//            //⑤遍历该集合,显示集合中的元素以及子元素的名字
//            for (int i = 0; i < nList.getLength(); i++) {
//                //先从Person元素开始解析
//                Test personElement = (Test) nList.item(i);
//                Element order = personElement.ge("Order");
//
//                LabelBean.setOrder(attributes.getValue("Order") + "");
//                LabelBean.setLeft(attributes.getValue("Left"));
//                LabelBean.setRight(attributes.getValue("Right"));
//                LabelBean.setTop(attributes.getValue("Top"));
//                LabelBean.setBottom(attributes.getValue("Bottom"));
//                LabelBean.setContent(attributes.getValue("Content") + "");
//                LabelBean.setType(attributes.getValue("Type") + "");
//                LabelBean.setFont(attributes.getValue("Font") + "");
//                LabelBean.setFontSize(attributes.getValue("FontSize"));
//                LabelBean.setBold(attributes.getValue("Bold") + "");
//                LabelBean.setTilt(attributes.getValue("Tilt") + "");
//                LabelBean.setColor(attributes.getValue("Color") + "");
//                LabelBean.setAlignment(attributes.getValue("Alignment") + "");
//
//                //获取person下的name和age的Note集合
//                for (int j = 0; j < childNoList.getLength(); j++) {
//                    Node childNode = childNoList.item(j);
//                    //判断子note类型是否为元素Note
//                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
//                        Element childElement = (Element) childNode;
//                        if ("name".equals(childElement.getNodeName()))
//                            p.setName(childElement.getFirstChild().getNodeValue());
//                        else if ("age".equals(childElement.getNodeName()))
//                            p.setAge(Integer.valueOf(childElement.getFirstChild().getNodeValue()));
//                    }
//                }
//                mDataList.add(p);
//
//
////                for (int i = 0; i < nList.getLength(); i++) {
////                    //先从Person元素开始解析
////                    Element personElement = (Element) nList.item(i);
////                    LabelBean p = new LabelBean();
////                    p.setId(Integer.valueOf(personElement.getAttribute("id")));
////
////                    //获取person下的name和age的Note集合
////                    NodeList childNoList = personElement.getChildNodes();
////                    for (int j = 0; j < childNoList.getLength(); j++) {
////                        Node childNode = childNoList.item(j);
////                        //判断子note类型是否为元素Note
////                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
////                            Element childElement = (Element) childNode;
////                            if ("name".equals(childElement.getNodeName()))
////                                p.setName(childElement.getFirstChild().getNodeValue());
////                            else if ("age".equals(childElement.getNodeName()))
////                                p.setAge(Integer.valueOf(childElement.getFirstChild().getNodeValue()));
////                        }
////                    }
////                    mDataList.add(p);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return mDataList;
//    }
//}