package com.mcx.springmvc.xml;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;

/**
 * 解析xml文件,得到扫描的包名。package：com.mcx.service,com.mcx.controller
 */
public class XmlParse {
    public static String getPackage(String xml) throws DocumentException {
        // 使用dom4j解析xml文件
        SAXReader saxReader = new SAXReader();
        InputStream resourceAsStream = XmlParse.class.getClassLoader().getResourceAsStream(xml);
        System.out.println("InputStream:"+resourceAsStream);
        //得到xml对象
        Document document = saxReader.read(resourceAsStream);
        Element rootElement = document.getRootElement();
        Element element = rootElement.element("component-scan");
        Attribute attribute = element.attribute("base-pack");
        System.out.println("atttibute:"+attribute);
        String text = attribute.getText();
        return text;
    }
}
