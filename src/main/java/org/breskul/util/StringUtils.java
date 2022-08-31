package org.breskul.util;

public class StringUtils {
    public static String camelToSnake(String camelString) {
        return camelString.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
