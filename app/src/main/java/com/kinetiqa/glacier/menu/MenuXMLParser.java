package com.kinetiqa.glacier.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

import com.kinetiqa.glacier.core.Config;

public class MenuXMLParser extends DefaultHandler {

    private Stack<MenuFolder> menuFoldersStack = new Stack<MenuFolder>();
    private MenuItem menuItem;
    private MenuManager menuManager;
    private StringBuilder textAccumulator;

    public MenuXMLParser(MenuManager menuManager) {
        this.menuManager = menuManager;
        textAccumulator = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (localName.toLowerCase(Config.locale).equals("menu")) {
            String requires = attributes.getValue("requires");
            String[] requiresArr = requires.split(",");
            List<String> requiresList = null;
            if (!"".equals(requires)) {
                requiresList = new ArrayList<String>(Arrays.asList(requiresArr));
            }
            MenuFolder menuFolder = new MenuFolder(attributes.getValue("id"), attributes.getValue("name"), attributes.getValue("desc"), requiresList);

            if (!menuFoldersStack.empty()) {
                // There is a parent node; Add as a child to the parent
                MenuFolder parentFolder = menuFoldersStack.peek();
                parentFolder.addSubMenu(menuFolder);
            } else {
                // This is the root menu node
                menuManager.setRootNode(menuFolder);
            }

            menuFoldersStack.push(menuFolder);
        } else if (localName.toLowerCase(Config.locale).equals("item")) {
            String requires = attributes.getValue("requires");
            String[] requiresArr = requires.split(",");
            List<String> requiresList = null;
            if (!"".equals(requires)) {
                requiresList = new ArrayList<String>(Arrays.asList(requiresArr));
            }

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

        if (localName.toLowerCase(Config.locale).equals("item") && menuItem != null) {
            menuItem.setTitle(textAccumulator.toString());
            if (!menuFoldersStack.empty()) {
                // There is a parent node; Add as a child to the parent
                MenuFolder parentFolder = menuFoldersStack.peek();
                parentFolder.addSubMenu(menuItem);
            } else {
                System.out.println("Error: No parent element");
            }
        }

        if (localName.toLowerCase(Config.locale).equals("menu")) {
            menuFoldersStack.pop();
        }
    }

    /**
     * Called when the end of the document is reached
     */
    public void endDocument() {
    }

}
