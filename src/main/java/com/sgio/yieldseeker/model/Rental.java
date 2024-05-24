package com.sgio.yieldseeker.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Rental extends Apartment {
    private float priceTotal;
    private float priceCharges;
    private float priceEnergy;
}
