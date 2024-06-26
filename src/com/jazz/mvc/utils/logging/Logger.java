package com.jazz.mvc.utils.logging;

public class Logger {
    public static void log(String prefix, String message){
        System.out.println(STR."[\{prefix.toUpperCase()}]>\{message}");
    }

    /**
     * General purpose logging.
     */
    public static void log(String message){
        log("LOG", message);
    }

    /**
     * Status reports.
     */
    public static void status(String component, String status){
        log("STATUS", STR."\{component}-->\{status}");
    }
    /**
     * Debug messages for development.
     */
    public static void debug(String message){
        log("DEBUG",message);
    }
}
