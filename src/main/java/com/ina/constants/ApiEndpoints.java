package com.ina.constants;

public class ApiEndpoints {

    private ApiEndpoints(){}

    public static final String SERVER_CONTEXT_FOR_TMS = "tms";
    public static final String SERVER_CONTEXT_FOR_KEYS = "key";
    public static final String API = "/api";
    public static final String VERSION = "/v1";
    public static final String DEVICE_SETUP = "/device/setup";
    public static final String DEVICE_CERTS = "/device/certs";

    public static final String GENERATE_ROOT_CERT = "/generateRootCert";
    public static final String GENERATE_SERVER_CERTS = "/generateServerCerts";
    public static final String GENERATE_SERVER_L4_CERTS = "/generateServerL4Certs";
    public static final String GET_ALL_SERVER_CERTS = "/getAllServerCerts";
    public static final String GET_ALL_DEVICE_CERTS = "/getAllDeviceCerts";
    public static final String GET_SERVER_CERTS_WITH_STATUS = "/getServerCertsWithStatus";
    public static final String VIEW_CERT_INFO = "/viewCertInfo";


    public static final String SETUP_TMS_INIT = "/tmsInit";
    public static final String GENERATE_DEK_AND_AEK_KEY = "/generateAEKKey";
    public static final String GET_ALL_AEK_DEK_SERVER_KEYS = "/getAllAEKAndDEKServerKeys";
    public static final String ROTATE_DEK_AND_AEK_KEY = "/rotateDEKAndAEKKey";

    public static final String GENERATE_SPOS_AUTH_KEY = "/generateSPOSAuthKey";
    public static final String GET_ALL_SPOS_AUTH_KEYS = "/getAllSPOSKeys";
    public static final String DEVICE_PROFILE="/device";
    public static final String PARAMETERS="/parameters";

    public static final String UPDATE_DEVICE_REVOKE_STATUS="/updateDeviceRevokeStatus";
    public static final String RELEASE = SERVER_CONTEXT_FOR_TMS + API + VERSION + "/release";
    public static final String CREATE="/create";

}
