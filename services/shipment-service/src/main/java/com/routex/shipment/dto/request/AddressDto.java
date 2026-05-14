package com.routex.shipment.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record AddressDto(
    @NotBlank String addressLine1, String addressLine2,
    @NotBlank String city, @NotBlank String state,
    @NotBlank @Pattern(regexp="^[1-9][0-9]{5}$") String pincode,
    @NotBlank String country,
    BigDecimal latitude, BigDecimal longitude,
    @NotBlank String contactName, @NotBlank String contactPhone
) {}
