package com.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DSEnum {
    DRMS_NAM_UAT("getInstance_namuat_drms"),
    DRMS_NAM_DAILYREFRESH("getInstance_namdailyrefresh_drms"),
    DRMS_NAM_QA("getInstance_namqa_drms"),
    DRMS_APAC_UAT("getInstance_apacuat_drms"),
    DRMS_APAC_DAILYREFRESH("getInstance_apacdailyrefresh_drms"),
    DRMS_APAC_QA("getInstance_apacqa_drms"),
    DRMS_EMEA_UAT("getInstance_emeauat_drms"),
    DRMS_EMEA_DAILYREFRESH("getInstance_emeadailyrefresh_drms"),
    DRMS_EMEA_QA("getInstance_emeaqa_drms"),
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