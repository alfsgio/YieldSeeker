package com.sgio.yieldseeker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class YieldService {

    @Autowired
    CollectorService collectorService;

    public Map<String, Map> yieldUrl(){
        return collectorService.collectAll();
    }
}