package com.sgio.yieldseeker.builder;

import com.google.gson.JsonObject;
import com.sgio.yieldseeker.enumerations.Convenience;
import com.sgio.yieldseeker.enumerations.DPE;
import com.sgio.yieldseeker.enumerations.ExtraSpace;
import com.sgio.yieldseeker.enumerations.Heating;
import com.sgio.yieldseeker.model.Apartment;

import java.util.ArrayList;
import java.util.List;

public class ApartmentBuilder {
    private String city;
    private Integer postalCode;
    private Float surfaceArea;
    private DPE dpe;
    private Heating heating;
    private Boolean parking;
    private List<ExtraSpace> extraSpaces = new ArrayList<>();
    private List<Convenience> convenience = new ArrayList<>();

    public ApartmentBuilder from(JsonObject jsonDatas) {
        this.city = jsonDatas.get("city").getAsString();
        this.postalCode = jsonDatas.get("postalCode").getAsInt();
        this.surfaceArea = jsonDatas.get("surfaceArea").getAsFloat();
        this.dpe = DPE.valueOf(jsonDatas.get("energyClassification").getAsString());
        String heatingString = jsonDatas.has("heating") ? jsonDatas.get("heating").getAsString() : "";
        this.heating = heatingString.contains("collectif") ? Heating.valueOf("collectif") : Heating.valueOf("individuel");
        this.parking = (jsonDatas.has("parkingPlacesQuantity") && jsonDatas.get("parkingPlacesQuantity").getAsInt() > 0)
                || (jsonDatas.has("enclosedParkingQuantity") && jsonDatas.get("enclosedParkingQuantity").getAsInt() > 0);

        if ((jsonDatas.has("hasBalcony") && jsonDatas.get("hasBalcony").getAsBoolean())
                || (jsonDatas.has("balconyQuantity") && jsonDatas.get("balconyQuantity").getAsInt() > 0)) {
            this.extraSpaces.add(ExtraSpace.valueOf("balcon"));
        }
        if ((jsonDatas.has("hasTerrace") && jsonDatas.get("hasTerrace").getAsBoolean())
                || (jsonDatas.has("terracesQuantity") && jsonDatas.get("terracesQuantity").getAsInt() > 0)) {
            this.extraSpaces.add(ExtraSpace.valueOf("terrasse"));
        }
        if ((jsonDatas.has("hasCellar") && jsonDatas.get("hasCellar").getAsBoolean())
                || (jsonDatas.has("cellarsOrUndergroundsQuantity") && jsonDatas.get("cellarsOrUndergroundsQuantity").getAsInt() > 0)) {
            this.extraSpaces.add(ExtraSpace.valueOf("cave"));
        }

        if (jsonDatas.has("hasElevator") && jsonDatas.get("hasElevator").getAsBoolean()) {
            this.convenience.add(Convenience.valueOf("ascenseur"));
        }
        if (jsonDatas.has("hasIntercom") && jsonDatas.get("hasIntercom").getAsBoolean()) {
            this.convenience.add(Convenience.valueOf("interphone"));
        }
        if (jsonDatas.has("hasDoorCode") && jsonDatas.get("hasDoorCode").getAsBoolean()) {
            this.convenience.add(Convenience.valueOf("digicode"));
        }
        return this;
    }

    public Apartment build() {
        return Apartment.builder()
                .city(city)
                .postalCode(postalCode)
                .surfaceArea(surfaceArea)
                .dpe(dpe)
                .heating(heating)
                .parking(parking)
                .extraSpaces(extraSpaces)
                .convenience(convenience)
                .build();
    }
}
