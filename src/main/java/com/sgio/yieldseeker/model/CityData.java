package com.sgio.yieldseeker.model;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityData {

    @CsvBindByName(column = "INSEE")
    private Integer inseeCode;

    @CsvBindByName(column = "POSTAL")
    private Integer postalCode;

    @CsvBindByName(column = "CITY")
    private String city;

    @CsvBindByName(column = "PRICE")
    private Float priceBySquareMeter;

    @CsvBindByName(column = "TFB")
    private Float tauxFonciere;
}
