package com.jazz.engine.gui;

import javax.swing.*;
import java.net.URL;

public class Icons {
    private static final String BASE = "assets/icons/";
    private static final String NPS_ICON_PATH = BASE + "npsIcon.png";
    private static final String BF_ICON_PATH = BASE + "bfIcon.png";

    public static ImageIcon depthIcon;
    public static ImageIcon npsIcon;
    public static ImageIcon bfIcon;

    public static void load(){
        //depthIcon = new ImageIcon("assets/icons/depthIcon.png");
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL npsIconResource = classLoader.getResource(NPS_ICON_PATH);
        URL bfIconResource = classLoader.getResource(BF_ICON_PATH);

        npsIcon = new ImageIcon(npsIconResource);
        bfIcon = new ImageIcon(bfIconResource);
    }
}
