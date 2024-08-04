package com.sgio.yieldseeker.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sgio.yieldseeker.builder.PurchaseBuilder;
import com.sgio.yieldseeker.builder.RentalBuilder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CollectorService {

    private static final Logger logger = LoggerFactory.getLogger(CollectorService.class);

    @Autowired
    private PurchaseBuilder purchaseBuilder;

    @Autowired
    private RentalBuilder rentalBuilder;

    /**
     * Returns an Image object that can then be painted on the screen.
     * The url argument must specify an absolute <a href="#{@link}">{@link URL}</a>. The name
     * argument is a specifier that is relative to the url argument.
     * <p>
     *
     * @param  url  an absolute URL giving the base location of the image
     * @return      a map containing all
     */
    public Map<String, Map<Integer, List<?>>> collectAll() {

        // In this app, we are forced to use Selenium to emulate web browser.
        // We can't make direct web requests as the server reject us (robots are blocked).
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

    /**
     * Returns an Image object that can then be painted on the screen.
     * The url argument must specify an absolute <a href="#{@link}">{@link URL}</a>. The name
     * argument is a specifier that is relative to the url argument.
     * <p>
     *
     * @param  url  an absolute URL giving the base location of the image
     * @return      a map containing all
     */
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

    /**
     * Collect the data for a given type of real estate (purchase or rental).
     * All the other parameters is already provided in the URL.
     * An example of the bienici datas return can be find at 'resources/specific_resources/'.
     *
     * @param  clazz    the class representing the type of real estate collected
     * @param  driver   the webdriver used to collect the datas
     * @param  wait     the webdriver waiter
     * @return          a list of real estate ads for the given class
     */
    private <T> List<T> collect(Class<T> clazz, WebDriver driver, WebDriverWait wait){
        final String url = "https://www.bienici.com/realEstateAds.json?filters=%7B%22size%22%3A500%2C%22filterType%22%3A%22$$$%22%2C%22propertyType%22%3A%5B%22flat%22%5D%2C%22maxRooms%22%3A1%2C%22minArea%22%3A20%2C%22maxArea%22%3A50%2C%22energyClassification%22%3A%5B%22A%22%2C%22B%22%2C%22C%22%2C%22D%22%5D%2C%22onTheMarket%22%3A%5Btrue%5D%2C%22zoneIdsByTypes%22%3A%7B%22zoneIds%22%3A%5B%22-7401%22%2C%22-7458%22%2C%22-7444%22%2C%22-7449%22%2C%22-7383%22%5D%7D%7D&extensionType=extendedIfNoResult";

        // Updating the URL with the type of ads we're trying to collect
        String finalUrl = "";
        if(clazz == Purchase.class){
            finalUrl = url.replace("$$$", "buy");
        } else if(clazz == Rental.class){
            finalUrl = url.replace("$$$", "rent");
        }

        // Trying to get the datas from the url
        String jsonText = "";
        try {
            driver.get(finalUrl);
            jsonText = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("pre"))).getText();
        } catch (Exception e) {
            logger.error("Error when trying to get data from url", e);
        }

        // Parsing the response to get only the part we are looking for
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

        // Filling the returned list with the parsed data through builders
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