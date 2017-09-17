package com.gio.common;

public class Config {
    public static final String apiURL = "http://api.fixer.io/latest?base=%1$s&symbols=%2$s";

    public static final String cachedFileTimeName = "last-time.txt";

    public static final int TIME_PERIOD_UPDATE = 21600000; //milli second
}