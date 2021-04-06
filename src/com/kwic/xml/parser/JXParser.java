package com.kwic.xml.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.io.SAXReader;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.jaxen.XPath;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom4j.Dom4jXPath;

/**
 * XPath Query를 사용하는 Xml 조작 Utility
 *
 * caution) jaxen engine에서 prefix가 없는 namespace를 인식하지 못하는 경우가 자주 발생한다.
 * 				ex)&lt;web-app version="2.4" xmlns="http://java.sun.com/xml/ns/j2ee"&gt;
 * 					--&gt;&lt;web-app version="2.4" xmlns:web="http://java.sun.com/xml/ns/j2ee"&gt;
 * 
 * 			1.root element에 prefix가 없는 namespace가 있는 경우 반드시 아래와 같이 조치한다.
 * 				ex) Element[] arr	= jxp.getElements(jxp.getNS()+"servlet/"+jxp.getNS()+"init-param");
 * 
 *  		2. 기타 element에 prefix가 없는 namespace가 있는 경우
 *  			ex) String xmlns	= element.getNamespaceURI();
 *  				String prefix	= element.getNamespacePrefix();
 *  				if( xmlns!=null && !"".equals(xmlns) && (prefix==null || "".equals(prefix)) )
 *  					jxp.setNamespace("sampleprefix",xmlns);
 *  				Element[] arr	= jxp.getElements("sampleprefix:servlet/sampleprefix:init-param");
*
* Class        : JXParser.java
* @program     : JXParser
* @description : XPath Query를 사용하는 Xml 조작 Utility
*
* @author      : 기웅정보통신
* @update      : 2010.10.18
* @package     : korealife.ma.cfp.com.xml
* @see
* @required     : dom4j.jar Specification-Version: 1.3,Implementation-Version: 1.3
* @DBTable
*/

public class JXParser implements Serializable {
  private static final long serialVersionUID 	= 1L;
  public static final String STANDARD_ENCODING  = "UTF-8";
  private String encoding                     	= "UTF-8";
  private String defaultNamespace				= "";
  private final String DEFAULT_NAMESPACE		= "jxpNS";
  
  /**
  * dom4j document
  */
  private Document xmlDoc;
  /**
  * xpath base element
  */
  private Element baseElement;
  /**
  * name space context
  */
  private SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();

  /**<pre>
  * default constructor
  * create empty dom document
  * </pre>
  * @throws DocumentException
 * @throws JaxenException 
  */
  public JXParser() throws DocumentException, JaxenException {
    this("<?xml version=\"1.0\" encoding=\""+STANDARD_ENCODING+"\" ?><root></root>");
  }
  /**<pre>
  * constructor
  * xml 문자열을 입력받아 xml document생성
  * </pre>
  * @param xmlText xml문자열
  * @throws DocumentException
 * @throws JaxenException 
  */
  public JXParser(String xmlText) throws DocumentException, JaxenException {
    loadXML(xmlText.trim());
  }
  /**<pre>
  * constructor
  * 파일 경로를 입력받아 xml document생성
  * </pre>
  * @param url 파일 경로
  * @param isURL 해당 문자열이 파일경로인지를 지정
  * @throws DocumentException
 * @throws JaxenException 
  */
  public JXParser(String url, boolean isURL) throws DocumentException,FileNotFoundException,IOException, JaxenException {
    if (!isURL)
        loadXML(url.trim());
    else
        loadXML(new java.io.File(url.trim()));
  }
  /**<pre>
  * constructor
  * 파일을 입력받아 xml document생성
  * </pre>
  * @param xmlFile 파일
  * @throws DocumentException
 * @throws JaxenException 
  */
  public JXParser(java.io.File xmlFile) throws DocumentException,FileNotFoundException,IOException, JaxenException {
    loadXML(xmlFile);
  }

  public void setEncoding(String encoding) {
    this.encoding   = encoding;
  }
  /**
  * xml문자열을 입력받아 xml document생성
  *@param xmlText : xml 문자열
  *@throws DocumentException
  *@return JXParser
 * @throws JaxenException 
  */
  public final JXParser loadXML(String xmlText) throws DocumentException, JaxenException {
    DocumentException exception = null;
    try {
      xmlDoc      = DocumentHelper.parseText(xmlText);
    } catch (DocumentException ex) {
      exception   = ex;
    }
    if (exception!= null) {
      int idx     = xmlText.indexOf("<!DOCTYPE");
      int idx2    = xmlText.indexOf(">", idx);
      if (idx<0) {
        throw exception;
      } else {
        xmlText     = xmlText.substring(0,idx)+xmlText.substring(idx2+1);
        xmlDoc      = DocumentHelper.parseText(xmlText);
      }
    }

    baseElement = xmlDoc.getRootElement();
    makeDefaultNamespacePrefix();
    return this;
  }
  /**
  * 파일을 입력받아 xml document생성
  *@param xmlFile xml 파일
  *@throws DocumentException
  *@return JXParser
 * @throws JaxenException 
  */
  public final JXParser loadXML(java.io.File xmlFile) throws DocumentException,FileNotFoundException,IOException, JaxenException {
    if (!xmlFile.exists())
        throw new DocumentException("There is no xml-file ["+xmlFile.getAbsolutePath()+"].");
    else if (!xmlFile.isFile())
        throw new DocumentException("It's not a xml file ["+xmlFile.getAbsolutePath()+"].");

    DocumentException exception = null;

    SAXReader reader    = new SAXReader();
    try {
      xmlDoc      = reader.read(xmlFile);
    } catch (DocumentException ex) {
      exception   = ex;
    }

    if (exception!= null) {
      StringBuffer sb = new StringBuffer();
      
      String line     = "";
      String xml      = "";
      String _line    = System.getProperty("line.separator");
      
      java.io.BufferedReader br   = null;
      try{
    	  br	= new java.io.BufferedReader(new java.io.FileReader(xmlFile));
	      while ((line = br.readLine())!= null) {
	        sb.append(line).append(_line);
	      }
      }catch(Exception e){
    	  throw e;
      }finally{
    	  try{if(br!=null)br.close();}catch(Exception ex){}
      }
      
      
      int idx     = sb.indexOf("<!DOCTYPE");
      int idx2    = sb.indexOf(">", idx);
      if (idx<0) {
        throw exception;
      } else {
        xml     = sb.substring(0,idx)+sb.substring(idx2+1);
        return loadXML(xml);
      }
    }

    baseElement = xmlDoc.getRootElement();
    makeDefaultNamespacePrefix();
    return this;
  }
  /**
   * 생성된 dom4j document를 반환한다.
  *@return Document dom4j document
  */
  public Document getDocument() {
    return xmlDoc;
  }
  /**
   * 생성된 document의 root Element를 반환한다.
  *@return Element document element
  */
  public Element getRootElement() {
    return xmlDoc.getRootElement();
  }
  /**
  * root Element를 기준 Element를 설정한다.
  * @throws JaxenException
  *@return JXParser
  */
  public synchronized JXParser setBaseElement() throws JaxenException {
    return setBaseElement(getRootElement());
  }
  /**<pre>
  * Xpath의 기준 Element를 설정한다.
  * 최초 BaseElement는 root element이다.
  * 최초의 root Element를 기준으로 디시 잡기위헤서는
  *  setBaseElement() 사용
  *   ex) j2x.setBaseElement("//BusinessArea");
  *</pre>
  *@param xpathText base로 지정할 element의 xpath
  * @throws JaxenException
  *@return JXParser
  */
  public synchronized JXParser setBaseElement(String xpathText) throws JaxenException {
    XPath xpath         = getXPath(xpathText);
    Element bsElement   = (Element) (xpath.selectSingleNode(xmlDoc));
    return setBaseElement(bsElement);
  }
  /**<pre>
  * 기준 Element를 설정한다.
  * 최초 BaseElement는 root element이다.
  * 최초의 root Element를 기준으로 다시 잡기위해서는
  *  setBaseElement() 사용
  *</pre>
  *@param bsElement base로 지정할 element
  * @throws JaxenException
  *@return JXParser
  */
  public synchronized JXParser setBaseElement(Element bsElement) throws JaxenException {
    if (bsElement == null)
        throw new JaxenException("BASE ELEMENT is Null.");
    this.baseElement    = bsElement;
    return this;
  }
  /**<pre>
  * 현재 base element를 기준으로하는 xpath로 element 반환
  *   ex) j2x.getElement("//BusinessArea/HUNM_INF[2]/CUST_CONT_RTP");
  *</pre>
  *@param xpathText xpath 문자열
  * @throws JaxenException
  *@return Element
  */
  public synchronized Element getElement(String xpathText) throws JaxenException {
    if (xpathText == null || "".equals(xpathText)) {
      return xmlDoc.getRootElement();
    }
    XPath xpath         = getXPath(xpathText);
    Node node           = (Node) (xpath.selectSingleNode(baseElement));
    if (node == null)
        return null;
    if (node.getNodeType()!= Node.ELEMENT_NODE)
        throw new JaxenException("It's not an ELEMENT NODE.");
    return (Element) node;
  }
  /**<pre>
  * 부모 element를 기준으로하는 xpath로 element 반환
  * </pre>
  *@param parentElement 기준이되는 부모element
  *@param xpathText 부모element를 기준으로 작성된 xpath문자열
  * @throws JaxenException
  *@return Element
  */
  public synchronized Element getElement(Element parentElement, String xpathText) throws JaxenException {
    XPath xpath         = getXPath(xpathText);
    Node node           = (Node) (xpath.selectSingleNode(parentElement));
    setBaseElement();
    if (node == null)
        return null;
    if (node.getNodeType()!= Node.ELEMENT_NODE)
        throw new JaxenException("It's not an ELEMENT NODE.");
    return (Element) node;
  }
  /**<pre>
  * element의 xpath반환
  * </pre>
  *@param element xpath를 추출할 element
  * @throws JaxenException
  *@return String element의 xpath
  */
  public synchronized String getAbsolutePath(Element element) throws JaxenException {
    return getAbsolutePath(element,true);
  }
  /**<pre>
  * element의 절대 xpath반환
  * </pre>
  *@param element xpath를 추출할 element
  *@param unique
  *<pre>unique xpath 여부
  *   (ex: flase=/root/user/ssn , true=/root/user[2]/ssn)
  *</pre>
  * @throws JaxenException
  *@return String element의 xpath
  */
  public synchronized String getAbsolutePath(Element element, boolean unique) throws JaxenException {
    if (unique)
        return element.getUniquePath();
    else
        return element.getPath();
  }
  /**
  * element의 상대 xpath반환
  *@param element xpath를 추출할 element
  * @throws JaxenException
  *@return String xpath
  */
  public synchronized String getRelativePath(Element element) throws JaxenException {
    return getRelativePath(element, true);
  }
  /**
  * element의 xpath반환
  *@param element xpath를 추출할 element
  *@param unique
  *<pre>unique xpath 여부
  *   (ex: flase=//root/user/ssn , true=//root/user[2]/ssn)
  *</pre>
  * @throws JaxenException
  *@return String xpath
  */
  public synchronized String getRelativePath(Element element, boolean unique) throws JaxenException {
    if (unique)
        return element.getUniquePath(this.baseElement);
    else
        return element.getPath(this.baseElement);
  }
  /**<pre>
  * 현재 base element를 기준으로하는 xpath로 해당 elements 반환
  *   ex) j2x.getElements("//BusinessArea/HUNM_INF");
  *</pre>
  *@param xpathText xpath 문자열
  * @throws JaxenException
  *@return Element[] 검색된 모든 element
  */
  public synchronized Element[] getElements(String xpathText) throws JaxenException {
    return getElements(this.baseElement,xpathText);
  }
  /**
  * 부모 element를 기준으로하는 xpath로 해당 elements 반환
  *@param parentXpathText 부모 element의 xpath
  *@param xpathText xpath 문자열
  * @throws JaxenException
  *@return Element[] 검색된 모든 element
  */
  public synchronized Element[] getElements(String parentXpathText, String xpathText) throws JaxenException {
    return getElements(getElement(parentXpathText),xpathText);
  }
  /**
  * 부모 element를 기준으로하는 xpath로 해당 elements 반환
  *@param parentElement 부모 element
  *@param xpathText xpath 문자열
  * @throws JaxenException
  *@return Element[] 검색된 모든 element
  */
  public synchronized Element[] getElements(Element parentElement, String xpathText) throws JaxenException {
    XPath xpath             = getXPath(xpathText);
    java.util.List<?> list		= xpath.selectNodes(parentElement);
    setBaseElement();
    Element[] elements      = new Element[list.size()];
    for (int i = 0; i<list.size(); i++) {
      elements[i]         = (Element) (list.get(i));
    }
    return elements;
  }
  
  /**
   * 자식element 배열 반환
   *@param parentElement 부모 element
   * @throws JaxenException
   *@return Element[] 검색된 모든 자식 element
   */
   public synchronized Element[] getChilds(Element parentElement) throws JaxenException {
	   List<?> list	= parentElement.elements();
	   
	   Element[] elements      = new Element[list.size()];
	   for (int i = 0; i<list.size(); i++) {
		   elements[i]         = (Element) (list.get(i));
	   }
	   return elements;
   }
  
  
  /**
  * 부모 element를 기준으로하는 xpath로 반복횟수반환
  *@param parentXpathText 부모 element xpath
  *@param childElementName 반복되는 자식 element name
  * @throws JaxenException
  *@return int 반복횟수
  */
  public synchronized int getLoopCount(String parentXpathText, String childElementName) throws JaxenException {
    return getLoopCount(getElement(parentXpathText),childElementName);
  }
  /**
  * 부모 element를 기준으로하는 xpath로 반복횟수반환
  *@param parentElement 부모 element
  *@param childElementName 반복되는 자식 element name
  * @throws JaxenException
  *@return int 반복횟수
  */
  public synchronized int getLoopCount(Element parentElement, String childElementName) throws JaxenException {
    return getElements(parentElement,childElementName).length;
  }
  /**
  * 부모element에 자식 노드를 추가한다.
  *@param parentXpathText : 부모 node xpath
  *@param childElementName : 추가할 자식 element name
  *@throws JaxenException
  *@return Element 추가된 자식 element
  */
  public synchronized Element addElement(String parentXpathText, String childElementName) throws JaxenException {
    return addElement(getElement(parentXpathText), childElementName);
  }
  /**
  * 입력된 부모노드에 자식 노드를 추가한다.
  *@param parentElement : 부모 Element
  *@param childElementName : 추가할 자식 element name
  *@return Element 추가된 자식 element
  *@throws JaxenException
  */
  public synchronized Element addElement(Element parentElement, String childElementName) throws JaxenException {
    return parentElement.addElement(childElementName);
  }
  /**
  * 입력된 부모노드 xpath에 자식 노드를 추가한다.
  *@param parentXpathText : 부모 node xpath
  *@param childElement : 추가할 자식 element
  *@return Element 추가된 자식 element
  *@throws JaxenException
  */
  public synchronized Element addElement(String parentXpathText, Element childElement) throws JaxenException {
    return addElement(getElement(parentXpathText), childElement);
  }
  /**
  * 입력된 부모노드 xpath에 자식 노드를 추가한다.
  *@param parentElement : 부모 element
  *@param childElement : 추가할 자식 element
  *@return Element 추가된 자식 element
  *@throws JaxenException
  */
  public synchronized Element addElement(Element parentElement, Element childElement) throws JaxenException {
    parentElement.add(childElement);
    return childElement;
  }
  /**
  * 다른 parent element 또는 document의 Element 를 import한다..
  *@param parentElement : 부모 element
  *@param childElement : 추가할 자식 element
  *@return Element 추가된 자식 element
  *@throws JaxenException
  */
  public synchronized Element importElement(Element parentElement, Element childElement) throws JaxenException {
    parentElement.add((Element) (childElement.clone()));
    return childElement;
  }
  /**
  * xpath에 해당하는 element를 삭제한다.
  *@param childXpathText : 삭제할 자식 element xpath
  *@return JXParser
  *@throws JaxenException
  */
  public synchronized JXParser removeElement(String childXpathText) throws JaxenException {
    return removeElement(getElement(childXpathText));
  }
  /**
  * 주어진 element를 삭제한다.
  *@param child : 삭제할 자식 element
  *@return JXParser
  *@throws JaxenException
  */
  public synchronized JXParser removeElement(Element child) throws JaxenException {
    child.getParent().remove(child);
    return this;
  }
  /**
  * 주어진 xpath에 해당하는 모든 elements를 삭제한다.
  *@param childXpathText : 삭제할 자식 elements xpath
  *@return JXParser
  *@throws JaxenException
  */
  public synchronized JXParser removeElements(String childXpathText) throws JaxenException {
    return removeElements(getElements(childXpathText));
  }
  /**
  * 주어진 모든 elements를 삭제한다.
  *@param childs 삭제할 element 목록
  *@return JXParser
  *@throws JaxenException
  */
  public synchronized JXParser removeElements(Element[] childs) throws JaxenException {
    for (int i = 0; i<childs.length; i++) {
      removeElement(childs[i]);
    }
    return this;
  }
  /**
  * 주어진 xpath에 해당하는 element의 text value를 추출한다.
  *@param xpath text value를 추출할 element의 xpath
  *@return java.lang.String text value
  *@throws JaxenException
  */
  public synchronized String getValue(String xpath) throws JaxenException {
    return getValue(getElement(xpath));
  }
  /**
  * 주어진 element의 text value를 추출한다.
  *@param element text value를 추출할 element
  *@return java.lang.String text value
  *@throws JaxenException
  */
  public synchronized String getValue(Element element) throws JaxenException {
    if (element == null)
        return null;
    return element.getText();
  }
  /**
  * 주어진 xpath에 해당하는 element에 text value를 할당한다.
  *@param xpath : text value를 할당할 element xpath
  *@param val : 할당할 값
  *@return Element text value를 할당한 element
  *@throws JaxenException
  */
  public synchronized Element setValue(String xpath, String val) throws JaxenException {
    String value    = (val == null)?"":val;
    return setValue(getElement(xpath),value);
  }
  /**
  * 주어진 element에 text value를 할당한다.
  *@param element : text value를 할당할 element
  *@param val : 할당할 값
  *@return Element text value를 할당한 element
  *@throws JaxenException
  */
  public synchronized Element setValue(Element element, String val) throws JaxenException {
    String value    = (val == null)?"":val;
    if (element == null)
        throw new JaxenException("Can not find an ELEMENT.");
    element.setText(value);
    return element;
  }
  public synchronized Element setCData(String xpath, String val) throws JaxenException {
	    String value    = (val == null)?"":val;
	    return setCData(getElement(xpath),value);
  }
  public synchronized Element setCData(Element element, String val) throws JaxenException {
	  String value    = (val == null)?"":val;
	    if (element == null)
	        throw new JaxenException("Can not find an ELEMENT.");
	    List<?> childs	= element.content();
	    for(int i=childs.size()-1;i>=0;i--){
	    	if(childs.get(i) instanceof CDATA)
		    	element.remove((CDATA)childs.get(i));
	    	else if(childs.get(i) instanceof Text)
		    	element.remove((Text)childs.get(i));
	    }
	    CDATA cdata	= DocumentHelper.createCDATA(value);
	    element.add(cdata);
	    return element;
	  }
  /**<pre>
  * 주어진 xpath에 해당하는 attribute의 속성값 반환.
  *   ex) j2x.getAttribute("Element_1/Element_2/@Attribute_1");
  *</pre>
  *@param attributeXpathText : attribute xpath
  *@return java.lang.String 속성값
  *@throws JaxenException
  */
  public synchronized String getAttribute(String attributeXpathText) throws JaxenException {
    XPath xpath         = getXPath(attributeXpathText);
    return getAttribute((Attribute) (xpath.selectSingleNode(baseElement)));

  }
  /**<pre>
  * 주어진 xpath에 해당하는 element의 주어진 attribute name에 해당하는 속성값 반환.
  *   ex) j2x.getAttribute("Element_1/Element_2","Attribute_1");
  *</pre>
  *@param parentElementXpathText : 부모 element xpath
  *@param attributeName : attribute name
  *@return java.lang.String
  *@throws JaxenException
  */
  public synchronized String getAttribute(String parentElementXpathText,String attributeName) throws JaxenException {
    XPath xpath         = getXPath(parentElementXpathText);
    Attribute attr      = ((Element) (xpath.selectSingleNode(baseElement))).attribute(attributeName);
    return getAttribute(attr);
  }
  /**<pre>
  * 주어진 element의 주어진 attribute name에 해당하는 속성값 반환.
  *   ex) j2x.getAttribute(Element_1,"Attribute_1");
  *</pre>
  *@param parentElement : 부모 element
  *@param attributeName : attribute name
  *@return java.lang.String 속성값
  *@throws JaxenException
  */
  public synchronized String getAttribute(Element parentElement,String attributeName) throws JaxenException {
    Attribute attr      = parentElement.attribute(attributeName);
    return getAttribute(attr);
  }
  /**
  * 주어진 attribute의 속성값 반환.
  *@param attribute  : attribute
  *@return java.lang.String 속성값
  *@throws JaxenException
  */
  public synchronized String getAttribute(Attribute attribute) throws JaxenException {
    return attribute == null?"":attribute.getValue();
  }
  /**
  * xpath에 해당하는 부모 element에 주어진 이름의 속성 추가.
  *@param parentElementXpathText : parent Element XPath
  *@param attributeName : 추가할 속성 name
  *@return Attribute 추가된 Attribute element
  *@throws JaxenException
  */
  public synchronized Attribute addAttribute(String parentElementXpathText,String attributeName) throws JaxenException {
    return addAttribute(getElement(parentElementXpathText),attributeName);
  }
  /**
  * 주어진 element에 속성추가.
  *@param parentElement : parent Element
  *@param attributeName : 추가할 속성 name
  *@return Attribute 추가된 Attribute element
  *@throws JaxenException
  */
  public synchronized Attribute addAttribute(Element parentElement,String attributeName) throws JaxenException {
    if (parentElement == null) {
      throw new JaxenException("Parent Element is null.");
    }
    parentElement.addAttribute(attributeName, "");
    return parentElement.attribute(attributeName);
  }
  /**
  * parentElementXpathText에 해당하는 element의 속성에 값 지정.
  *@param parentElementXpathText : 부모 element xpath
  *@param attributeName : 속성명
  *@param attributeValue : 속성 값
  *@return JXParser
  *@throws JaxenException
  */
  public synchronized JXParser setAttribute(String parentElementXpathText, String attributeName, String attributeValue) throws JaxenException {
    return setAttribute(getElement(parentElementXpathText),attributeName,attributeValue);
  }
  /**
  * parentElementXpathText에 해당하는 element의 속성에 값 지정.
  *@param parentElement : 부모 element
  *@param attributeName : 속성명
  *@param attributeValue : 속성 값
  *@return JXParser
  *@throws JaxenException
  */
  public synchronized JXParser setAttribute(Element parentElement, String attributeName, String attributeValue) throws JaxenException {
    if (parentElement == null) {
      throw new JaxenException("Parent Element is null.");
    }
    Attribute attr  = parentElement.attribute(attributeName);
    if (attr == null) {
      attr    = addAttribute(parentElement,attributeName);
    }
    return setAttribute(attr,attributeValue);
  }
  /**
  * attributeXpathText에 해당하는 속성에 값 지정.
  *@param attributeXpathText : 속성의 xpath
  *@param attributeValue : 속성 값
  *@return JXParser
  *@throws JaxenException
  */
  public synchronized JXParser setAttribute(String attributeXpathText, String attributeValue) throws JaxenException {
    XPath xpath         = getXPath(attributeXpathText);
    return setAttribute((Attribute) (xpath.selectSingleNode(baseElement)),attributeValue);
  }
  /**
  * 주어진 attribute에 값 지정.
  *@param attribute : 속성
  *@param attributeValue : 속성 값
  *@return JXParser
  *@throws JaxenException
  */
  public synchronized JXParser setAttribute(Attribute attribute, String attributeValue) throws JaxenException {
    if (attribute == null) {
      throw new JaxenException("Attribute is null. You can resolve this problem using method [setAttribute(String parentElementXpathText, String attributeName , String attributeValue)]");
    }
    attribute.setValue(attributeValue);
    return this;
  }
  /**
  * Element를 xml 문자열로 변환
  *@param node : 문자열로 변환할 element
  *@return java.lang.String
  *@throws JaxenException
  */
  public String toString(Node node) throws JaxenException {
    String xml  = null;
    if (node == null)
        xml     = xmlDoc.asXML();
    else
        xml     = node.asXML();

    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\" encoding=\"").append(encoding).append("\"?>")
        .append(xml.substring(xml.indexOf("?>")+2));

    return sb.toString();
  }
  /**
   * Element를 xml 문자열로 변환
   *@param node : 문자열로 변환할 element
   *@return java.lang.String
   *@throws JaxenException
   */
   public String toString(Node node,boolean addDeclare) throws JaxenException {
     String xml  = null;
     if (node == null)
         xml     = xmlDoc.asXML();
     else
         xml     = node.asXML();

     StringBuffer sb = new StringBuffer();
     if(addDeclare)
	     sb.append("<?xml version=\"1.0\" encoding=\"").append(encoding).append("\"?>")
	         .append(xml.substring(xml.indexOf("?>")+1));
     else
         sb.append(xml.substring(xml.indexOf("?>")+1));
    	 
     return sb.toString();
   }
  /**
  * XPath Object 생성.
  *@param xpathText : xpath 문자열
  *@return org.jaxen.XPath xpath문자열에 해당하는 XPath객체
  *@throws JaxenException
  */
  public XPath getXPath(String xpathText) throws JaxenException {
    XPath xpath = new Dom4jXPath(xpathText);
    xpath.setNamespaceContext(namespaceContext);
    return xpath;
  }
  /**
  * namespace 셋팅.
  *@param prefix : namespace prefix
  *@param uri : namespace uri
  *@return JXParser
  *@throws JaxenException
  */
  public synchronized JXParser setNamespace(String prefix, String uri) throws JaxenException {
    namespaceContext.addNamespace(prefix, uri);
    return this;
  }

  /**
  * 기준 element전에 주어진 element를 추가한다.
  *@param baseXpath : 기준 element xpath
  *@param newElementName : 추가할 element name
  *@return Element 추가된 element
  *@throws JaxenException
  */
  public synchronized Element insertBefore(String baseXpath, String newElementName) throws JaxenException {
    return insertBefore(getElement(baseXpath),addElement(getRootElement(),newElementName));
  }
  /**
  * 기준 element전에 주어진 element를 추가한다.
  *@param baseXpath : 기준 element xpath
  *@param newElement : 추가할 element
  *@return Element 추가된 element
  *@throws JaxenException
  */
  public synchronized Element insertBefore(String baseXpath, Element newElement) throws JaxenException {
    return insertBefore(getElement(baseXpath),newElement);
  }
  /**
  * 기준 element전에 주어진 element를 추가한다.
  *@param baseElement : 기준 element
  *@param newElementName : 추가할 element name
  *@return Element 추가된 element
  *@throws JaxenException
  */
  public synchronized Element insertBefore(Element baseElement, String newElementName) throws JaxenException {
    return insertBefore(baseElement,addElement(getRootElement(),newElementName));
  }

  /**
  * 기준 element전에 주어진 element를 추가한다.
  *@param baseElement : 기준 element
  *@param newElement : 추가할 element
  *@return Element 추가된 element
  *@throws JaxenException
  */
  public synchronized Element insertBefore(Element baseElement, Element newElement) throws JaxenException {
    Element parentElement   = baseElement.getParent();
    Element newElement2     = (Element) newElement.clone();
    List<?> list       = parentElement.elements();
    List<Element> cloneList  = new ArrayList<Element>(list.size());

    for (int i = 0; i<list.size(); i++) {
      if (baseElement == list.get(i))
          cloneList.add(newElement2);
      if (newElement!= list.get(i))
          cloneList.add((Element) ((Element) (list.get(i))).clone());
    }
    removeElement(newElement);

    for (int i = list.size()-1; i>= 0; i--) {
      if (newElement != list.get(i))
          removeElement(((Element) (list.get(i))));
    }
    for (int i = 0; i<cloneList.size(); i++) {
      addElement(parentElement, ((Element) (cloneList.get(i))));
    }
    return newElement2;
  }

  /**
  * 기존 element전에 주어진 element로 대체한다.
  *@param oldElement : 기존 element
  *@param newElement : 추가할 element
  *@return Element 추가된 element
  *@throws JaxenException
  */
  public synchronized Element replace(Element oldElement, Element newElement) throws JaxenException {

    Element parentElement   = oldElement.getParent();
    Element newElement2     = (Element) newElement.clone();

    List<?> list               = parentElement.elements();
    List<Element> cloneList          = new ArrayList<Element>(list.size());

    for (int i = 0; i<list.size(); i++) {
      if (oldElement == list.get(i)) {
        cloneList.add(newElement2);
      } else {
        if (newElement!= list.get(i))
            cloneList.add((Element) ((Element) (list.get(i))).clone());
      }
    }
    removeElement(newElement);
    for (int i = list.size()-1; i>= 0; i--) {
      if (newElement!= list.get(i))
          removeElement(((Element) (list.get(i))));
    }
    for (int i = 0; i<cloneList.size(); i++) {
      addElement(parentElement, ((Element) (cloneList.get(i))));
    }
    return newElement2;
  }

  private void makeDefaultNamespacePrefix() throws JaxenException{
	  Element root	= xmlDoc.getRootElement();
	  String xmlns	= root.getNamespaceURI();
	  String prefix	= root.getNamespacePrefix();
	  
	  if( xmlns!=null && !"".equals(xmlns) && (prefix==null || "".equals(prefix)) )
		  setNamespace(DEFAULT_NAMESPACE,xmlns).defaultNamespace	= DEFAULT_NAMESPACE+":";
  }
  
  public String getNS(){
	  return defaultNamespace;
  }
  
  public static void main(String[] args) throws DocumentException, JaxenException, FileNotFoundException, IOException{
	  JXParser jxp	= new JXParser(new File("C:/eGovFrame/eGovFrameDev-3.5.0-64bit/workspace/HanaCardMQDeliverer/config/hometax-accounts.xml"));
	  System.out.println(jxp.toString(null));
//	  Element[] arr	= jxp.getElements(jxp.getNS()+"servlet/"+jxp.getNS()+"init-param");
//	  
//	  
//	  System.out.println(arr.length);
//	  System.out.println(jxp.getNS());
  }
}
