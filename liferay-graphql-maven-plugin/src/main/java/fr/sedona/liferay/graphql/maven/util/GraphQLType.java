package fr.sedona.liferay.graphql.maven.util;

import java.util.Arrays;

public enum GraphQLType {
    INT_PRIMITIVE(int.class, "Int"),
    LONG_PRIMITIVE(long.class, "Long"),
    DOUBLE_PRIMITIVE(double.class, "Float"),
    BOOLEAN_PRIMITIVE(boolean.class, "Boolean"),
    INT(Integer.class, "Int"),
    LONG(Long.class, "Long"),
    DOUBLE(Double.class, "Float"),
    BOOLEAN(Boolean.class, "Boolean"),
    STRING(String.class, "String"),
    INT_PRIMITIVE_ARRAY(int[].class, "[Int]"),
    LONG_PRIMITIVE_ARRAY(long[].class, "[Long]"),
    DOUBLE_PRIMITIVE_ARRAY(double[].class, "[Float]"),
    BOOLEAN_PRIMITIVE_ARRAY(boolean[].class, "[Boolean]"),
    INT_ARRAY(Integer[].class, "[Int]"),
    LONG_ARRAY(Long[].class, "[Long]"),
    DOUBLE_ARRAY(Double[].class, "[Float]"),
    BOOLEAN_ARRAY(Boolean[].class, "[Boolean]"),
    STRING_ARRAY(String[].class, "[String]");

    private Class correspondingClass;
    private String schemaType;

    public static GraphQLType fromClass(Class clazz) {
        return Arrays.stream(values())
                .filter(graphQLType -> graphQLType.getCorrespondingClass() == clazz)
                .findFirst()
                .orElse(null);
    }

    GraphQLType(Class correspondingClass, String schemaType) {
        this.correspondingClass = correspondingClass;
        this.schemaType = schemaType;
    }

    public Class getCorrespondingClass() {
        return correspondingClass;
    }

    public String getSchemaType() {
        return schemaType;
    }

    @Override
    public String toString() {
        return schemaType;
    }
}
