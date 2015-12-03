package com.joechang.loco;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author:    joechang
 * Created:   7/15/15 4:07 PM
 * Purpose:
 */
public enum Configuration {

    PRODUCTION_PROTOCOL ("productionProtocol", "http"),
    PRODUCTION_SERVER   ("productionServer", "joelo.co"),
    PRODUCTION_PORT     ("productionPort", "8080"),
    DEV_PROTOCOL        ("devProtocol", "http"),
    DEV_SERVER          ("devServer", "localhost"),
    DEV_PORT            ("devPort", "8080"),
    OPENTABLE_PROTOCOL  ("openTableProtocol", "http"),
    OPENTABLE_SERVER    ("opentableServer", "opentable.herokuapp.com"),
    OPENTABLE_PORT      ("opentablePort", "80"),
    GIPHY_PROTOCOL      ("giphyProtocol", "http"),
    GIPHY_SERVER        ("giphyServer", "api.giphy.com"),
    GIPHY_PORT          ("giphyPort", "80"),
    REST_TIMEOUT        ("restClientTimeoutMillis", "10000"),

    SERVER_PHONE_NUMBER ("serverPhoneNumber", "4153122379")
    ;

    private final String key;
    private String defaultValue;

    Configuration(String key) {
        this(key, NA);
    }

    Configuration(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    private final static Logger logger = Logger.getLogger(Configuration.class.getName());
    private final static String NA = "n.a.";
    private final static String CONFIG_FILE = "configuration.properties";
    private final static String NOT_A_VALID_KEY = "Not a valid property key";
    private final static Map<Configuration, String> configuration = new EnumMap<>(Configuration.class);

    static {
        readConfigurationFrom(CONFIG_FILE);
    }

    public static String getProdServerAddress() {
        return PRODUCTION_PROTOCOL.get() + "://" + PRODUCTION_SERVER.get() + ":" + PRODUCTION_PORT.get();
    }

    public static String getDevServerAddress() {
        return DEV_PROTOCOL.get() + "://" + DEV_SERVER.get() + ":" + DEV_PORT.get();
    }

    public static String getOpenTableApiEndpoint() {
        return OPENTABLE_PROTOCOL.get() + "://" + OPENTABLE_SERVER.get() + ":" + OPENTABLE_PORT.get();
    }

    public static String getGiphyApiEndpoint() {
        return GIPHY_PROTOCOL.get() + "://" + GIPHY_SERVER.get() + ":" + GIPHY_PORT.get();
    }

    public static String getServerPhoneNumber() {
        return SERVER_PHONE_NUMBER.get();
    }

    private static void readConfigurationFrom(String fileName) {
        logger.info("Reading resource: {}" + fileName);
        try (InputStream resource = Configuration.class.getClassLoader().getResourceAsStream(fileName);) {
            Properties properties = new Properties();
            properties.load(resource); //throws a NPE if resource not founds
            for (String key : properties.stringPropertyNames()) {
                configuration.put(getConfigurationKey(key), properties.getProperty(key));
            }
        } catch (IllegalArgumentException | IOException | NullPointerException e) {
            logger.log(Level.SEVERE, "Error while reading the properties file {}" + fileName, e);
            populateDefaultValues();
        }
    }

    private static Configuration getConfigurationKey(String key) {
        for (Configuration c : values()) {
            if (c.key.equals(key)) {
                return c;
            }
        }
        throw new IllegalArgumentException(NOT_A_VALID_KEY + ": " + key);
    }

    private static void populateDefaultValues() {
        for (Configuration c : values()) {
            configuration.put(c, c.defaultValue);
        }
    }

    /**
     * @return the property corresponding to the key or null if not found
     */
    public String get() {
        return configuration.get(this);
    }
}