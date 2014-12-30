package com.kinetiqa.glacier.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.kinetiqa.glacier.core.Config;
import com.kinetiqa.glacier.database.DatabaseHelper;

/**
 * Use right after downloading the menu XML to establish database and media
 * files to download
 * 
 * @author Tom
 * 
 */
public class MenuXMLSetupParser extends DefaultHandler {

	private Context c;
	private SharedPreferences sharedPreferences;

	private MenuItem menuItem;
	private StringBuilder textAccumulator;
	private int versionNumber = 1;

	public MenuXMLSetupParser(Context c) {
		this.c = c;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
		textAccumulator = new StringBuilder();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		if (localName.toLowerCase(Config.locale).equals("menu")) {
            String requires = attributes.getValue("requires");
            String[] requiresArr = requires.split(",");
            List<String> requiresList = new ArrayList<String>(Arrays.asList(requiresArr));
            MenuFolder menuFolder = new MenuFolder(attributes.getValue("id"), attributes.getValue("name"), attributes.getValue("desc"), requiresList);
            DatabaseHelper.getInstance(c).addMenuFolder(menuFolder);
		}
		
		if (localName.toLowerCase(Config.locale).equals("item")) {
            String requires = attributes.getValue("requires");
            String[] requiresArr = requires.split(",");
            List<String> requiresList = new ArrayList<String>(Arrays.asList(requiresArr));

            String isActivityStr = attributes.getValue("activity");
            String mediaTypeStr = attributes.getValue("type");

            boolean isActivity = false;
            int mediaType = MenuManager.MEDIA_UNDEFINED;

            try {
                isActivity = Boolean.valueOf(isActivityStr);
                mediaType = Integer.valueOf(mediaTypeStr);
                menuItem = new MenuItem(attributes.getValue("id"), null, attributes.getValue("desc"),
                        requiresList, isActivity, mediaType, attributes.getValue("path"));
            } catch (Exception e) {
                // Corrupt Item; Ignore Item
                menuItem = null;
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

		if (localName.toLowerCase(Config.locale).equals("version")) {
			try {
				int versionNumber = Integer.valueOf(textAccumulator.toString());
				this.versionNumber = versionNumber;
				sharedPreferences.edit().putLong("version", versionNumber).commit();
			} catch (Exception e) {
				System.out.println("Version Corrupted: " + e.getMessage());
			}
		}

		if (localName.toLowerCase(Config.locale).equals("item") && menuItem != null) {
            menuItem.setTitle(textAccumulator.toString());
			DatabaseHelper.getInstance(c).addMenuItem(menuItem);
		}

	}

	/**
	 * Called when the end of the document is reached
	 */
	public void endDocument() {
		// End of document
	}

}
