package com.gio;

import com.gio.common.Config;
import com.gio.common.ProgressPoint;
import com.gio.common.Utils;
import com.gio.model.ApiResponse;
import com.gio.network.BackgroundWork;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Timer;

public class Main {

    private static final String CONSOLE_TITLE = "Program of viewers current currency";
    private static final String ENTER_DATE_FROM_CURRENCY = "Enter from currency:";
    private static final String ERROR_ENTER_DATE = "Error of enter date! Return step please";
    private static final String ENTER_DATE_TO_CURRENCY = "Enter to currency:";
    private static final String CONTINUE_KEY = "Continue (Y/N)";

    public static void main(String[] args) {
        String keyExit = null;

        System.out.println(String.format("\n%s\n\n", CONSOLE_TITLE));
        do {
            System.out.println(ENTER_DATE_FROM_CURRENCY);

            final String fromCurrency = enterCurrency(ENTER_DATE_FROM_CURRENCY);

            System.out.println(ENTER_DATE_TO_CURRENCY);

            final String toCurrency = enterCurrency(ENTER_DATE_TO_CURRENCY);

            //Simulate of work ProgressBar
            final Timer timer = new Timer();
            timer.schedule(new ProgressPoint(), 0, 50);

            BackgroundWork backgroundWork = new BackgroundWork();

            final String lastTimeString = backgroundWork.getLastTimeFromFile();
            long lastUpdateTime = 0;
            if (lastTimeString != null) {
                lastUpdateTime = Long.parseLong(lastTimeString);
            }

            final long currentTime = Calendar.getInstance().getTimeInMillis();

            ApiResponse apiResponse;

            if ((currentTime - lastUpdateTime) > Config.TIME_PERIOD_UPDATE) {
                apiResponse = backgroundWork.loadRatesAndCurrency(fromCurrency, toCurrency);
                timer.cancel();
                System.out.println(String.format("\n%s => %s : %s", fromCurrency, toCurrency,
                        apiResponse.getRates().getRate()));
                //convert to jsonSting data and write to file
                Gson gson = new Gson();
                final String jsonStingApiResponse = gson.toJson(apiResponse, ApiResponse.class);
                backgroundWork.setDataToFile(jsonStingApiResponse, fromCurrency, toCurrency);
                //change last time in the file
                backgroundWork.setLastTimeToFile(String.valueOf(currentTime));
            } else {
                apiResponse = backgroundWork.getDataFromFile(fromCurrency, toCurrency);
                timer.cancel();
                System.out.println(String.format("\n%s => %s : %s", fromCurrency, toCurrency,
                        apiResponse.getRates().getRate()));

            }
            System.out.println(String.format("\n%s", CONTINUE_KEY));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            try {
                keyExit = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (keyExit != null && keyExit.toUpperCase().equals("Y"));

        System.exit(0);
    }

    /**
     * Method for entering currency
     * @param currencyType type what will be enter
     * @return entered params
     */
    private static String enterCurrency(String currencyType) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String currency = null;

        do {
            try {
                currency = reader.readLine().toUpperCase();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!Utils.isCorrectCurrencyEnum(currency)) {
                System.out.println(String.format("%s\n%s", ERROR_ENTER_DATE, currencyType));
            }
        } while (!Utils.isCorrectCurrencyEnum(currency));

        return currency;
    }
}