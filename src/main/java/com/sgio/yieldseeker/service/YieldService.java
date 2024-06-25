package com.sgio.yieldseeker.service;

import com.sgio.yieldseeker.model.Purchase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class YieldService {

    @Autowired
    ScrapService scrapService;

    public List<Purchase> yield(){
        return scrapService.scrapPurchases();
    }

    public String yieldUrl(){
        return scrapService.testScrapUrl();
    }
}