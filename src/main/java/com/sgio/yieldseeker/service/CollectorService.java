package com.sgio.yieldseeker.service;

import com.google.gson.*;
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

    public Map<String, Map<Integer, ?>> collectAll() {
        WebDriverManager.chromedriver().setup();
        final ChromeOptions chromeOptions = new ChromeOptions().addArguments("--headless=new", "--disable-gpu");
        final WebDriver driver = new ChromeDriver(chromeOptions);
        final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<Purchase> collectedPurchases = collect(Purchase.class, driver, wait);
        List<Rental> collectedRentals = collect(Rental.class, driver, wait);

        Map<Integer, List<Purchase>> sortedByCityPurchases = sortByCity(Purchase.class, collectedPurchases);
        Map<Integer, List<Rental>> sortedByCityRentals = sortByCity(Rental.class, collectedRentals);

        Map<String, Map<Integer, ?>> mapAll = new HashMap<>();
        mapAll.put("Purchases", sortedByCityPurchases);
        mapAll.put("Rentals",sortedByCityRentals);

        driver.quit(); // Close web browser
        return mapAll;
    }

    private <T> Map<Integer, List<T>> sortByCity(Class<T> clazz, List<T> listToSort){
        Map<Integer, List<T>> sortedMap = new HashMap<>();

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
        String filters = "";
        if(clazz == Purchase.class){
            filters = "{\"filterType\":\"buy\",\"propertyType\":[\"flat\"],\"maxRooms\":1,\"minArea\":25,\"energyClassification\":[\"A\",\"B\",\"C\",\"D\"],\"onTheMarket\":[true],\"zoneIdsByTypes\":{\"zoneIds\":[\"-7401\"]}}";
        } else if(clazz == Rental.class){
            filters = "{\"filterType\":\"rent\",\"propertyType\":[\"flat\"],\"maxRooms\":1,\"minArea\":25,\"energyClassification\":[\"A\",\"B\",\"C\",\"D\"],\"onTheMarket\":[true],\"zoneIdsByTypes\":{\"zoneIds\":[\"-7401\"]}}";
        }

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
                    collected.add(clazz.cast(this.fillPurchase(jsonElement.getAsJsonObject())));
                } else if(clazz == Rental.class){
                    collected.add(clazz.cast(this.fillRental(jsonElement.getAsJsonObject())));
                }
            });
        }

        return collected;
    }

    private Apartment fillApartment(JsonObject jsonDatas){
        // - Non nullable
        String city = jsonDatas.get("city").getAsString();
        Integer postalCode = jsonDatas.get("postalCode").getAsInt();
        Float surfaceArea = jsonDatas.get("surfaceArea").getAsFloat();
        DPE dpe = DPE.valueOf(jsonDatas.get("energyClassification").getAsString());

        // - Heating
        String heatingString = jsonDatas.has("heating") ? jsonDatas.get("heating").getAsString() : "";
        Heating heating = heatingString.contains("collectif") ? Heating.valueOf("collectif") : Heating.valueOf("individuel");

        // - Parking
        Boolean parking = (
                (jsonDatas.has("parkingPlacesQuantity") && jsonDatas.get("parkingPlacesQuantity").getAsInt() > 0)
                || (jsonDatas.has("enclosedParkingQuantity") && jsonDatas.get("enclosedParkingQuantity").getAsInt() > 0)
        );

        // - ExtraSpaces
        List<ExtraSpace> extraSpaces = new ArrayList<>();

        if((jsonDatas.has("hasBalcony") && jsonDatas.get("hasBalcony").getAsBoolean())
        || (jsonDatas.has("balconyQuantity") && jsonDatas.get("balconyQuantity").getAsInt() > 0)
        ){
            extraSpaces.add(ExtraSpace.valueOf("balcon"));
        }
        if((jsonDatas.has("hasTerrace") && jsonDatas.get("hasTerrace").getAsBoolean())
        || (jsonDatas.has("terracesQuantity") && jsonDatas.get("terracesQuantity").getAsInt() > 0)){
            extraSpaces.add(ExtraSpace.valueOf("terrasse"));
        }
        if(jsonDatas.has("hasCellar") && jsonDatas.get("hasCellar").getAsBoolean()
        || (jsonDatas.has("cellarsOrUndergroundsQuantity") && jsonDatas.get("cellarsOrUndergroundsQuantity").getAsInt() > 0)){
            extraSpaces.add(ExtraSpace.valueOf("cave"));
        }

        // - Conveniences
        List<Convenience> convenience = new ArrayList<>();
        if(jsonDatas.has("hasElevator") && jsonDatas.get("hasElevator").getAsBoolean()){
            convenience.add(Convenience.valueOf("ascenseur"));
        }
        if(jsonDatas.has("hasIntercom") && jsonDatas.get("hasIntercom").getAsBoolean()){
            convenience.add(Convenience.valueOf("interphone"));
        }
        if(jsonDatas.has("hasDoorCode") && jsonDatas.get("hasDoorCode").getAsBoolean()){
            convenience.add(Convenience.valueOf("digicode"));
        }

        // - Build Apartement
        return Apartment.builder().city(city).postalCode(postalCode).surfaceArea(surfaceArea).dpe(dpe).heating(heating)
                .parking(parking).extraSpaces(extraSpaces).convenience(convenience).build();
    }

    private Purchase fillPurchase(JsonObject jsonDatas){
        Float price = jsonDatas.get("price").getAsFloat();
        Float agencyFeePercentage = jsonDatas.has("agencyFeePercentage") ? jsonDatas.get("agencyFeePercentage").getAsFloat() : 0;
        Float annualCondominiumFees = jsonDatas.has("annualCondominiumFees") ? jsonDatas.get("annualCondominiumFees").getAsFloat() : 0;
        Boolean procedureInProgress = jsonDatas.has("isCondominiumInProcedure") && jsonDatas.get("isCondominiumInProcedure").getAsBoolean();
        Boolean rented = jsonDatas.get("description").getAsString().toLowerCase().contains("vendu loué");

        Apartment apartment = this.fillApartment(jsonDatas);

        return Purchase.builder().apartment(apartment).price(price).agencyFeePercentage(agencyFeePercentage).annualCondominiumFees(annualCondominiumFees)
                .rented(rented).procedureInProgress(procedureInProgress).build();
    }

    private Rental fillRental(JsonObject jsonDatas){
        Float priceTotal = jsonDatas.get("price").getAsFloat();
        Float priceCharges = jsonDatas.has("charges") ? jsonDatas.get("charges").getAsFloat() : 0;
        Boolean isFurnished = jsonDatas.has("isFurnished") && jsonDatas.get("isFurnished").getAsBoolean();

        Apartment apartment = this.fillApartment(jsonDatas);

        return Rental.builder().apartment(apartment).priceTotal(priceTotal).priceCharges(priceCharges).isFurnished(isFurnished).build();
    }
}

