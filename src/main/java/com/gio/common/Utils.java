package com.gio.common;

import com.gio.common.enums.CurrencyEnum;
import com.gio.model.RateObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sun.rmi.runtime.Log;
import sun.util.logging.PlatformLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    private final static Logger logger = Logger.getLogger(Utils.class.getName());

    /**
     * Initializing Gson
     */
    public static Gson initializeGson() {
        return new GsonBuilder()
                .registerTypeAdapter(RateObject.class, new RatesDeserializer())
                .create();
    }

    /**
     * @param fromCurrency currency from convert
     * @param toCurrency   currency to convert
     * @return file name
     */
    public static String getCachedFileName(String fromCurrency, String toCurrency) {
        return String.format("%s-%s.txt", fromCurrency, toCurrency);
    }

    /**
     * used for check correct enter value
     * @param currency compere with enumeration type
     * @return is correct
     */
    public static boolean isCorrectCurrencyEnum(String currency) {
        try {
            CurrencyEnum.valueOf(currency);
        } catch (IllegalArgumentException e) {
            //logger.log(Level.WARNING, String.valueOf(e));
            return false;
        }

        return true;
    }
}