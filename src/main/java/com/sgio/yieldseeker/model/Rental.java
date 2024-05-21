package com.sgio.yieldseeker.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Rental extends Apartment {
    private float priceTotal;
    private float priceCharges;
    private float priceEnergy;
}
