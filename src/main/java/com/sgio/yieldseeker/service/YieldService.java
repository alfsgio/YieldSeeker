package com.sgio.yieldseeker.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YieldService {

    @Autowired
    private RestTemplate restTemplate;

    public String startScrapper() {
//        String apiUrl = "https://www.seloger.com/search-bff/api/externaldata?from=0&size=25";

//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Cookie","AB-TESTING=%7B%22relatedAdsPosition%22%3A%22relatedAdsAfterDocumentationServices%22%7D; i18next=fr; webGLBenchmarkScore=9.964354697102722");
//        headers.set("Referer","https://www.bienici.com/recherche/achat/chilly-mazarin-91380/appartement/studio?surface-min=30&classification-energetique=A%2CB%2CC%2CD");
//        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0");
//
//        String apiTest = "https://www.bienici.com/realEstateAds.json?filters=%7B%22size%22%3A24%2C%22from%22%3A0%2C%22showAllModels%22%3Afalse%2C%22filterType%22%3A%22buy%22%2C%22propertyType%22%3A%5B%22flat%22%5D%2C%22maxRooms%22%3A1%2C%22minArea%22%3A30%2C%22energyClassification%22%3A%5B%22A%22%2C%22B%22%2C%22C%22%2C%22D%22%5D%2C%22page%22%3A1%2C%22sortBy%22%3A%22relevance%22%2C%22sortOrder%22%3A%22desc%22%2C%22onTheMarket%22%3A%5Btrue%5D%2C%22mapMode%22%3A%22enabled%22%2C%22limit%22%3A%22wvihHcx%7DL%3Fs%7BGl%60CZ%3F%7CyG%22%2C%22newProperty%22%3Afalse%2C%22blurInfoType%22%3A%5B%22disk%22%2C%22exact%22%5D%2C%22zoneIdsByTypes%22%3A%7B%22zoneIds%22%3A%5B%22-395443%22%5D%7D%7D&extensionType=extendedIfNoResult";
//        String filters = URLEncoder.encode("{\"size\":24,\"from\":0,\"showAllModels\":false,\"filterType\":\"buy\",\"propertyType\":[\"flat\"],\"maxRooms\":1,\"minArea\":30,\"energyClassification\":[\"A\",\"B\",\"C\",\"D\"],\"page\":1,\"sortBy\":\"relevance\",\"sortOrder\":\"desc\",\"onTheMarket\":[true],\"mapMode\":\"enabled\",\"limit\":\"wvihHcx}L?s{Gl`CZ?|yG\",\"newProperty\":false,\"blurInfoType\":[\"disk\",\"exact\"],\"zoneIdsByTypes\":{\"zoneIds\":[\"-395443\"]}}&extensionType=extendedIfNoResult", StandardCharsets.UTF_8);
//        String apiTestTest = "https://www.bienici.com/realEstateAds.json?filters={filters}";
//        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(headers);
//        ResponseEntity<String> responseEntity = restTemplate.exchange(apiTestTest, HttpMethod.GET, requestEntity, String.class);
//
//        return responseEntity.getBody();

        return scrapIt();
    }

    private String scrapIt() {
        String content = "";
        System.setProperty("webdriver.chrome.driver", "C:/Users/Sgio/Workspace/Tools/chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        try {
            driver.get("https://www.bienici.com/recherche/achat/chilly-mazarin-91380/appartement/studio?surface-min=25&classification-energetique=A%2CB%2CC%2CD");

            WebDriverWait waiter = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.resultsListContainer"))); // Attente jusqu'a ce que l'élément soit visible
            content = element.getText();

//            // Attendre que toutes les balises <div class="resultsListContainer"> soient visibles et les récupérer
//            List<WebElement> elements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div.resultsListContainer")));


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Fermez le navigateur
            driver.quit();
        }

        return content;
    }

    private Map<String, Object> getParameters(){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("enterprise", false);

        List<Integer> projects = new ArrayList<>();
        projects.add(1);
        parameters.put("projects", projects);

        Map<String, Integer> surface = new HashMap<>();
        surface.put("min", 30);
        surface.put("max", null); // null est une référence possible dans ce cas
        parameters.put("surface", surface);

        List<Integer> types = new ArrayList<>();
        types.add(2);
        types.add(1);
        parameters.put("types", types);

        List<Integer> rooms = new ArrayList<>();
        rooms.add(1);
        parameters.put("rooms", rooms);

        List<Map<String, List<Integer>>> places = new ArrayList<>();
        Map<String, List<Integer>> place = new HashMap<>();
        List<Integer> inseeCodes = new ArrayList<>();
        inseeCodes.add(910161);
        place.put("inseeCodes", inseeCodes);
        places.add(place);
        parameters.put("places", places);

        parameters.put("textCriteria", new ArrayList<String>());

        parameters.put("mandatoryCommodities", false);

        List<String> epc = new ArrayList<>();
        epc.add("A");
        epc.add("B");
        epc.add("C");
        epc.add("D");
        parameters.put("epc", epc);

        return parameters;
    }
}