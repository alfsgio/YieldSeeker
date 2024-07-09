package com.sgio.yieldseeker.controller;

import com.sgio.yieldseeker.service.CalculatorService;
import com.sgio.yieldseeker.service.YieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class YieldController {

    @Autowired
    YieldService yieldService;

    @Autowired
    CalculatorService calculatorService;

    @RequestMapping("/yield")
    public Map<String, Map> getYield(){
        return yieldService.yield();
    }


    @RequestMapping("/monthly/{amount}")
    public Double getMonthly(@PathVariable Integer amount){
        return calculatorService.calculateMonthly(amount);
    }

}
