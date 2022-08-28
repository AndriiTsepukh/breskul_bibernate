package org.breskul.connectivity.datasource;

import lombok.extern.slf4j.Slf4j;
import org.breskul.exception.BoboException;
import org.breskul.exception.ExceptionMessage;

import java.io.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.breskul.util.Constants.*;

@Slf4j
public class DataSourcePropertyResolver {
    private Map<String, String> valueToPropertyName;

    public DataSourcePropertyResolver() {
        scanFile(DEFAULT_PROPERTIES_FILE_NAME);
        initializeDataSource();
    }

    public DataSourcePropertyResolver(String fileName) {
        scanFile(fileName);
        initializeDataSource();
    }

    private void initializeDataSource() {
        if (Objects.nonNull(valueToPropertyName) && !valueToPropertyName.isEmpty()) {
            String url = valueToPropertyName.get(URL);
            String username = valueToPropertyName.get(USERNAME);
            String password = valueToPropertyName.get(PASSWORD);

            if (isEmpty(url) || isEmpty(username) || isEmpty(password)) {
                log.warn("Could not find one of the properties in application.properties file: {}, {}, {}. DataSource has not been initialized.", URL, USERNAME, PASSWORD);
                return;
            }
            FileConfigurableDataSource.getInstance(url, username, password);
        }
    }

    private void scanFile(String fileName) {
        Optional<File> file = getFile(fileName);
        if (file.isPresent()) {
            try (var fileReader = new BufferedReader(new FileReader(file.get()))) {
                valueToPropertyName = fileReader.lines()
                        .filter(Predicate.not(this::isEmpty))
                        .map(String::trim)
                        .filter(Predicate.not(this::isCommented))
                        .map(line -> line.split(KEY_VALUE_DELIMITER))
                        .filter(arr -> arr.length > 1)
                        .collect(Collectors.toMap(key -> key[0], value -> value[1]));
            } catch (FileNotFoundException e) {
                throw new BoboException(ExceptionMessage.PROPERTY_FILE_NOT_FOUND_EXCEPTION);
            } catch (IOException e) {
                throw new BoboException(ExceptionMessage.READ_PROPERTY_FILE_EXCEPTION);
            }
        } else {
            throw new BoboException(ExceptionMessage.PROPERTY_FILE_NOT_FOUND_EXCEPTION.replace("{}", fileName));
        }
    }

    private Optional<File> getFile(String fileName) {
        if (isEmpty(fileName)) {
            return Optional.empty();
        }

        return Optional.ofNullable(ClassLoader.getSystemClassLoader().getResource(fileName))
                .map(java.net.URL::getPath)
                .map(File::new);
    }

    private boolean isCommented(String s) {
        return s.startsWith("#");
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
