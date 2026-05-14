package com.routex.fleet.dto;
public record FleetUtilizationStats(long total, long inUse, long available, long maintenance, double utilizationPercent) {}
