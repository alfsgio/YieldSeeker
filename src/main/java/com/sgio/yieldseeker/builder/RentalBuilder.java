package com.sgio.yieldseeker.builder;

import com.google.gson.JsonObject;
import com.sgio.yieldseeker.model.Apartment;
import com.sgio.yieldseeker.model.Rental;

public class RentalBuilder {
    private Apartment apartment;
    private Float priceTotal;
    private Float priceCharges;
    private Boolean isFurnished;

    public RentalBuilder from(JsonObject jsonDatas) {
        this.apartment = new ApartmentBuilder().from(jsonDatas).build();
        this.priceTotal = jsonDatas.get("price").getAsFloat();
        this.priceCharges = jsonDatas.has("charges") ? jsonDatas.get("charges").getAsFloat() : 0;
        this.isFurnished = jsonDatas.has("isFurnished") && jsonDatas.get("isFurnished").getAsBoolean();
        return this;
    }

    public Rental build() {
        return Rental.builder()
                .apartment(apartment)
                .priceTotal(priceTotal)
                .priceCharges(priceCharges)
                .isFurnished(isFurnished)
                .build();
    }
}
