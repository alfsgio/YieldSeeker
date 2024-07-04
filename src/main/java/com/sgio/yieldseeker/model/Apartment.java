package com.sgio.yieldseeker.model;

import com.sgio.yieldseeker.enumerations.Convenience;
import com.sgio.yieldseeker.enumerations.DPE;
import com.sgio.yieldseeker.enumerations.ExtraSpace;
import com.sgio.yieldseeker.enumerations.Heating;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Apartment {
    private String city;
    private Integer postalCode;
    private Float size;
    private DPE dpe;
    private Heating heating;
    private Boolean parking;
    private List<ExtraSpace> extraSpaces;
    private List<Convenience> convenience;

    public Apartment() {
        this.extraSpaces = new ArrayList<>();
        this.convenience = new ArrayList<>();
    }

    public void addConvenience(Convenience convenience) {
        this.convenience.add(convenience);
    }

    public void addExtraSpaces(ExtraSpace extraSpaces) {
        this.extraSpaces.add(extraSpaces);
    }
}