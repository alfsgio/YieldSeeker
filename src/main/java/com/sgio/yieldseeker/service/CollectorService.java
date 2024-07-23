package com.sgio.yieldseeker.service;

import com.google.gson.*;
import com.sgio.yieldseeker.builder.PurchaseBuilder;
import com.sgio.yieldseeker.builder.RentalBuilder;
import com.sgio.yieldseeker.enumerations.Convenience;
import com.sgio.yieldseeker.enumerations.DPE;
import com.sgio.yieldseeker.enumerations.ExtraSpace;
import com.sgio.yieldseeker.enumerations.Heating;
import com.sgio.yieldseeker.model.Apartment;
import com.sgio.yieldseeker.model.Purchase;
import com.sgio.yieldseeker.model.Rental;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CollectorService {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public Map<String, Map<Integer, List<?>>> collectAll() {
        WebDriverManager.chromedriver().setup();

        final ChromeOptions chromeOptions = new ChromeOptions().addArguments("--headless=new", "--disable-gpu");
        final WebDriver driver = new ChromeDriver(chromeOptions);
        final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            List<Purchase> collectedPurchases = collect(Purchase.class, driver, wait);
            List<Rental> collectedRentals = collect(Rental.class, driver, wait);

            Map<Integer, List<Purchase>> sortedByCityPurchases = sortByCity(Purchase.class, collectedPurchases);
            Map<Integer, List<Rental>> sortedByCityRentals = sortByCity(Rental.class, collectedRentals);

            Map<String, Map<Integer, List<?>>> mapAll = new HashMap<>();
            mapAll.put("Purchases", new HashMap<>(sortedByCityPurchases));
            mapAll.put("Rentals", new HashMap<>(sortedByCityRentals));

            return mapAll;
        } finally {
            driver.quit(); // Always close web browser
        }
    }

    private <T> Map<Integer, List<T>> sortByCity(Class<T> clazz, List<T> listToSort){
        final Map<Integer, List<T>> sortedMap = new HashMap<>();

        listToSort.forEach(item -> {
            try {
                final Method getApartmentMethod = clazz.getMethod("getApartment");
                final Apartment apartment = (Apartment)getApartmentMethod.invoke(item);
                final Integer postalCode = apartment.getPostalCode();
                sortedMap.computeIfAbsent(postalCode, k -> new ArrayList<>()).add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return sortedMap;
    }

    private <T> List<T> collect(Class<T> clazz, WebDriver driver, WebDriverWait wait){
        // TO EXTRACT
        String filters = "";
        if(clazz == Purchase.class){
            filters = "{\"filterType\":\"buy\",\"propertyType\":[\"flat\"],\"maxRooms\":1,\"minArea\":25,\"energyClassification\":[\"A\",\"B\",\"C\",\"D\"],\"onTheMarket\":[true],\"zoneIdsByTypes\":{\"zoneIds\":[\"-7401\"]}}";
        } else if(clazz == Rental.class){
            filters = "{\"filterType\":\"rent\",\"propertyType\":[\"flat\"],\"maxRooms\":1,\"minArea\":25,\"energyClassification\":[\"A\",\"B\",\"C\",\"D\"],\"onTheMarket\":[true],\"zoneIdsByTypes\":{\"zoneIds\":[\"-7401\"]}}";
        }
        // TO EXTRACT

        final String encodedUrl = "https://www.bienici.com/realEstateAds.json?filters=" + URLEncoder.encode(filters, StandardCharsets.UTF_8) + "&extensionType=extendedIfNoResult";
        String jsonText = "";

        try {
            driver.get(encodedUrl);
            jsonText = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("pre"))).getText();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonArray jsonCollected = null;
        if(!jsonText.isBlank()){
            try {
                jsonCollected = JsonParser.parseString(jsonText)
                        .getAsJsonObject()
                        .get("realEstateAds").getAsJsonArray();
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        List<T> collected = new ArrayList<>();

        if(jsonCollected!= null){
            jsonCollected.forEach(jsonElement -> {
                if(clazz == Purchase.class){
                    collected.add(clazz.cast(new PurchaseBuilder().from(jsonElement.getAsJsonObject()).build()));
                } else if(clazz == Rental.class){
                    collected.add(clazz.cast(new RentalBuilder().from(jsonElement.getAsJsonObject()).build()));
                }
            });
        }

        return collected;
    }
}

