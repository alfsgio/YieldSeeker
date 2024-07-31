package com.sgio.yieldseeker.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sgio.yieldseeker.builder.PurchaseBuilder;
import com.sgio.yieldseeker.builder.RentalBuilder;
import com.sgio.yieldseeker.model.Apartment;
import com.sgio.yieldseeker.model.Purchase;
import com.sgio.yieldseeker.model.Rental;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CollectorService {

    private static final Logger logger = LoggerFactory.getLogger(YieldService.class);

    @Autowired
    private WebDriver webDriver;

    @Autowired
    private WebDriverWait webDriverWait;

    @Autowired
    private PurchaseBuilder purchaseBuilder;

    @Autowired
    private RentalBuilder rentalBuilder;

    public Map<String, Map<Integer, List<?>>> collectAll() {
        List<Purchase> collectedPurchases = collect(Purchase.class, webDriver, webDriverWait);
        List<Rental> collectedRentals = collect(Rental.class, webDriver, webDriverWait);

        Map<Integer, List<Purchase>> sortedByCityPurchases = sortByCity(Purchase.class, collectedPurchases);
        Map<Integer, List<Rental>> sortedByCityRentals = sortByCity(Rental.class, collectedRentals);

        Map<String, Map<Integer, List<?>>> mapAll = new HashMap<>();
        mapAll.put("Purchases", new HashMap<>(sortedByCityPurchases));
        mapAll.put("Rentals", new HashMap<>(sortedByCityRentals));

        return mapAll;
    }

    private <T> Map<Integer, List<T>> sortByCity(Class<T> clazz, List<T> listToSort){
        final Map<Integer, List<T>> sortedMap = new HashMap<>();

        listToSort.forEach(item -> {
            try {
                final Method getApartmentMethod = clazz.getMethod("getApartment");
                final Apartment apartment = (Apartment)getApartmentMethod.invoke(item);
                final Integer postalCode = apartment.getPostalCode();

                if(!sortedMap.containsKey(postalCode)){
                    sortedMap.put(postalCode, new ArrayList<>());
                }
                sortedMap.get(postalCode).add(item);
            } catch (Exception e) {
                logger.error("Error when tyring to sort by city", e);
            }
        });

        return sortedMap;
    }

    private <T> List<T> collect(Class<T> clazz, WebDriver driver, WebDriverWait wait){
        final String url = "https://www.bienici.com/realEstateAds.json?filters=%7B%22size%22%3A500%2C%22filterType%22%3A%22$$$%22%2C%22propertyType%22%3A%5B%22flat%22%5D%2C%22maxRooms%22%3A1%2C%22minArea%22%3A20%2C%22maxArea%22%3A50%2C%22energyClassification%22%3A%5B%22A%22%2C%22B%22%2C%22C%22%2C%22D%22%5D%2C%22onTheMarket%22%3A%5Btrue%5D%2C%22zoneIdsByTypes%22%3A%7B%22zoneIds%22%3A%5B%22-7401%22%2C%22-7458%22%2C%22-7444%22%2C%22-7449%22%2C%22-7383%22%5D%7D%7D&extensionType=extendedIfNoResult";

        String finalUrl = "";
        if(clazz == Purchase.class){
            finalUrl = url.replace("$$$", "buy");
        } else if(clazz == Rental.class){
            finalUrl = url.replace("$$$", "rent");
        }

        String jsonText = "";
        try {
            driver.get(finalUrl);
            jsonText = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("pre"))).getText();
        } catch (Exception e) {
            logger.error("Error when trying to get data from url", e);
        }

        JsonArray jsonCollected = null;
        if(!jsonText.isBlank()){
            try {
                jsonCollected = JsonParser.parseString(jsonText)
                        .getAsJsonObject()
                        .get("realEstateAds").getAsJsonArray();
            } catch (JsonSyntaxException e) {
                logger.error("Error when trying to parse "+clazz.getName()+" list", e);
            }
        }

        List<T> collected = new ArrayList<>();

        if(jsonCollected!= null){
            jsonCollected.forEach(jsonElement -> {
                if(clazz == Purchase.class){
                    collected.add(clazz.cast(purchaseBuilder.from(jsonElement.getAsJsonObject()).build()));
                } else if(clazz == Rental.class){
                    collected.add(clazz.cast(rentalBuilder.from(jsonElement.getAsJsonObject()).build()));
                }
            });
        }

        return collected;
    }
}