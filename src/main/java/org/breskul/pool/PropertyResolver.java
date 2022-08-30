package org.breskul.pool;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.breskul.exception.BoboException;
import org.breskul.exception.ExceptionMessage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import static org.breskul.util.Constants.DEFAULT_PROPERTIES_FILE_NAME;

/**
 * Used to read specified property file. By default, it looks into application.properties file.
 */
@Slf4j
public class PropertyResolver {

    @Getter
    private Properties properties = new Properties();

    public PropertyResolver(String fileName) {
        scanFile(fileName);
    }

    public PropertyResolver() {
        scanFile(DEFAULT_PROPERTIES_FILE_NAME);
    }

    private void scanFile(String fileName) {
        log.trace("Loading properties from file: {}", fileName);
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = Optional.ofNullable(loader.getResourceAsStream(fileName)).orElseThrow(FileNotFoundException::new);
            properties.load(stream);
        } catch (FileNotFoundException e) {
            log.error("Exception occurred when initializing {} file", fileName, e);
            throw new BoboException(ExceptionMessage.PROPERTY_FILE_NOT_FOUND_EXCEPTION.formatted(fileName), e);
        } catch (IOException e) {
            log.error("Exception occurred when reading {} file", fileName, e);
            throw new BoboException(ExceptionMessage.READ_PROPERTY_FILE_EXCEPTION, e);
        }
    }
}
