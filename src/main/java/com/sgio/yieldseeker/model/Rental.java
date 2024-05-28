package com.sgio.yieldseeker.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Rental extends Apartment {
    private Float priceTotal;
    private Float priceCharges;
    private Float priceEnergy;
}
