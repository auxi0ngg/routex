-- Shipment Service Schema
-- V1__init_shipment_schema.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE shipments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tracking_number VARCHAR(20) NOT NULL UNIQUE,
    organization_id UUID NOT NULL,
    customer_id UUID NOT NULL,

    -- Pickup address
    pickup_address_line1 VARCHAR(255),
    pickup_address_line2 VARCHAR(255),
    pickup_city VARCHAR(100),
    pickup_state VARCHAR(100),
    pickup_pincode VARCHAR(20),
    pickup_country VARCHAR(100),
    pickup_latitude NUMERIC(10,8),
    pickup_longitude NUMERIC(11,8),
    pickup_contact_name VARCHAR(100),
    pickup_contact_phone VARCHAR(20),

    -- Delivery address
    delivery_address_line1 VARCHAR(255),
    delivery_address_line2 VARCHAR(255),
    delivery_city VARCHAR(100),
    delivery_state VARCHAR(100),
    delivery_pincode VARCHAR(20),
    delivery_country VARCHAR(100),
    delivery_latitude NUMERIC(10,8),
    delivery_longitude NUMERIC(11,8),
    delivery_contact_name VARCHAR(100),
    delivery_contact_phone VARCHAR(20),

    -- Package details
    weight_kg NUMERIC(10,3),
    length_cm NUMERIC(8,2),
    width_cm NUMERIC(8,2),
    height_cm NUMERIC(8,2),
    package_type VARCHAR(30) NOT NULL DEFAULT 'PARCEL',
    package_description VARCHAR(500),
    declared_value NUMERIC(12,2),
    shipping_cost NUMERIC(12,2),

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',

    -- Assignments
    assigned_driver_id UUID,
    assigned_vehicle_id UUID,
    warehouse_id UUID,

    -- Scheduling
    scheduled_pickup_at TIMESTAMP WITH TIME ZONE,
    scheduled_delivery_at TIMESTAMP WITH TIME ZONE,
    actual_pickup_at TIMESTAMP WITH TIME ZONE,
    actual_delivery_at TIMESTAMP WITH TIME ZONE,
    estimated_delivery_at TIMESTAMP WITH TIME ZONE,

    -- Proof of delivery
    proof_of_delivery_image_url VARCHAR(1000),
    delivery_signature_url VARCHAR(500),
    delivery_notes VARCHAR(255),

    -- Flags
    fragile BOOLEAN NOT NULL DEFAULT FALSE,
    requires_signature BOOLEAN NOT NULL DEFAULT FALSE,
    cash_on_delivery BOOLEAN NOT NULL DEFAULT FALSE,
    cod_amount NUMERIC(12,2),
    priority VARCHAR(100),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_shipments_tracking_number ON shipments(tracking_number);
CREATE INDEX idx_shipments_status ON shipments(status);
CREATE INDEX idx_shipments_organization_id ON shipments(organization_id);
CREATE INDEX idx_shipments_customer_id ON shipments(customer_id);
CREATE INDEX idx_shipments_driver ON shipments(assigned_driver_id) WHERE assigned_driver_id IS NOT NULL;
CREATE INDEX idx_shipments_created_at ON shipments(created_at DESC);

-- GiST index for geospatial queries on pickup location
CREATE INDEX idx_shipments_pickup_geo ON shipments USING btree(pickup_latitude, pickup_longitude)
    WHERE pickup_latitude IS NOT NULL AND pickup_longitude IS NOT NULL;

CREATE TABLE shipment_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    remarks VARCHAR(500),
    latitude NUMERIC(10,8),
    longitude NUMERIC(11,8),
    location_name VARCHAR(255),
    updated_by_user_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_status_history_shipment ON shipment_status_history(shipment_id);
CREATE INDEX idx_status_history_created_at ON shipment_status_history(created_at DESC);
