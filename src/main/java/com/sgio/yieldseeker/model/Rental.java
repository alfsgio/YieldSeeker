package com.sgio.yieldseeker.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental {
    private Apartment apartment;
    private Float priceTotal;
    private Float priceCharges;
    private Float priceEnergy;
    private Boolean isFurnished;
}
