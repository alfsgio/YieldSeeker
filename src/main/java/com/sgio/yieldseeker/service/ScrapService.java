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

    private final String filters = "{\"filterType\":\"buy\",\"propertyType\":[\"flat\"],\"maxRooms\":1,\"minArea\":25,\"energyClassification\":[\"A\",\"B\",\"C\",\"D\"],\"onTheMarket\":[true],\"zoneIdsByTypes\":{\"zoneIds\":[\"-7401\"]}}";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public List<Purchase> testScrapUrl() {
        WebDriverManager.chromedriver().setup();
        final ChromeOptions chromeOptions = new ChromeOptions().addArguments("--headless=new", "--disable-gpu");
        final WebDriver driver = new ChromeDriver(chromeOptions);

        final String encodedUrl = "https://www.bienici.com/realEstateAds.json?filters=" + URLEncoder.encode(filters, StandardCharsets.UTF_8) + "&extensionType=extendedIfNoResult";
        String jsonText = "";

        try {
            driver.get(encodedUrl);
            final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            final WebElement jsonWebElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("pre")));
            jsonText = jsonWebElement.getText();
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
            for(JsonElement jsonPurchase : jsonPurchases){

                // - Non nullable
                String city = jsonPurchase.getAsJsonObject().get("city").getAsString();
                Integer postalCode = jsonPurchase.getAsJsonObject().get("postalCode").getAsInt();
                Float size = jsonPurchase.getAsJsonObject().get("surfaceArea").getAsFloat();
                DPE dpe = DPE.valueOf(jsonPurchase.getAsJsonObject().get("energyClassification").getAsString());

                // Exemple à suivre :
                // String heatingString = jsonObject.has("heating") ? jsonObject.get("heating").getAsString() : null;

                // - Heating
                String heatingString = null;
                if(jsonPurchase.getAsJsonObject().get("heating") != null){
                    heatingString = jsonPurchase.getAsJsonObject().get("heating").getAsString();
                }

                Heating heating = null;
                if(heatingString != null){
                    heating = heatingString.contains("collectif") ? Heating.valueOf("collectif") : Heating.valueOf("individuel");
                }

                // - Parking
                Boolean parking = null;
                if(jsonPurchase.getAsJsonObject().get("parkingPlacesQuantity") != null){
                    parking = jsonPurchase.getAsJsonObject().get("parkingPlacesQuantity").getAsInt() > 0;
                }

                // - ExtraSpaces
                List<ExtraSpace> extraSpaces = new ArrayList<>();
                if(jsonPurchase.getAsJsonObject().get("balconyQuantity") != null
                        && jsonPurchase.getAsJsonObject().get("parkingPlacesQuantity").getAsInt() > 0){
                    extraSpaces.add(ExtraSpace.valueOf("balcon"));
                }
                if(jsonPurchase.getAsJsonObject().get("hasTerrace") != null
                        && jsonPurchase.getAsJsonObject().get("hasTerrace").getAsBoolean()){
                    extraSpaces.add(ExtraSpace.valueOf("terrasse"));
                }
                if(jsonPurchase.getAsJsonObject().get("hasCellar") != null
                        && jsonPurchase.getAsJsonObject().get("hasCellar").getAsBoolean()){
                    extraSpaces.add(ExtraSpace.valueOf("cave"));
                }

                // - Conveniences
                List<Convenience> convenience = new ArrayList<>();
                if(jsonPurchase.getAsJsonObject().get("hasElevator") != null
                        && jsonPurchase.getAsJsonObject().get("hasElevator").getAsBoolean()){
                    convenience.add(Convenience.valueOf("ascenceur"));
                }
                if(jsonPurchase.getAsJsonObject().get("hasIntercom") != null
                        && jsonPurchase.getAsJsonObject().get("hasIntercom").getAsBoolean()){
                    convenience.add(Convenience.valueOf("interphone"));
                }
                if(jsonPurchase.getAsJsonObject().get("hasDoorCode") != null
                        && jsonPurchase.getAsJsonObject().get("hasDoorCode").getAsBoolean()){
                    convenience.add(Convenience.valueOf("digicode"));
                }

                // - Build Apartement
                Apartment apartment = Apartment.builder().city(city).postalCode(postalCode).size(size).dpe(dpe).heating(heating).parking(parking).extraSpaces(extraSpaces).convenience(convenience).build();

                Purchase purchase = new Purchase();
                purchase.setApartment(apartment);

                purchases.add(purchase);
            }
        }

        return purchases;
    }

//    private Apartment scrapApartment(JsonNode node) {
//        final Apartment apartment = new Apartment();
//        Matcher matcher;
//
//        System.out.println("-----> APPARTEMENT PART");
//        System.out.println("-----| Get Size");
//
//        apartment.setSize(node.path("city").asInt());
//
//
//        final WebElement size = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.titleInside h1")));
//        matcher = Pattern.compile("\\d+\\s*m²").matcher(size.getText()); // Regex : number(s) followed by "m²"
//        final Integer formatedSize = (Integer) formatToNumber(matcher.find() ? matcher.group() : "", false);
//        apartment.setSize(formatedSize);
//
//        System.out.println("-----| Get City");
//
//        final WebElement city = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.titleInside span.fullAddress")));
//        matcher = Pattern.compile("\\d{5}").matcher(city.getText()); // Regex : 5 numbers
//        final Integer formatedCity = (Integer) formatToNumber(matcher.find() ? matcher.group() : "", false);
//        apartment.setCity(formatedCity);
//
//        System.out.println("-----| Get DPE");
//
//        try {
//            final WebElement dpeValue = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.dpe-line__classification span div")));
//            final DPE dpe = DPE.valueOf(dpeValue.getText());
//            apartment.setDpe(dpe);
//        } catch (IllegalArgumentException e) { // If the DPE.valueOf(dpeValue.getText()) fails
//            System.out.println("-----} This purchase doesn't contain compliant DPE value");
//        } catch (TimeoutException e) { // If "div.dpe-line__classification span div" not found
//            try {
//                System.out.println("Trying the second DPE tag");
//                final WebElement dpeValue = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.energy-diagnostic-rating span.energy-diagnostic-rating__classification")));
//                final DPE dpe = DPE.valueOf(dpeValue.getText());
//                apartment.setDpe(dpe);
//            } catch (IllegalArgumentException e2) { // If the DPE.valueOf(dpeValue.getText()) fails
//                System.out.println("-----} This purchase doesn't contain compliant DPE value");
//            } catch (
//                    TimeoutException e2) { // If "div.energy-diagnostic-rating span.energy-diagnostic-rating__classification" not found
//                System.out.println("-----} This purchase doesn't contain DPE value");
//            }
//        }
//
//        // - Load all spans and their texts
//        final List<WebElement> spanElements = waiter.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.labelInfo span")));
//        final List<String> spanTexts = spanElements.stream()
//                .map(element -> element.getText().toLowerCase()) // Get text of each span in lowCase
//                .map(text -> text.startsWith("1 ") ? text.substring(2) : text) // Substring the "1 " if the text contains it
//                .toList();
//        // ----------------
//
//        System.out.println("-----| Get Heating");
//
//        // Transform to string's list the enum
//        final List<String> heatingKeywords = Arrays.stream(Heating.values())
//                .map(Enum::name)
//                .toList();
//
//        // Filter to get the detail containing the keyword
//        final String detailHeating = spanTexts.stream()
//                .filter(spanText -> spanText.contains("chauffage"))
//                .findFirst()
//                .orElse("");
//
//        // Find the first heating in the heating keywords that match the detailHeating string
//        final String heatingValue = heatingKeywords.stream()
//                .filter(keyword -> Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b").matcher(detailHeating).find())
//                .findFirst()
//                .orElse("");
//
//        try {
//            apartment.setHeating(Heating.valueOf(heatingValue));
//        } catch (IllegalArgumentException e) {
//            System.out.println("-----} This purchase doesn't contain heating value");
//        }
//
//        System.out.println("-----| Get Parking");
//
//        // Filter to get the detail containing the keyword
//        final String detailParking = spanTexts.stream()
//                .filter(spanText -> spanText.contains("parking"))
//                .findFirst()
//                .orElse("");
//
//        apartment.setParking(!detailParking.isBlank());
//
//        System.out.println("-----| Get ExtraSpace");
//
//        Arrays.stream(ExtraSpace.values())
//                .filter(extraSpace -> spanTexts.contains(extraSpace.name()))
//                .toList()
//                .forEach(extraSpace -> apartment.addExtraSpaces(extraSpace));
//
//        System.out.println("-----| Get Convenience");
//
//        Arrays.stream(Convenience.values())
//                .filter(convenience -> spanTexts.contains(convenience.name()))
//                .toList()
//                .forEach(convenience -> apartment.addConvenience(convenience));
//
//        return apartment;
//    }

    private Purchase scrapPurchase() {
        final Purchase purchase = new Purchase();
        Matcher matcher;

        System.out.println("-----> PURCHASE PART");
        System.out.println("-----| Get Price");

        final WebElement price = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.titleInside span.ad-price__the-price")));
        final Float formatedPrice = (Float) formatToNumber(price.getText(), true);
        purchase.setPrice(formatedPrice);

        System.out.println("-----| Get Price Fees");

        try {
            final WebElement priceFees = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.titleInside div.ad-price__fees-infos")));
            matcher = Pattern.compile("\\d+(?:[.,]\\d+)?% TTC").matcher(priceFees.getText()); // Regex : Number(s) followed by "." or "," followed by "% TTC"
            final Float formatedPriceFees = (Float) formatToNumber(matcher.find() ? matcher.group() : "", true);
            purchase.setPriceFees(formatedPriceFees);
        } catch (TimeoutException e) {
            System.out.println("-----} This purchase doesn't contain price fees value");
        }

        System.out.println("-----| Get Price Charges");

        try { // If there is no div / If there is one, it can have no amount
            final WebElement priceCharges = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.isInCondominiumBlock")));
            matcher = Pattern.compile("\\b\\d{1,3}(?:\\s?\\d{3})*(?:,\\d{2})?\\s*€").matcher(priceCharges.getText()); // Regex : Number(s) with or without space followed by "€"
            final Float formatedPriceCharges = (Float) formatToNumber(matcher.find() ? matcher.group() : "", true);
            purchase.setPriceCharges(formatedPriceCharges);
        } catch (TimeoutException e) {
            System.out.println("-----} This purchase doesn't contain price charges value");
        }

        // Get the all texts
        final WebElement allText = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.see-more-description span.see-more-description__content")));
        // ----------------

        System.out.println("-----| Get Rented");

        purchase.setRented(allText.getText().toLowerCase().contains("vendu loué"));

        System.out.println("-----| Get Procedure in progress");

        try { // If there is no div / If there is one, it can have no amount
            final WebElement procedureInProgress = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.isInCondominiumBlock")));
            purchase.setProcedureInProgress(procedureInProgress.getText().contains("Une procédure est en cours."));
        } catch (TimeoutException e) {
            System.out.println("-----} This purchase doesn't contain procedure in progress value");
        }

        return purchase;
    }

    private Rental scrapRental() {
        final Rental rental = new Rental();
        Matcher matcher;

        System.out.println("-----> RENTAL PART");
        System.out.println("-----| Get Price Total");



        return rental;
    }

    public List<Purchase> scrapPurchases() {
        final List<Purchase> purchases = new ArrayList<Purchase>();
        Matcher matcher;

//        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");
        WebDriver webDriver = new ChromeDriver(chromeOptions);

        waiter = new WebDriverWait(webDriver, Duration.ofSeconds(120));

        try {
            System.out.println("****> START SCRAPPING");
//            webDriver.get(urlPart1 + "achat" + urlPart2);

            final List<WebElement> elements = waiter.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.resultsListContainer a.detailedSheetLink")));

            // Get href of each a.detailedSheetLink
            final List<String> hrefList = elements.stream()
                    .map(element -> element.getAttribute("href"))
                    .toList();

            for(String href : hrefList) {
                webDriver.get(href);
                System.out.println("****| " + (purchases.size() + 1) + " Purchase");
//                final Apartment apartment = scrapApartment();
                final Purchase purchase = scrapPurchase();
                purchase.setPurchaseAdLink(href);
//                purchase.setApartment(apartment);


                purchases.add(purchase);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            webDriver.quit(); // Close web browser
        }

        System.out.println("-----> END SCRAPPING");

        return purchases;
    }

    private Number formatToNumber(String string, boolean isFloat){
        final String stringFormated = string.replaceAll("[^\\d,]",  ""); // Regex : Everything that's not a number or ","
        final String numberFormated = stringFormated.replaceAll(",", ".");

        if(!numberFormated.isBlank()) {
            if (isFloat) {
                return Float.parseFloat(numberFormated);
            } else {
                return Integer.parseInt(numberFormated);
            }
        } else {
            return null;
        }
    }
}

