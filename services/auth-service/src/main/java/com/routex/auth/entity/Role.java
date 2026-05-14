package com.routex.auth.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public enum Role {
    SUPER_ADMIN,
    COMPANY_ADMIN,
    FLEET_MANAGER,
    WAREHOUSE_MANAGER,
    DRIVER,
    CUSTOMER;

    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }
}
