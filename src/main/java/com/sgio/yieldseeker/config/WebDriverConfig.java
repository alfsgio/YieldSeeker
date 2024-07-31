package com.sgio.yieldseeker.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.time.Duration;

@Configuration
public class WebDriverConfig {

    private WebDriver webDriver;

    @Bean
    public WebDriver webDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new", "--disable-gpu");
        this.webDriver = new ChromeDriver(chromeOptions);
        return this.webDriver;
    }

    @Bean
    public WebDriverWait webDriverWait() {
        return new WebDriverWait(webDriver(), Duration.ofSeconds(10));
    }

    @PreDestroy
    public void quitWebDriver() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }
}