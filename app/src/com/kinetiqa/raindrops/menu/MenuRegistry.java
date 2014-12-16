package com.kinetiqa.raindrops.menu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Stack;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import android.content.Context;
import android.os.Environment;

public class MenuRegistry {

	public final static int MEDIA_OTHER = -1;
	public final static int MEDIA_MENU = 0;
	public final static int MEDIA_VIDEO = 1;
	public final static int MEDIA_RICH_TEXT = 2;

	private MenuComponent rootNode;

	/**
	 * Static default instance.
	 */
	private static MenuRegistry defaultInstance;

	// Saves the current menu stack
	public static Stack<MenuComponent> currNode = new Stack<MenuComponent>();

	public static MenuRegistry getDefault(Context c) {
		if (defaultInstance == null) {
			defaultInstance = createDefaultInstance(c);
			return defaultInstance;
		} else {
			return defaultInstance;
		}
	}

	private static MenuRegistry createDefaultInstance(Context c) {
		MenuRegistry registry = new MenuRegistry();
		System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(new MenuStructureParser(c, registry));
			File xmlMenu = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/raindrops/content/menu.xml");
			InputStream inputStream = new FileInputStream(xmlMenu);

			Reader reader_input = new InputStreamReader(inputStream, "UTF-8");
			InputSource is = new InputSource(reader_input);
			reader.parse(is);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return registry;
	}

	public static MenuComponent findNode(String itemIDtoFind, MenuComponent node) {
		if (itemIDtoFind.equals(node.getID())) {
			return node;
		}
		
		if (node.getMediaType() == MenuRegistry.MEDIA_MENU) {
			for (MenuComponent m : ((MenuComposite) node).getMenuItems()) {
				MenuComponent innerNode = findNode(itemIDtoFind, m);
				if (innerNode != null) {
					return innerNode;
				}
			}
			return null;
		} else {
			// This node was a MenuLeaf; we cannot go any further so return null
			return null;
		}
	}

	public MenuComponent getRootNode() {
		return rootNode;
	}

	public void setRootNode(MenuComponent rootNode) {
		this.rootNode = rootNode;
	}

}
