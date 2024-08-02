package com.sgio.yieldseeker.controller;

import com.sgio.yieldseeker.model.PurchaseStats;
import com.sgio.yieldseeker.model.Rental;
import com.sgio.yieldseeker.service.CalculatorService;
import com.sgio.yieldseeker.service.YieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class YieldController {

    @Autowired
    YieldService yieldService;

    @Autowired
    CalculatorService calculatorService;

    @RequestMapping("/yield")
    public Map<Integer, List<PurchaseStats>> getYield(){ return yieldService.getYield(); }

    @RequestMapping("/rentals/{citycode}")
    public List<Rental> getRentalsFromCityCode(@PathVariable Integer citycode){ return yieldService.getRentalsFromCityCode(citycode); }

    @RequestMapping("/monthly/{amount}")
    public Float getMonthly(@PathVariable Float amount){
        return calculatorService.calculateLoanMonthly(amount);
    }
}