package com.sgio.yieldseeker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sgio.yieldseeker.enumerations.Convenience;
import com.sgio.yieldseeker.enumerations.DPE;
import com.sgio.yieldseeker.enumerations.ExtraSpace;
import com.sgio.yieldseeker.enumerations.Heating;
import com.sgio.yieldseeker.model.Apartment;
import com.sgio.yieldseeker.model.Purchase;
import com.sgio.yieldseeker.model.Rental;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScrapService {

    private WebDriverWait waiter;

    private final String zoneCode91 = "-7401";
    private final String zoneCode92 = "-7449";
    private final String zoneCode94 = "-7458";
    private final String zoneCode75 = "-7444";
    private final String zoneCode77 = "-7383";
    private final String zoneCode78 = "-7457";

//    https://res.bienici.com/place.json?q=essonne-91
// pour avoir les codes par zone

    private final String filtersPurchase = "{\"filterType\":\"buy\",\"propertyType\":[\"flat\"],\"maxRooms\":1,\"minArea\":25,\"energyClassification\":[\"A\",\"B\",\"C\",\"D\"],\"onTheMarket\":[true],\"zoneIdsByTypes\":{\"zoneIds\":[\"-7401\"]}}";
    private final String filtersRental = "{\"filterType\":\"rent\",\"propertyType\":[\"flat\"],\"maxRooms\":1,\"minArea\":25,\"energyClassification\":[\"A\",\"B\",\"C\",\"D\"],\"onTheMarket\":[true],\"zoneIdsByTypes\":{\"zoneIds\":[\"-7401\"]}}";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public List<Purchase> testScrapUrl() {
        WebDriverManager.chromedriver().setup();
        final ChromeOptions chromeOptions = new ChromeOptions().addArguments("--headless=new", "--disable-gpu");
        final WebDriver driver = new ChromeDriver(chromeOptions);
        final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        final String encodedUrl = "https://www.bienici.com/realEstateAds.json?filters=" + URLEncoder.encode(filtersPurchase, StandardCharsets.UTF_8) + "&extensionType=extendedIfNoResult";
        String jsonText = "";

        try {
            driver.get(encodedUrl);
            jsonText = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("pre"))).getText();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // Close web browser
        }

        JsonArray jsonPurchases = null;
        if(!jsonText.isBlank()){
            jsonPurchases = JsonParser.parseString(jsonText)
                    .getAsJsonObject()
                    .get("realEstateAds").getAsJsonArray();
        }

        List<Purchase> purchases = new ArrayList<>();

        if(jsonPurchases != null){
            jsonPurchases.forEach(jsonElement -> purchases.add(this.fillPurchase(jsonElement.getAsJsonObject())));
        }

        return purchases;
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
        Boolean parking = jsonDatas.has("parkingPlacesQuantity") && jsonDatas.get("parkingPlacesQuantity").getAsInt() > 0;

        // - ExtraSpaces
        List<ExtraSpace> extraSpaces = new ArrayList<>();
        if(jsonDatas.has("parkingPlacesQuantity") && jsonDatas.get("parkingPlacesQuantity").getAsInt() > 0){
            extraSpaces.add(ExtraSpace.valueOf("balcon"));
        }
        if(jsonDatas.has("hasTerrace") && jsonDatas.get("hasTerrace").getAsBoolean()){
            extraSpaces.add(ExtraSpace.valueOf("terrasse"));
        }
        if(jsonDatas.has("hasCellar") && jsonDatas.get("hasCellar").getAsBoolean()){
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
        Float price = jsonDatas.get("price").getAsFloat();
        Float agencyFeePercentage = jsonDatas.has("agencyFeePercentage") ? jsonDatas.get("agencyFeePercentage").getAsFloat() : 0;
        Float annualCondominiumFees = jsonDatas.has("annualCondominiumFees") ? jsonDatas.get("annualCondominiumFees").getAsFloat() : 0;
        Boolean procedureInProgress = jsonDatas.has("isCondominiumInProcedure") && jsonDatas.get("isCondominiumInProcedure").getAsBoolean();
        Boolean rented = jsonDatas.get("description").getAsString().toLowerCase().contains("vendu loué");

        Float priceTotal = jsonDatas.get("price").getAsFloat();
        Float priceCharges;
        Float priceEnergy;

        Apartment apartment = this.fillApartment(jsonDatas);

        return new Rental();
    }
}

