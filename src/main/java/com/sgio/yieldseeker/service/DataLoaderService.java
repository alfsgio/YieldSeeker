package com.sgio.yieldseeker.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.sgio.yieldseeker.model.CityData;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Getter
@Service
public class DataLoaderService {

    private List<CityData> cities = new ArrayList<>();

    /**
     * This service loads the city datas from the 'price_by_square_meter.csv' file.
     */
    public void loadCitiesMap() {
        final InputStream resource = getClass().getClassLoader().getResourceAsStream("specific_resources/price_by_square_meter.csv");
        if (resource != null) {
            try (Reader reader = new InputStreamReader(resource)) {
                cities = new CsvToBeanBuilder<CityData>(reader)
                        .withType(CityData.class)
                        .build()
                        .parse();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
