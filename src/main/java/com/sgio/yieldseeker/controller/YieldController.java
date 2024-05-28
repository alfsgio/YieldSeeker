package com.sgio.yieldseeker.controller;

import com.sgio.yieldseeker.model.Purchase;
import com.sgio.yieldseeker.service.YieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class YieldController {

    @Autowired
    YieldService yieldService;

    @RequestMapping("/scrap")
    public List<Purchase> getScrap(){
        return yieldService.startScrapper();
    }
}
