package com.mcx.springmvc;

import com.mcx.springmvc.xml.XmlParse;
import org.dom4j.DocumentException;
import org.junit.Test;

public class XmlParseTest {
    @Test
    public void pareXml() throws DocumentException {
        String aPackage = XmlParse.getPackage("springmvc.xml");
        System.out.println("扫描到的包为："+aPackage);
    }
}
