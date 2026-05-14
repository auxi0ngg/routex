package com.routex.shipment.mapper;

import com.routex.shipment.dto.request.AddressDto;
import com.routex.shipment.dto.response.ShipmentResponse;
import com.routex.shipment.entity.Address;
import com.routex.shipment.entity.Shipment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    @Mapping(target = "pickupCity", source = "pickupAddress.city")
    @Mapping(target = "pickupState", source = "pickupAddress.state")
    @Mapping(target = "deliveryCity", source = "deliveryAddress.city")
    @Mapping(target = "deliveryState", source = "deliveryAddress.state")
    ShipmentResponse toResponse(Shipment shipment);

    @Mapping(target = "latitude", source = "latitude")
    @Mapping(target = "longitude", source = "longitude")
    Address toAddress(AddressDto dto);
}
