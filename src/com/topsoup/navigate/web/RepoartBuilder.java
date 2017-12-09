package com.topsoup.navigate.web;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RepoartBuilder {
	public void abc() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Element eleRoot = doc.createElement("root");
			eleRoot.setAttribute("author", "homer");
			eleRoot.setAttribute("date", "2012-04-26");
			doc.appendChild(eleRoot);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
}
