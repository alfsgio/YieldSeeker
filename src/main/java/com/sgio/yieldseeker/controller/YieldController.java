package com.sgio.yieldseeker.controller;

import com.sgio.yieldseeker.service.YieldService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YieldController {

    YieldService yieldService = new YieldService();

    @RequestMapping("/scrap")
    public String getScrap(){
        return yieldService.scrapIt();
    }
}
