package com.kinetiqa.raindrops.menu;

import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.kinetiqa.raindrops.database.DatabaseHelper;

/**
 * Use right after downloading the menu XML to establish database and media
 * files to download
 * 
 * @author Tom
 * 
 */
public class MenuStructureSetupParser extends DefaultHandler {

	private Context c;
	private SharedPreferences sharedPreferences;

	private MenuLeaf leaf;
	private StringBuilder textAccumulator;
	private int versionNumber = 1;

	public MenuStructureSetupParser(Context c) {
		this.c = c;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
		textAccumulator = new StringBuilder();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		if (localName.toLowerCase(Locale.CANADA).equals("menu")) {
			MenuComposite menuItem = new MenuComposite(this.versionNumber, attributes.getValue("id"), attributes.getValue("name"), attributes.getValue("requires"), attributes.getValue("desc"));
			DatabaseHelper.getInstance(c).addItem(menuItem);
		}
		
		if (localName.toLowerCase(Locale.CANADA).equals("item")) {
			String attr_is_activity = attributes.getValue("activity");
			String attr_media_type = attributes.getValue("type");

			boolean is_activity = false;
			int media_type = -1;

			try {
				is_activity = Boolean.valueOf(attr_is_activity);
				media_type = Integer.valueOf(attr_media_type);
				leaf = new MenuLeaf(this.versionNumber, is_activity, media_type,
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

		if (localName.toLowerCase(Locale.CANADA).equals("version")) {
			try {
				int versionNumber = Integer.valueOf(textAccumulator.toString());
				this.versionNumber = versionNumber;
				sharedPreferences.edit().putLong("version", versionNumber).commit();
			} catch (Exception e) {
				System.out.println("Version Corrupted: " + e.getMessage());
			}
		}

		if (localName.toLowerCase(Locale.CANADA).equals("item") && leaf != null) {
			leaf.setName(textAccumulator.toString());
			DatabaseHelper.getInstance(c).addItem(leaf);
		}

	}

	/**
	 * Called when the end of the document is reached
	 */
	public void endDocument() {
		// End of document
	}

}
