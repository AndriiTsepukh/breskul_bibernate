package org.breskul.exception;

public enum ExceptionMessage {
    ;
    public static final String READ_PROPERTY_FILE_EXCEPTION = "Exception occurred during reading properties from the application.properties file";
    public static final String PROPERTY_FILE_NOT_FOUND_EXCEPTION = "Couldn't find properties file in resources with name: %s";
    public static final String GET_CONNECTION_EXCEPTION = "Exception occurred during getting connection";
    public static final String TABLE_NAME_ERROR = "Something went wrong. Check table name";
    public static final String COLUMN_ERROR = "Dude check the column names match";


}
