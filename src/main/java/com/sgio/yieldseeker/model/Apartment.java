package com.sgio.yieldseeker.model;

import com.sgio.yieldseeker.enumerations.Convenience;
import com.sgio.yieldseeker.enumerations.DPE;
import com.sgio.yieldseeker.enumerations.Heating;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Apartment {
    private String city;
    private String size;
    private DPE dpe;
    private Heating heating;
    private Parking parking;
    private ExtraSpace extraSpace;
    private Convenience convenience;
}