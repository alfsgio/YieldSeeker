package com.sgio.yieldseeker.model;

import com.sgio.yieldseeker.constant.SpaceType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExtraSpace {
    private boolean exist;
    private SpaceType spaceType;
}
