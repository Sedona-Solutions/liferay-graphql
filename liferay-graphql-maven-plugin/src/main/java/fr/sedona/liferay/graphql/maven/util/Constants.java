package fr.sedona.liferay.graphql.maven.util;

public class Constants {
    public static final String PROPS_LIFERAY_MODEL_IGNORED_ATTRIBUTES = "liferay.model.ignore.attributes";
    public static final String PROPS_LIFERAY_OVERWRITE_SCHEMA_FILE = "liferay.overwrite.schema.file";
    public static final String PROPS_LIFERAY_OVERWRITE_PREFIX = "liferay.overwrite.";
    public static final String PROPS_LIFERAY_SERVICE_PREFIX = "liferay.service.";
    public static final String PROPS_LIFERAY_MODEL_PREFIX = "liferay.model.";
    public static final String PROPS_LIFERAY_METHOD_CREATE_PREFIX = "liferay.method.create.";
    public static final String PROPS_LIFERAY_METHOD_READ_PREFIX = "liferay.method.read.";
    public static final String PROPS_LIFERAY_METHOD_READ_ALL_PREFIX = "liferay.method.read-all.";
    public static final String PROPS_LIFERAY_METHOD_UPDATE_PREFIX = "liferay.method.update.";
    public static final String PROPS_LIFERAY_METHOD_DELETE_PREFIX = "liferay.method.delete.";

    public static final String METHOD_GET = "get";
    public static final String METHOD_IS = "is";
    public static final String METHOD_ADD = "add";
    public static final String METHOD_UPDATE = "update";
    public static final String METHOD_DELETE = "delete";

    private Constants() {
        // Do nothing
    }
}
