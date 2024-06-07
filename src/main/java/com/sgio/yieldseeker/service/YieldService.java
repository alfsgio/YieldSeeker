package com.sgio.yieldseeker.service;

import com.sgio.yieldseeker.enumerations.DPE;
import com.sgio.yieldseeker.enumerations.ExtraSpace;
import com.sgio.yieldseeker.enumerations.Heating;
import com.sgio.yieldseeker.model.Purchase;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class YieldService {

    public List<Purchase> startScrapper() {
        // Initialize a web driver in headless mode
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver();

        List<Purchase> purchases = new ArrayList<Purchase>();
        Matcher matcher;

        System.out.println("--------------- START SCRAPPING ---------------");

        try {
            WebDriverWait waiter = new WebDriverWait(driver, Duration.ofSeconds(10));

            driver.get("https://www.bienici.com/recherche/achat/chilly-mazarin-91380/appartement/studio?surface-min=25&classification-energetique=A%2CB%2CC%2CD");
            List<WebElement> elements = waiter.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div.resultsListContainer a.detailedSheetLink")));

            // Stream = Transform the list into stream
            // Map = Map between property and property.method
            // Collect = Collect results to make a list
            List<String> hrefList = elements.stream()
                    .map(element -> element.getAttribute("href"))
                    .collect(Collectors.toList());

            for(String href : hrefList){
                Purchase purchase = new Purchase();
                purchase.setLink(href);
                driver.get(purchase.getLink());

                // --- APARTMENT

                // - Size
                WebElement size = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.titleInside h1")));
                matcher = Pattern.compile("\\d+\\s*m²").matcher(size.getText()); // Regex : number(s) followed by "m²"
                Integer formatedSize = (Integer) formatToNumber(matcher.find() ? matcher.group() : "", false);
                purchase.setSize(formatedSize);

                // - City
                WebElement city = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.titleInside span.fullAddress")));
                matcher = Pattern.compile("\\d{5}").matcher(city.getText()); // Regex : 5 numbers
                Integer formatedCity = (Integer) formatToNumber(matcher.find() ? matcher.group() : "", false);
                purchase.setCity(formatedCity);

                // - DPE
                WebElement dpeValue = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.dpe-line__classification span div")));
                try { // If the value we retrieve doesn't match witch enumeration
                    DPE dpe = DPE.valueOf(dpeValue.getText());
                    purchase.setDpe(dpe);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error detected : " + e);
                }

                // - Heating
                final String detailHeating = findDetailWithKeyword(waiter, "chauffage");

                List<String> keywords = List.of("collectif", "individuel");
                // Stream = transform the list into stream
                // Filter = return a stream with only the matching elements
                // Pattern = regex with one of the two keyword, only check if exists
                // FindFirst = get the first element
                // OrElse = if value, returns it, otherwise, return the specified value
                String heatingValue = keywords.stream()
                        .filter(keyword -> Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b").matcher(detailHeating).find())
                        .findFirst()
                        .orElse("");

                try { // If the value we retrieve doesn't match with enumeration
                    Heating heating = Heating.valueOf(heatingValue);
                    purchase.setHeating(heating);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error detected : " + e);
                }

                // - Parking
                final String detailParking = findDetailWithKeyword(waiter, "parking");
                purchase.setParking(!detailParking.isBlank());

                // - ExtraSpace
                for(ExtraSpace es : ExtraSpace.values()){
                    final String detailExtraSpace = findDetailWithKeyword(waiter, es.toString());

                    try { // If the value we retrieve doesn't match with enumeration
                        final ExtraSpace extraSpace = ExtraSpace.valueOf(detailExtraSpace);
                        purchase.addExtraSpaces(extraSpace);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error detected : " + e);
                    }
                }

                // --- PURCHASE

                // - Price
                WebElement price = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.titleInside span.ad-price__the-price")));
                Float formatedPrice = (Float) formatToNumber(price.getText(), true);
                purchase.setPrice(formatedPrice);

                // - Price fees
                WebElement priceFees = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.titleInside div.ad-price__fees-infos")));
                matcher = Pattern.compile("\\d+(?:[.,]\\d+)?% TTC").matcher(priceFees.getText()); // Regex : Number(s) followed by "." or "," followed by "% TTC"
                Float formatedPriceFees = (Float) formatToNumber(matcher.find() ? matcher.group() : "", true);
                purchase.setPriceFees(formatedPriceFees);

                // - Price charges
                try { // If there is no div / If there is one, it can have no amount
                    WebElement priceCharges = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.isInCondominiumBlock")));
                    matcher = Pattern.compile("\\b\\d{1,3}(?:\\s?\\d{3})*(?:,\\d{2})?\\s*€").matcher(priceCharges.getText()); // Regex : Number(s) with or without space followed by "€"
                    Float formatedPriceCharges = (Float) formatToNumber(matcher.find() ? matcher.group() : "", true);
                    purchase.setPriceCharges(formatedPriceCharges);
                } catch (TimeoutException e) {
                    System.out.println("Error detected : " + e);
                }

                // ---

                purchases.add(purchase);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return purchases;
        } finally {
            driver.quit(); // Close web browser
        }

        System.out.println("--------------- END SCRAPPING ---------------");

        return purchases;
    }

    private Number formatToNumber(String string, boolean isFloat){
        String stringFormated = string.replaceAll("[^\\d,]",  ""); // Regex : Everything that's not a number or ","
        String numberFormated = stringFormated.replaceAll(",", ".");

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

    private String findDetailWithKeyword(WebDriverWait waiter, String keyword){
        List<WebElement> elements = waiter.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div.allDetails div.labelInfo span")));

        // Stream = transform the list into stream
        // Map = map between property and property.method
        // Collect = collect results to make a list
        List<String> listDetails = elements.stream()
                .map(element -> element.getText().toLowerCase())
                .collect(Collectors.toList());

        // Stream = transform the list into stream
        // Filter = return a stream with only the matching elements
        // FindFirst = get the first element
        // OrElse = if value, returns it, otherwise, return the specified value
        return listDetails.stream()
                .filter(detail -> detail.contains(keyword))
                .findFirst()
                .orElse("");
    }

    private String findInTextWithKeyword(WebDriverWait waiter, List<String> keywords){
        return "";
    }
}