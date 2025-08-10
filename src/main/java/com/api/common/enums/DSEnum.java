package com.api.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DSEnum {
    LIF_UAT("getInstance_postgresql_lif");

    private final String sqlSessionManager;

    public static DSEnum fromValue(String v) {
        for (DSEnum c : DSEnum.values()) {
            if (c.sqlSessionManager.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}