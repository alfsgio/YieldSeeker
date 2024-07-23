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
        this.apartment = parseApartment(jsonDatas);
        this.priceTotal = parsePriceTotal(jsonDatas);
        this.priceCharges = parsePriceCharges(jsonDatas);
        this.isFurnished = parseIsFurnished(jsonDatas);
        return this;
    }

    private Apartment parseApartment(JsonObject jsonDatas) {
        return new ApartmentBuilder().from(jsonDatas).build();
    }

    private Float parsePriceTotal(JsonObject jsonDatas) {
        return jsonDatas.get("price").getAsFloat();
    }

    private Float parsePriceCharges(JsonObject jsonDatas) {
        return jsonDatas.has("charges") ? jsonDatas.get("charges").getAsFloat() : 0;
    }

    private Boolean parseIsFurnished(JsonObject jsonDatas) {
        return jsonDatas.has("isFurnished") && jsonDatas.get("isFurnished").getAsBoolean();
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
