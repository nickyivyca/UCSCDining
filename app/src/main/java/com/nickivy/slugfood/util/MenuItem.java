package com.nickivy.slugfood.util;

/**
 * Simple object so that we can store the nutrition info code and string name for each dining menu
 * item in a single arrayList.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 *
 * @author @author Nicky Ivy parkedraccoon@gmail.com
 */
public class MenuItem {

    private String itemName,
    code;

    public MenuItem(String itemName, String code) {
        this.itemName = itemName;
        this.code = code;
    }

    public String getItemName() {
        return itemName;
    }

    public String getCode() {
        return code;
    }

}

