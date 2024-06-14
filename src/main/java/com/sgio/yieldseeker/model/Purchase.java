package com.sgio.yieldseeker.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
    private String purchaseAdLink;
    private Apartment apartment;
    private Float price;
    private Float priceFees;
    private Float priceCharges;
    private Float priceNotary;
    private Boolean rented;
    private Boolean procedureInProgress;
    private Float propertyTax;
    private Float credit;
    private Float management;
}
