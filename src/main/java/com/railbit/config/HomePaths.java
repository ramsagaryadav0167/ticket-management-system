package com.railbit.config;

import com.railbit.domain.Role;

public final class HomePaths {

    private HomePaths() {
    }

    public static String of(Role role) {
        return switch (role) {
            case ADMIN -> "/admin/dashboard";
            case AGENT -> "/agent/dashboard";
            case CUSTOMER -> "/customer/dashboard";
        };
    }
}
