package com.sgio.yieldseeker.model;

import com.sgio.yieldseeker.constant.Convenience;
import com.sgio.yieldseeker.constant.DPE;
import com.sgio.yieldseeker.constant.Heating;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Apartment {
    private String city;
    private String size;
    private DPE dpe;
    private Heating heating;
    private Parking parking;
    private ExtraSpace extraSpace;
    private Convenience convenience;
}