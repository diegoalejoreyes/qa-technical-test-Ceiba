package com.ceiba.qa.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuracion del proyecto.
 *
 * Precedencia: -Dpropiedad=valor (linea de comandos / CI)  >  config.properties.
 * Se mantiene aparte de serenity.conf para que los datos de negocio sean
 * independientes de la configuracion del driver.
 */
public final class Config {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("No se encontro config.properties en el classpath");
            }
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo config.properties", e);
        }
    }

    private Config() {
    }

    public static String get(String key) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        if (value == null) {
            throw new IllegalArgumentException("Propiedad de configuracion no definida: " + key);
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    // ------------------ Accesos tipados ------------------
    public static String sauceDemoUrl() {
        return get("saucedemo.url");
    }

    public static String standardUser() {
        return get("saucedemo.user.standard.username");
    }

    public static String standardPassword() {
        return get("saucedemo.user.standard.password");
    }

    public static String bookerBaseUrl() {
        return get("restfulbooker.url");
    }

    public static String bookerUser() {
        return get("restfulbooker.auth.username");
    }

    public static String bookerPassword() {
        return get("restfulbooker.auth.password");
    }

    /** SLA funcional de tiempo de respuesta para las pruebas de API (ms). */
    public static long apiSlaMillis() {
        return getInt("restfulbooker.sla.responseTimeMs");
    }

    public static int defaultWaitSeconds() {
        return getInt("waits.default.seconds");
    }
}
