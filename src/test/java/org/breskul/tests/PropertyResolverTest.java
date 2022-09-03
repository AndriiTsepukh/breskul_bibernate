package org.breskul.tests;

import org.breskul.exception.BoboException;
import org.breskul.pool.PooledDataSource;
import org.breskul.pool.PropertyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.breskul.testdata.entity.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

public class PropertyResolverTest {

    @BeforeEach
    public void setup() {
        PooledDataSource.reset();
    }

    @Test
    @DisplayName("Read default file when name is not specified")
    void readPropertiesFromDefaultFile() {

        PropertyResolver propertyResolver = new PropertyResolver();
        Properties properties = propertyResolver.getProperties();

        assertNotNull(properties);
        assertNotNull(properties.getProperty(URL));
        assertNotNull(properties.getProperty(USERNAME));
        assertNotNull(properties.getProperty(PASSWORD));
        assertNotNull(properties.getProperty(POOL_SIZE));
    }


    @Test
    @DisplayName("Read from specified file")
    void readPropertiesFromSpecifiedFile() {
        PropertyResolver propertyResolver = new PropertyResolver("application-test.properties");
        Properties properties = propertyResolver.getProperties();

        assertNotNull(properties);
        assertEquals(properties.get(USERNAME), "sa");
    }


    @Test
    @DisplayName("Throws exception when file doesn't exists")
    void shouldThrowExceptionWhenFileDoesNotExists() {
        assertThrows(BoboException.class, () -> new PropertyResolver("application-throw-exception.properties"));
    }
}
