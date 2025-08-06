package com.api.helpers;

import java.util.*;

public class Constants {

    // Framework Constants
    public static String PATHPARAMS = "pathParams";

    public static String TESTCASE_BASEPATH = "com.api.testcases.";

    public static int TIMEOUT = 30 * 2 * 20;

    // Business Constants - Removed unused Elite and Booking service constants

    // Utils Constants
    public final static List<String> SERVICES_SSLIGNORE = new ArrayList<>(Arrays.asList(
            "eh_pre_trade_booking_service", "elite",
            "fund", "useradmin", "configui", "portal",
            "reportdashboard", "cfim_dataloader"
    ));

    public final static List<String> ENDPOINTS_SSLIGNORE = new ArrayList<>(Arrays.asList(
            "Test_bookingVanillas",
            "Test_globalbookingCitimlxml",
            "Test_globalbookingFxpgxml"
    ));
}