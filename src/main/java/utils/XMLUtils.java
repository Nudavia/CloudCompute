package utils;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.Objects;
import java.util.logging.Logger;

;
;

/**
 * @Auther: ldw
 * @Date: 2022/3/15
 */
public class XMLUtils {
    private static final String volXMLT = "storage-volume.xml";
    private static final String poolXMLT = "storage-pool.xml";
    private static final String domainXMLT = "domain.xml";
    private static final Logger logger = Logger.getLogger("XMLCreator");

    public static String createVolXML(String volPath, String volName, long capacity) {
        try {
            SAXReader saxReader = new SAXReader();
            String path = Objects.requireNonNull(XMLUtils.class.getClassLoader().getResource(volXMLT)).getPath();
            Document document = saxReader.read(new File(path));
            Node node = document.selectSingleNode("//volume/name");//确保节点是真的唯一
            System.out.println(document.asXML());
            node.setText(volName);
            node = document.selectSingleNode("//volume/capacity");
            node.setText(Long.toString(capacity));
            node = document.selectSingleNode("//volume/target/path");
            node.setText(volPath);
            logger.info(document.asXML());
            logger.info(document.asXML());
            return document.asXML();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String createPoolXML(String poolName, int capacity, String poolPath) {
        try {
            SAXReader saxReader = new SAXReader();
            String path = Objects.requireNonNull(XMLUtils.class.getClassLoader().getResource(poolXMLT)).getPath();
            Document document = saxReader.read(new File(path));
            Node node = document.selectSingleNode("//pool/name");//确保节点是真的唯一
            System.out.println(document.asXML());
            node.setText(poolName);
            node = document.selectSingleNode("//pool/capacity");
            node.setText(Long.toString(capacity));
            node = document.selectSingleNode("//pool/target/path");
            node.setText(poolPath);
            logger.info(document.asXML());
            logger.info(document.asXML());
            return document.asXML();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String createDomainXML(String domainName, long memory, int vcpu, String volPath, String mac) {
        try {
            SAXReader saxReader = new SAXReader();
            String path = Objects.requireNonNull(XMLUtils.class.getClassLoader().getResource(domainXMLT)).getPath();
            Document document = saxReader.read(new File(path));
            Node node = document.selectSingleNode("//domain/name");//确保节点是真的唯一
            node.setText(domainName);

//            node = document.selectSingleNode("//domain/uuid");//确保节点是真的唯一
//            node.setText(uuid);

            node = document.selectSingleNode("//domain/memory");//确保节点是真的唯一
            node.setText(Long.toString(memory));

            node = document.selectSingleNode("//domain/currentMemory");//确保节点是真的唯一
            node.setText(Long.toString(memory));

            node = document.selectSingleNode("//domain/vcpu");//确保节点是真的唯一
            node.setText(Integer.toString(vcpu));

            node = document.selectSingleNode("//domain/devices/disk[@device='disk']/source");//确保节点是真的唯一
            Element element = (Element) (node);
            Attribute attribute = element.attribute("file");
            element.remove(attribute);
            element.addAttribute("file", volPath);

            node = document.selectSingleNode("//domain/devices/interface/mac");//确保节点是真的唯一
            element = (Element) (node);
            attribute = element.attribute("address");
            element.remove(attribute);
            element.addAttribute("address", mac);

            node = document.selectSingleNode("//domain/devices/interface/target");//确保节点是真的唯一
            element = (Element) (node);
            attribute = element.attribute("dev");
            element.remove(attribute);
            element.addAttribute("dev", "vnet" + System.currentTimeMillis());

//            node = document.selectSingleNode("//domain/devices/interface/ip");//确保节点是真的唯一
//            element = (Element) (node);
//            attribute = element.attribute("address");
//            element.remove(attribute);
//            element.addAttribute("address", ip);

            logger.info(document.asXML());
            return document.asXML();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String createDomainXML(String domainName, String domainXML) {
        try {
            SAXReader saxReader = new SAXReader();
            InputStream inputStream = new ByteArrayInputStream(domainXML.getBytes());
            Document document = saxReader.read(new InputStreamReader(inputStream));
            Node node = document.selectSingleNode("//domain/name");//确保节点是真的唯一
            node.setText(domainName);
            return document.asXML();
        } catch (DocumentException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String getVolPath(String domainXML) {
        try {
            SAXReader saxReader = new SAXReader();
            //字符串转流
            InputStream inputStream = new ByteArrayInputStream(domainXML.getBytes());
            Document document = saxReader.read(new InputStreamReader(inputStream));
            Node node = document.selectSingleNode("//domain/devices/disk[@device='disk']/source");//确保节点是真的唯一
            Element element = (Element) (node);
            Attribute attribute = element.attribute("file");
            return attribute.getValue();
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }

    }
}
