package com.sgio.yieldseeker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CityStats {
    private Float minPrice;
    private Float avgPrice;
    private Float maxPrice;
}
