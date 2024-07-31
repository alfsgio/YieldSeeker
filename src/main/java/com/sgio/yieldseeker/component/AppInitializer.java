package com.sgio.yieldseeker.component;

import com.sgio.yieldseeker.service.DataLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppInitializer implements ApplicationRunner {

    @Autowired
    private DataLoaderService dataLoaderService;

    @Override
    public void run(ApplicationArguments args){
        dataLoaderService.loadCitiesMap();
    }
}
