package org.breskul.exception;

import java.sql.SQLSyntaxErrorException;

public class TableNameNotCorrect extends RuntimeException {
    public TableNameNotCorrect(String message) {
        super(message);
    }
}
