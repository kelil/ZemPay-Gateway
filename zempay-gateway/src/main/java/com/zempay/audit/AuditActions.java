package com.zempay.audit;

public final class AuditActions {

    private AuditActions() {
    }

    // Administrator actions
    public static final String ADMINISTRATOR_BOOTSTRAPPED =
            "ADMINISTRATOR_BOOTSTRAPPED";

    public static final String ADMINISTRATOR_CREATED =
            "ADMINISTRATOR_CREATED";

    public static final String ADMINISTRATOR_DISABLED =
            "ADMINISTRATOR_DISABLED";

    public static final String ADMINISTRATOR_ENABLED =
            "ADMINISTRATOR_ENABLED";

    // Merchant actions
    public static final String MERCHANT_APPROVED =
            "MERCHANT_APPROVED";

    public static final String MERCHANT_REJECTED =
            "MERCHANT_REJECTED";

    public static final String MERCHANT_SUSPENDED =
            "MERCHANT_SUSPENDED";

    public static final String MERCHANT_API_KEY_CREATED =
            "MERCHANT_API_KEY_CREATED";

    public static final String MERCHANT_API_KEY_REVOKED =
            "MERCHANT_API_KEY_REVOKED";
}