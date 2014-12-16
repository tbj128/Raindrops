package com.kinetiqa.raindrops.menu;

import java.util.Locale;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

public class MenuStructureParser extends DefaultHandler {

	private int versionNumber = 1;

	private Stack<MenuComposite> menuStack = new Stack<MenuComposite>();
	private MenuLeaf leaf;
	private MenuRegistry reg;
	private StringBuilder textAccumulator;

	public MenuStructureParser(Context c, MenuRegistry reg) {
		this.reg = reg;
		textAccumulator = new StringBuilder();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		if (localName.equals("menu")) {
			MenuComposite menuItem = new MenuComposite(this.versionNumber, attributes.getValue("id"), attributes.getValue("name"), attributes.getValue("requires"), attributes.getValue("desc"));
		
			if (!menuStack.empty()) {
				// There is a parent node; Add as a child to the parent
				MenuComposite parentItem = menuStack.peek();
				parentItem.addSubItem(menuItem);
			} else {
				// This is the root menu node
				reg.setRootNode(menuItem);
			}

			menuStack.push(menuItem);
		}

		if (localName.equals("item")) {
			String attr_is_activity = attributes.getValue("activity");
			String attr_media_type = attributes.getValue("type");

			boolean is_activity = false;
			int media_type = -1;

			try {
				is_activity = Boolean.valueOf(attr_is_activity);
				media_type = Integer.valueOf(attr_media_type);
				leaf = new MenuLeaf(versionNumber, is_activity, media_type,
						attributes.getValue("id"), attributes.getValue("desc"),
						attributes.getValue("requires"),
						attributes.getValue("path"));
			} catch (Exception e) {
				// Corrupt Item; Ignore Item
				leaf = null;
				System.out.println(e.toString());
			}
		}
		textAccumulator.setLength(0);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		textAccumulator.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);

		if (localName.toLowerCase(Locale.CANADA).equals("item") && leaf != null) {
			leaf.setName(textAccumulator.toString());
			if (!menuStack.empty()) {
				// There is a parent node; Add as a child to the parent
				MenuComposite parentItem = menuStack.peek();
				parentItem.addSubItem(leaf);
			} else {
				System.out.println("No parent? D:");
			}
		}

		if (localName.equals("menu")) {
			menuStack.pop();
		}
	}

	/**
	 * Called when the end of the document is reached
	 */
	public void endDocument() {

	}

}
