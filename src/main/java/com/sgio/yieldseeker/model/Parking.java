package com.sgio.yieldseeker.model;

import com.sgio.yieldseeker.constant.ParkingType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Parking {
    private boolean exist;
    private ParkingType parkingType;
}
