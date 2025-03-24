/**
 * @author Ebing Jan S31236
 */

package zad1;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class Service {
    private Locale _country;
    private Currency _currency;
    private WeatherJson _weatherJson;
    private final Map<String, String[]> _cachedCities;
    private static Service instance;

    public static Service getInstance(String countryName) {
        if(instance == null) {
            instance = new Service(countryName);
        }
        instance.setCountry(countryName);
        return instance;
    }

    public Service(String countryName) {
        instance = this;
        setCountry(countryName);
        _cachedCities = new HashMap<>();
    }

    private void setCountry(String countryName) {
        _country = getLocaleFromCountryName(countryName);
        _currency = Currency.getInstance(_country);
    }

    public Locale getLocaleFromCountryName(String countryName) {
        for (String countryCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", countryCode);
            if (locale.getDisplayCountry().equalsIgnoreCase(countryName)) {
                return locale;
            }
        }
        return null;
    }


    public String getWeather(String city) {
        try {
            String json = getJsonFromUrl("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + "86d0991dc07f2a61795f0446af327832");
            _weatherJson = new Gson().fromJson(json, WeatherJson.class);
            return json;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double getTemperature() {

        double temperature = _weatherJson.getTemperature();
        return (double) Math.round(10 * (temperature - 273.15)) / 10;
    }

    public double getRateFor(String currencyCode) {

        String collected;
        try {
            collected = getJsonFromUrl("https://open.er-api.com/v6/latest/" + _currency.getCurrencyCode());
            Rates rates = new Gson().fromJson(collected, Rates.class);
            return rates.getRateFor(currencyCode);
        } catch (Exception e) {
            throw new RuntimeException("no data for this currency:(");
        }
    }


    public double getNBPRate() {
        String collected = null;
        for (char xd : "ABC".toCharArray()) {
            try {
                collected = getJsonFromUrl("https://api.nbp.pl/api/exchangerates/rates/" + xd + "/" + _currency.getCurrencyCode());
                break;
            } catch (Exception e) {
                if (_country.getCountry().equals("PL"))
                    return 1;
                throw new RuntimeException("no data for this currency :(");
            }
        }

        NBPRate NBPrates = new Gson().fromJson(collected, NBPRate.class);
        return NBPrates.getMid();


    }

    private String getJsonFromUrl(String url) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));

        return br.lines().collect(Collectors.joining(System.lineSeparator()));

    }

    public ImageIcon getWeatherIcon() {
        String icon = _weatherJson.getIcon();
        String imageUrl = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
        String savePath = "resources/" + icon + ".png";

        try {
            downloadImage(imageUrl, savePath);
            BufferedImage image = ImageIO.read(new File(savePath));
            return new ImageIcon(image);
        } catch (IOException e) {
            System.err.println("couldn't download image");
        }
        return null;
    }

    private void downloadImage(String imageUrl, String savePath) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream()) {
            Files.copy(in, Paths.get(savePath));
        } catch (Exception e) {
            //file already exists
        }
    }


    public String[] getCities() {

        String[] cached = _cachedCities.get(_country.getCountry());
        if (cached != null)
            return cached;

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.countrystatecity.in/v1/countries/" + _country.getCountry() + "/cities"))
                    .header("X-CSCAPI-KEY", "QmN5Y090MzdYRGY1NWtHOW5GTUZ5TWZqY2djcE9tM0Q4UExZQXhxOA==")
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
            String[] cityNames = new String[jsonArray.size()];

            int index = 0;
            for (JsonElement element : jsonArray) {
                cityNames[index++] = element.getAsJsonObject().get("name").getAsString();
            }
            _cachedCities.put(_country.getCountry(), cityNames);
            return cityNames;

        } catch (Exception e) {
            System.err.println("http client error");
        }

        return null;
    }

    public Currency getCurrency() {
        return _currency;
    }


    //api classes

    static class Rates {
        @SuppressWarnings("unused")
        private Map<String, Double> rates;

        public double getRateFor(String currencyCode) {
            return rates.get(currencyCode);
        }
    }

    static class Rate {
        double mid;
    }

    static class NBPRate {
        public double getMid() {
            return rates.getFirst().mid;
        }

        List<Rate> rates;
    }

    static class WeatherJson {
        @SuppressWarnings("unused")
        private List<WeatherItem> weather;
        @SuppressWarnings("unused")
        private Main main;

        public String getIcon() {
            return weather.getFirst().getIcon();
        }

        public double getTemperature() {
            return main.getTemperature();
        }
    }

    static class WeatherItem {
        @SuppressWarnings("unused")
        private String icon;

        public String getIcon() {
            return icon;
        }
    }

    static class Main {
        @SuppressWarnings("unused")
        private double temp;

        public double getTemperature() {
            return temp;
        }
    }


}
