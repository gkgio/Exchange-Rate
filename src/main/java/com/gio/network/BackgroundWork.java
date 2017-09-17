package com.gio.network;

import com.gio.common.Config;
import com.gio.common.Utils;
import com.gio.model.ApiResponse;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;

/**
 * all background work
 */
public class BackgroundWork {

    private static final String CONNECT_ERROR = "Ошибка подключения к серверу. " +
            "Проверьте настройки сети или повторите попытку позже.";
    private static final String SECURITY_ERROR = "Ошибка доступа к ресурсу. Повторите попытку позже!";
    private static final String ERROR_CREATE_DIRECTORY = "Error create directory";

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ApiResponse loadRatesAndCurrency(String fromCurrency, String toCurrency) {
        Future<ApiResponse> apiResponseFuture = executorService.submit(new LoadDateCallAble(fromCurrency, toCurrency));
        try {
            return apiResponseFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setLastTimeToFile(String lastTime) {
        executorService.submit(new SetLastTimeToFileRunAble(lastTime));
    }

    public String getLastTimeFromFile() {
        Future<String> lastTimeFuture = executorService.submit(new GetLastTimeFromFileCallAble());
        try {
            return lastTimeFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setDataToFile(String data, String fromCurrency, String toCurrency) {
        executorService.submit(new SetDataToFileRunAble(data, fromCurrency, toCurrency));
    }

    public ApiResponse getDataFromFile(String fromCurrency, String toCurrency) {
        Future<ApiResponse> apiResponseFuture =
                executorService.submit(new GetDataFromFileCallAble(fromCurrency, toCurrency));
        try {
            return apiResponseFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class LoadDateCallAble implements Callable<ApiResponse> {

        private String fromCurrency;
        private String toCurrency;

        public LoadDateCallAble(String fromCurrency, String toCurrency) {
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
        }

        @Override
        public ApiResponse call() throws Exception {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(String.format(Config.apiURL, fromCurrency, toCurrency));
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.connect();

                int responseCode = connection.getResponseCode();

                switch (responseCode) {
                    case HttpURLConnection.HTTP_OK:
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        ApiResponse apiResponse = Utils.initializeGson().fromJson(br, ApiResponse.class);
                        br.close();
                        return apiResponse;
                    case HttpURLConnection.HTTP_FORBIDDEN:
                        System.err.println(SECURITY_ERROR);
                        break;
                    default:
                        System.err.println(CONNECT_ERROR);
                        break;

                }
            } catch (IOException e) {
                System.err.println(CONNECT_ERROR);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return null;
        }
    }

    private class SetDataToFileRunAble implements Runnable {
        private String data;
        private String fromCurrency;
        private String toCurrency;

        public SetDataToFileRunAble(String data, String fromCurrency, String toCurrency) {
            this.data = data;
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
        }

        @Override
        public void run() {
            PrintWriter printWriter = null;
            final String cachedFileName = Utils.getCachedFileName(fromCurrency, toCurrency);

            try {
                File file = new File(cachedFileName);
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new IllegalStateException(ERROR_CREATE_DIRECTORY);
                }
                printWriter = new PrintWriter(cachedFileName);
                printWriter.println(data);
            } catch (FileNotFoundException | IllegalStateException e) {
                e.printStackTrace();
            } finally {
                if (printWriter != null) {
                    printWriter.close();
                }
            }
        }
    }

    private class GetDataFromFileCallAble implements Callable<ApiResponse> {
        private String fromCurrency;
        private String toCurrency;

        public GetDataFromFileCallAble(String fromCurrency, String toCurrency) {
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
        }

        @Override
        public ApiResponse call() throws Exception {
            final String cachedFileName = Utils.getCachedFileName(fromCurrency, toCurrency);

            try {
                File f = new File(cachedFileName);
                if (f.exists() && !f.isDirectory()) {
                    BufferedReader br = new BufferedReader(new FileReader(cachedFileName));

                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        line = br.readLine();
                    }

                    br.close();
                    //convert jsonString to ApiResponse class
                    final Gson gson = new Gson();
                    return gson.fromJson(sb.toString(), ApiResponse.class);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class SetLastTimeToFileRunAble implements Runnable {
        private String lastTime;

        public SetLastTimeToFileRunAble(String lastTime) {
            this.lastTime = lastTime;
        }

        @Override
        public void run() {
            PrintWriter printWriter = null;

            try {
                File file = new File(Config.cachedFileTimeName);
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new IllegalStateException(ERROR_CREATE_DIRECTORY);
                }
                printWriter = new PrintWriter(Config.cachedFileTimeName);
                printWriter.println(lastTime);
            } catch (FileNotFoundException | IllegalStateException e) {
                e.printStackTrace();
            } finally {
                if (printWriter != null) {
                    printWriter.close();
                }
            }
        }
    }

    private class GetLastTimeFromFileCallAble implements Callable<String> {
        @Override
        public String call() throws Exception {

            try {
                File f = new File(Config.cachedFileTimeName);
                if (f.exists() && !f.isDirectory()) {

                    BufferedReader br = new BufferedReader(new FileReader(Config.cachedFileTimeName));

                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        line = br.readLine();
                    }

                    br.close();
                    return sb.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}