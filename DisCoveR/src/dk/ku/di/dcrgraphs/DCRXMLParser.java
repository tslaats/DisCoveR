/**
 * @author Tijs Slaats
 * 
 * DisCoveR: The DCR Graphs process miner.
 * Copyright (C) 2021 Tijs Slaats
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional terms under GNU AGPL version 3 section 7:
 * - By using the program, you provide the copyright holder with permission to mention, discuss, and describe your use of the program in academic publications and accept your responsibility to assist the copyright holder in such publications by providing relevant information.
 *  
 */
package dk.ku.di.dcrgraphs;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DCRXMLParser {

	static public BitDCRGraph loadToBV(String file) {
		return loadToBV(file, null);
	}
	
	static public String analyse(File inputFile) {
		String result = "";
		
		try {			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
	
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
	
			XPath xPath = XPathFactory.newInstance().newXPath();
	
			String expression = "/dcrgraph/specification/resources/events/event/event";
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			if (nodeList.getLength() > 0)
				result +="Graph uses nesting";
			
			
			expression = "/dcrgraph/specification/resources/subProcesses/subProcess";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			if (nodeList.getLength() > 0)
				result +="Graph uses subProcesses";
			
			expression = "/dcrgraph/specification/resources/expressions/expression";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			if (nodeList.getLength() > 0)
				result +="Graph uses expressions";						
			
			
		} catch (ParserConfigurationException e) {
			result += "ParserConfigurationException";
		} catch (SAXException e) {
			result += "SAXException";
		} catch (IOException e) {
			result += "IOException";
		} catch (XPathExpressionException e) {
			result += "XPathExpressionException";
		}
		
		return result;
	}
	

	static public BitDCRGraph loadToBV(String file, BitDCRGraph g) {
		try {
			File inputFile = new File(file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			XPath xPath = XPathFactory.newInstance().newXPath();

			String expression = "/dcrgraph/specification/resources/events/event";
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			if (g == null) {
				g = new BitDCRGraph();
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node nNode = nodeList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						g.addEvent(eElement.getAttribute("id"));
					}
				}
			}

			expression = "/dcrgraph/specification/constraints/conditions/condition";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String src = eElement.getAttribute("sourceId");
					String trg = eElement.getAttribute("targetId");
					g.addCondition(g.eventToId.get(src), g.eventToId.get(trg));
				}
			}

			expression = "/dcrgraph/specification/constraints/responses/response";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String src = eElement.getAttribute("sourceId");
					String trg = eElement.getAttribute("targetId");
					g.addResponse(g.eventToId.get(src), g.eventToId.get(trg));
				}
			}

			expression = "/dcrgraph/specification/constraints/includes/include";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String src = eElement.getAttribute("sourceId");
					String trg = eElement.getAttribute("targetId");
					g.addInclude(g.eventToId.get(src), g.eventToId.get(trg));
				}
			}

			expression = "/dcrgraph/specification/constraints/excludes/exclude";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String src = eElement.getAttribute("sourceId");
					String trg = eElement.getAttribute("targetId");
					g.addExclude(g.eventToId.get(src), g.eventToId.get(trg));
				}
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return g;
	}

	
	static public DCRGraph load(String file) {
			File inputFile = new File(file);
			return load(inputFile);
	}
	
	
	
	static public DCRGraph load(File inputFile) {
		DCRGraph g = new DCRGraph();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();

			XPath xPath = XPathFactory.newInstance().newXPath();

			String expression = "/dcrgraph/specification/resources/events//event";
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					g.addEvent(eElement.getAttribute("id"));
					// Add nesting:
					Element pElement = (Element) eElement.getParentNode();
					if (pElement.getNodeName() == "event")
					{
						g.addNesting(eElement.getAttribute("id"), pElement.getAttribute("id"));
					}
				}
			}

			expression = "/dcrgraph/specification/constraints/conditions/condition";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String src = eElement.getAttribute("sourceId");
					String trg = eElement.getAttribute("targetId");
					g.addCondition(src, trg);
				}
			}

			expression = "/dcrgraph/specification/constraints/responses/response";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String src = eElement.getAttribute("sourceId");
					String trg = eElement.getAttribute("targetId");
					g.addResponse(src, trg);
				}
			}

			expression = "/dcrgraph/specification/constraints/includes/include";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String src = eElement.getAttribute("sourceId");
					String trg = eElement.getAttribute("targetId");
					g.addInclude(src, trg);
				}
			}

			expression = "/dcrgraph/specification/constraints/excludes/exclude";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String src = eElement.getAttribute("sourceId");
					String trg = eElement.getAttribute("targetId");
					g.addExclude(src, trg);
				}
			}
			
			// Labels
			
			expression = "/dcrgraph/specification/resources/labelMappings/labelMapping";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String e = eElement.getAttribute("eventId");
					String l = eElement.getAttribute("labelId");
					g.addLabel(e, l);
				}
			}
			
			
			// Marking
			
			g.marking = g.emptyInitialMarking();
			
			expression = "/dcrgraph/runtime/marking/included/event";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String e = eElement.getAttribute("id");
					
					System.out.println(e);
					if (g.children.get(e).isEmpty())
						g.marking.included.add(e);
				}
			}			

			expression = "/dcrgraph/runtime/marking/pendingResponses/event";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String e = eElement.getAttribute("id");
					
					if (g.children.get(e).isEmpty())
						g.marking.pending.add(e);
				}
			}			

			
			expression = "/dcrgraph/runtime/marking/executed/event";
			nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String e = eElement.getAttribute("id");
					
					if (g.children.get(e).isEmpty())
						g.marking.executed.add(e);
				}
			}			
			
			
			
			
			

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return g;
	}
	

}
