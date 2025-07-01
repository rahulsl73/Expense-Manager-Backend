package com.example.expensemanager.constant;

public class Constants {
    public static final String AUTH_BASE = "/auth";
    public static final String SIGNUP = "/signup";
    public static final String LOGIN = "/login";

    public static final String EXPENSE_BASE = "/expenses";
    public static final String STATS_SUMMARY = "/stats/summary";
    public static final String STATS_CATEGORY = "/stats/category";
    public static final String STATS_TOP = "/stats/top";
    public static final String STATS_TIMESERIES = "/stats/timeseries";
    public static final String EXPORT = "/export";
    public static final String CONVERT = "/convert";

    public static final String SETTINGS_BASE = "/settings";

    public static final String USER_BASE = "/users";
    public static final String PROFILE = "/profile";

    public static final String HEADER_USER_ID = "User-Id";

    public static final String CSV_CONTENT_TYPE = "text/csv";
    public static final String CSV_DISPOSITION = "attachment; filename=expenses.csv";

    public static final String ERR_USER_NOT_FOUND = "User not found";

    public static final String DATE_PATTERN_ISO = "yyyy-MM-dd";


    public static final String COOKIE_NAME    = "X-Auth-Token";
    public static final long   COOKIE_MAX_AGE = 60 * 60 * 24; 
}
