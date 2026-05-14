package com.routex.shipment.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.math.BigDecimal;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String contactName;
    private String contactPhone;
}
