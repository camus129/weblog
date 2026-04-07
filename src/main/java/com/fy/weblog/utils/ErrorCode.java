package com.fy.weblog.utils;

public class ErrorCode {
    public static final int SUCCESS = 0;
    public static final int USERNAME_OR_PASSWORD_ERROR = 1001;
    public static final int USER_NOT_EXIST = 1002;
    public static final int ACCOUNT_DISABLED = 1003;
    public static final int ACCOUNT_EXPIRED = 1004;
    public static final int ACCOUNT_LOCKED = 1005;
    public static final int CREDENTIAL_EXPIRED = 1006;
    public static final int ACCESS_DENIED = 1007;
    public static final int NO_PERMISSION = 1008;
    public static final int CREDENTIAL_INVALID_OR_EXPIRED = 1009;
    public static final int REFRESH_TOKEN_INVALID_OR_EXPIRED = 1010;
    public static final int INVALID_REQUEST = 1011;
    public static final int RATE_LIMIT_EXCEEDED = 1012;
    public static final int SYSTEM_ERROR = 1013;
}
