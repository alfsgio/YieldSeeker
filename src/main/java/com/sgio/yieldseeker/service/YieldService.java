package com.sgio.yieldseeker.service;

import com.sgio.yieldseeker.model.Purchase;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
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
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        List<Purchase> purchases = new ArrayList<Purchase>();
        Matcher matcher;

        try {
            WebDriverWait waiter = new WebDriverWait(driver, Duration.ofSeconds(5));

            driver.get("https://www.bienici.com/recherche/achat/chilly-mazarin-91380/appartement/studio?surface-min=25&classification-energetique=A%2CB%2CC%2CD");
            List<WebElement> elements = waiter.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div.resultsListContainer a.detailedSheetLink")));

            // Stream = transform the list into stream / Map = map between property and property.method / Collect = collect results to make a list
            List<String> hrefList = elements.stream().map(element -> element.getAttribute("href")).collect(Collectors.toList());

            for(String href : hrefList){
                // Example :
                Purchase purchase = new Purchase();
                purchase.setLink(href);
                driver.get(purchase.getLink());

                // Size
                WebElement size = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.titleInside h1")));
                matcher = Pattern.compile("\\d+\\s*m²").matcher(size.getText()); // Regex : Numéro(s) suivis de "m²"
                Integer formatedSize = (Integer) formatToNumber(matcher.find() ? matcher.group() : "", false);
                purchase.setSize(formatedSize);

                // City
                WebElement city = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.titleInside span.fullAddress")));
                matcher = Pattern.compile("\\d{5}").matcher(city.getText()); // Regex : 5 numéros
                Integer formatedCity = (Integer) formatToNumber(matcher.find() ? matcher.group() : "", false);
                purchase.setCity(formatedCity);

                // Price
                WebElement price = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.titleInside span.ad-price__the-price")));
                Float formatedPrice = (Float) formatToNumber(price.getText(), true);
                purchase.setPrice(formatedPrice);

                // Price fees
                WebElement priceFees = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.titleInside div.ad-price__fees-infos")));
                matcher = Pattern.compile("\\d+(?:[.,]\\d+)?% TTC").matcher(priceFees.getText()); // Regex : Numéro(s) suivis de "." ou "," suivis de "% TTC"
                Float formatedPriceFees = (Float) formatToNumber(matcher.find() ? matcher.group() : "", true);
                purchase.setPriceFees(formatedPriceFees);

                purchases.add(purchase);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return purchases;
        } finally {
            driver.quit(); // Ferme le navigateur
        }

        return purchases;
    }

    private Number formatToNumber(String string, boolean isFloat){
        String stringFormated = string.replaceAll("\\D", ""); // Regex : Tout ce qui n'est pas un numéro
        String numberFormated = string.replaceAll(",", ".");

        if(!numberFormated.isBlank()) {
            if (isFloat) {
                return Float.parseFloat(numberFormated);
            } else {
                return Integer.parseInt(numberFormated);
            }
        } else {
            return formatToNumber("0", isFloat);
        }
    }
}