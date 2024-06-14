package com.sgio.yieldseeker.service;

import com.sgio.yieldseeker.enumerations.Convenience;
import com.sgio.yieldseeker.enumerations.DPE;
import com.sgio.yieldseeker.enumerations.ExtraSpace;
import com.sgio.yieldseeker.enumerations.Heating;
import com.sgio.yieldseeker.model.Apartment;
import com.sgio.yieldseeker.model.Purchase;
import com.sgio.yieldseeker.model.Rental;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScrapService {

    private WebDriverWait waiter;

    private String urlPart1 = "https://www.bienici.com/recherche/";
    private String urlPart2 = "/chilly-mazarin-91380,morangis-91420,massy-91300,longjumeau-91160/appartement/studio?surface-min=20&classification-energetique=A%2CB%2CC%2CD&mode=galerie";

    private Apartment scrapApartment() {
        final Apartment apartment = new Apartment();
        Matcher matcher;

        System.out.println("-----> APPARTEMENT PART");
        System.out.println("-----| Get Size");

        final WebElement size = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.titleInside h1")));
        matcher = Pattern.compile("\\d+\\s*m²").matcher(size.getText()); // Regex : number(s) followed by "m²"
        final Integer formatedSize = (Integer) formatToNumber(matcher.find() ? matcher.group() : "", false);
        apartment.setSize(formatedSize);

        System.out.println("-----| Get City");

        final WebElement city = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.titleInside span.fullAddress")));
        matcher = Pattern.compile("\\d{5}").matcher(city.getText()); // Regex : 5 numbers
        final Integer formatedCity = (Integer) formatToNumber(matcher.find() ? matcher.group() : "", false);
        apartment.setCity(formatedCity);

        System.out.println("-----| Get DPE");

        try {
            final WebElement dpeValue = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.dpe-line__classification span div")));
            final DPE dpe = DPE.valueOf(dpeValue.getText());
            apartment.setDpe(dpe);
        } catch (IllegalArgumentException e) { // If the DPE.valueOf(dpeValue.getText()) fails
            System.out.println("-----} This purchase doesn't contain compliant DPE value");
        } catch (TimeoutException e) { // If "div.dpe-line__classification span div" not found
            try {
                System.out.println("Trying the second DPE tag");
                final WebElement dpeValue = waiter.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.energy-diagnostic-rating span.energy-diagnostic-rating__classification")));
                final DPE dpe = DPE.valueOf(dpeValue.getText());
                apartment.setDpe(dpe);
            } catch (IllegalArgumentException e2) { // If the DPE.valueOf(dpeValue.getText()) fails
                System.out.println("-----} This purchase doesn't contain compliant DPE value");
            } catch (
                    TimeoutException e2) { // If "div.energy-diagnostic-rating span.energy-diagnostic-rating__classification" not found
                System.out.println("-----} This purchase doesn't contain DPE value");
            }
        }

        // - Load all spans and their texts
        final List<WebElement> spanElements = waiter.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.labelInfo span")));
        final List<String> spanTexts = spanElements.stream()
                .map(element -> element.getText().toLowerCase()) // Get text of each span in lowCase
                .map(text -> text.startsWith("1 ") ? text.substring(2) : text) // Substring the "1 " if the text contains it
                .toList();
        // ----------------

        System.out.println("-----| Get Heating");

        // Transform to string's list the enum
        final List<String> heatingKeywords = Arrays.stream(Heating.values())
                .map(Enum::name)
                .toList();

        // Filter to get the detail containing the keyword
        final String detailHeating = spanTexts.stream()
                .filter(spanText -> spanText.contains("chauffage"))
                .findFirst()
                .orElse("");

        // Find the first heating in the heating keywords that match the detailHeating string
        final String heatingValue = heatingKeywords.stream()
                .filter(keyword -> Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b").matcher(detailHeating).find())
                .findFirst()
                .orElse("");

        try {
            apartment.setHeating(Heating.valueOf(heatingValue));
        } catch (IllegalArgumentException e) {
            System.out.println("-----} This purchase doesn't contain heating value");
        }

        System.out.println("-----| Get Parking");

        // Filter to get the detail containing the keyword
        final String detailParking = spanTexts.stream()
                .filter(spanText -> spanText.contains("parking"))
                .findFirst()
                .orElse("");

        apartment.setParking(!detailParking.isBlank());

        System.out.println("-----| Get ExtraSpace");

        Arrays.stream(ExtraSpace.values())
                .filter(extraSpace -> spanTexts.contains(extraSpace.name()))
                .toList()
                .forEach(extraSpace -> apartment.addExtraSpaces(extraSpace));

        System.out.println("-----| Get Convenience");

        Arrays.stream(Convenience.values())
                .filter(convenience -> spanTexts.contains(convenience.name()))
                .toList()
                .forEach(convenience -> apartment.addConvenience(convenience));

        return apartment;
    }

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

        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");
        WebDriver webDriver = new ChromeDriver(chromeOptions);

        waiter = new WebDriverWait(webDriver, Duration.ofSeconds(120));

        try {
            System.out.println("****> START SCRAPPING");
            webDriver.get(urlPart1 + "achat" + urlPart2);

            final List<WebElement> elements = waiter.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.resultsListContainer a.detailedSheetLink")));

            // Get href of each a.detailedSheetLink
            final List<String> hrefList = elements.stream()
                    .map(element -> element.getAttribute("href"))
                    .toList();

            for(String href : hrefList) {
                webDriver.get(href);
                System.out.println("****| " + (purchases.size() + 1) + " Purchase");
                final Apartment apartment = scrapApartment();
                final Purchase purchase = scrapPurchase();
                purchase.setPurchaseAdLink(href);
                purchase.setApartment(apartment);

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