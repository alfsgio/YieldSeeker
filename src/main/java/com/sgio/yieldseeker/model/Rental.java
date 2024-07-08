package com.sgio.yieldseeker.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental {
    private String rentalAdLink;
    private Apartment apartment;
    private Float priceTotal;
    private Float priceCharges;
    private Float priceEnergy;
    private Boolean isFurnished;
}
