package fr.sedona.liferay.graphql.util;

public class Constants {
    public static final String MEDIA_TYPE_GRAPHQL = "application/graphql";
    public static final String ENDPOINT_SCHEMA = "/schema";
    public static final String ENDPOINT_API = "/api";

    public static final String SCHEMA_FILE = "/liferay-schema.graphql";

    public static final String PARAM_QUERY = "query";
    public static final String PARAM_OPERATION_NAME = "operationName";
    public static final String PARAM_VARIABLES = "variables";

    private Constants() {
        // Do nothing
    }
}
