package com.kinetiqa.raindrops.menu;

import java.util.ArrayList;
import java.util.List;

public class MenuComposite extends MenuComponent {

	private int versionNumber = 1;
	private boolean isActivity = false;
	private int mediaType = MenuRegistry.MEDIA_MENU;
	private String id;
	private String name;
	private String requires;
	private String text;

	private List<MenuComponent> menuItems;

	public MenuComposite(int versionNumber, String id, String name,
			String requires, String text) {
		this.versionNumber = versionNumber;
		this.id = id;
		this.name = name;
		this.requires = requires;
		this.text = text;
		this.menuItems = new ArrayList<MenuComponent>();
	}

	public void addSubItem(MenuComponent m) {
		menuItems.add(m);
	}

	public List<MenuComponent> getMenuItems() {
		return this.menuItems;
	}

	public List<MenuComponent> getMenuSubMenus() {
		List<MenuComponent> listOfMenuSubMenus = new ArrayList<MenuComponent>();
		for (MenuComponent m : this.menuItems) {
			if (m.getMediaType() == MenuRegistry.MEDIA_MENU) {
				listOfMenuSubMenus.add(m);
			}
		}
		return this.menuItems;
	}

	@Override
	public String getDesc() {
		return this.text;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public void printNames() {
		System.out.println(name);
		for (MenuComponent m : menuItems) {
			m.printNames();
		}
	}

	@Override
	public String getRequires() {
		return this.requires;
	}

	@Override
	public String getID() {
		return this.id;
	}

	@Override
	public int getMediaType() {
		return this.mediaType;
	}

	@Override
	public boolean isActivity() {
		return this.isActivity;
	}

	@Override
	public int getVersionNumber() {
		return this.versionNumber;
	}

	@Override
	public String getPath() {
		return null;
	}
}
