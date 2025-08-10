package com.agencevoyage.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;

public final class UIUtil {
    private UIUtil() {}
    public static void scaleUI(double factor) {
        // Global font scale
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object val = UIManager.get(key);
            if (val instanceof Font f) {
                UIManager.put(key, f.deriveFont((float)(f.getSize2D() * factor)));
            }
        }
        // Bigger default row height / controls feel
        UIManager.put("Table.rowHeight", (int)(UIManager.getInt("Table.rowHeight") * factor));
        UIManager.put("Button.margin", new Insets(8, 16, 8, 16));
        UIManager.put("TextField.margin", new Insets(6, 10, 6, 10));
    }
}
