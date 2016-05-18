package com.hkc.xml.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class ElementUtils {

	static ElementUtils elementUtils;

	public static ElementUtils getInstance() {
		if (elementUtils == null) {
			elementUtils = new ElementUtils();
		}
		return elementUtils;
	}

	public Element element(Element element, String elementName) {
		try {
			NodeList elNodeList = element.getElementsByTagName(elementName);
			if (elNodeList != null && elNodeList.getLength() > 0) {
				return (Element) elNodeList.item(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Element> elements(Element element) {
		try {
			NodeList elNodeList = element.getChildNodes();
			if (elNodeList != null && elNodeList.getLength() > 0) {
				return (ArrayList<Element>) elNodeList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Element> elements(Element element, String elementName) {
		try {
			NodeList elNodeList = element.getElementsByTagName(elementName);
			if (elNodeList != null && elNodeList.getLength() > 0) {
				List<Element> elements = new ArrayList<Element>();
				for (int i = 0; i < elNodeList.getLength(); i++) {
					elements.add((Element) elNodeList.item(i));
				}
				return elements;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Iterator<Element> elementIterator(Element element, String elementName) {
		try {
			List<Element> elements = elements(element, elementName);
			if (elements != null) {
				return elements.iterator();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Element> selectNodes(Element e, String nodesURI) {
		try {
			String[] array = nodesURI.split("\\/");
			Element element = null;
			for (int i = 2; i < array.length - 1; i++) {
				if (element == null) {
					element = element(e, array[i]);
				} else {
					element = element(element, array[i]);
				}
			}
			if (element != null) {
			NodeList lists=	element
				.getElementsByTagName(array[array.length - 1]);
				List<Element> elements = new ArrayList<Element>();
				if (lists!=null&&lists.getLength()>0) {
					for (int i = 0; i < lists.getLength(); i++) {
						elements.add((Element)lists.item(i));
					}
					return elements;
				}
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		return null;
	}

	public String attributeValue(Element element, String name) {
		return element.getAttribute(name);
	}

	public List<Attribute> attributes(Element element) {
		NamedNodeMap map = element.getAttributes();
		List<Attribute> attributes = new ArrayList<Attribute>();
		if (map != null) {
			for (int i = 0; i < map.getLength(); i++) {
				Attribute attribute = new Attribute();
				attribute.setName(map.item(i).getNodeName());
				attribute.setValue(map.item(i).getNodeValue());
				attributes.add(attribute);
			}
			return attributes;
		}
		return null;
	}

	public String elementText(Element element, String name) {
		try {
			return element(element, name).getNodeValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
