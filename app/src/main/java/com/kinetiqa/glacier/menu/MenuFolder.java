package com.kinetiqa.glacier.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 2014-11-01.
 */
public class MenuFolder extends Menu {

    List<Menu> subMenus = new ArrayList<Menu>();

    public MenuFolder(String id, String title, String desc, List<String> requires) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.requires = requires;
    }

    public void addSubMenu(Menu menu) {
        this.subMenus.add(menu);
    }

    public List<Menu> getSubMenus() {
        return this.subMenus;
    }

    /**
     * Gets all the subfolders of this menu folder
     * @return
     */
    public List<MenuFolder> getSubFolders() {
        List<MenuFolder> subFolders = new ArrayList<MenuFolder>();
        for (Menu m: subMenus) {
            if (m instanceof MenuFolder) {
                subFolders.add((MenuFolder) m);
            }
        }
        return subFolders;
    }
}
