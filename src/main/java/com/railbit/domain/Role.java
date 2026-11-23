package com.railbit.domain;

public enum Role {
    ADMIN, AGENT, CUSTOMER;

    /** Spring Security authority name, e.g. ROLE_ADMIN. */
    public String authority() {
        return "ROLE_" + name();
    }
}
