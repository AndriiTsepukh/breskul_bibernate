package org.breskul.util;

public class StringUtils {
    private final static String PARAM_FOR_REPLACE = "$1_$2";
    private final static String FIRST_RGX = "([A-Z]+)([A-Z][a-z])";
    private final static String SECOND_RGX = "([a-z])([A-Z])";
    public static String camelToSnake(String camelString) {


        return camelString.replaceAll(FIRST_RGX, PARAM_FOR_REPLACE).replaceAll(SECOND_RGX, PARAM_FOR_REPLACE).toLowerCase();
    }
}
