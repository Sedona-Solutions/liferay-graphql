package fr.sedona.liferay.graphql.util;

import com.liferay.portal.kernel.util.LocaleUtil;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionContextBuilder;
import graphql.execution.ExecutionId;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Test suite for {@link GraphQLUtil}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
        LocaleUtil.class
)
public class GraphQLUtilTest {
    private String argumentName;
    private String stringArgValue;
    private String stringArgDefaultValue;
    private int intArgValue;
    private int intArgDefaultValue;
    private long longArgValue;
    private long longArgDefaultValue;
    private double doubleArgValue;
    private double doubleArgDefaultValue;
    private boolean booleanArgValue;
    private boolean booleanArgDefaultValue;
    private String[] stringArrayValue;
    private long[] longArrayValue;
    private ExecutionId executionId;
    private ExecutionContext executionContext;

    @InjectMocks
    GraphQLUtil graphQLUtil = new GraphQLUtil();

    @Before
    public void setUp() {
        argumentName = "testName";
        stringArgValue = "some value";
        stringArgDefaultValue = "default value";
        intArgValue = 12345;
        intArgDefaultValue = 99;
        longArgValue = 12345L;
        longArgDefaultValue = 99L;
        doubleArgValue = 12345.0;
        doubleArgDefaultValue = 99.0;
        booleanArgValue = true;
        booleanArgDefaultValue = true;
        stringArrayValue = new String[]{"string 1", "string 2"};
        longArrayValue = new long[]{1L, 2L, 3L};

        executionId = ExecutionId.from("execution-1");
        executionContext = ExecutionContextBuilder.newExecutionContextBuilder()
                .executionId(executionId)
                .build();
    }

    private DataFetchingEnvironment getTestEnvironment(Map<String, Object> arguments) {
        return new DataFetchingEnvironmentImpl(
                null,
                arguments,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                executionId,
                null,
                null,
                executionContext);
    }

    @Test
    public void getStringArg_no_default_value_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, stringArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        String value = graphQLUtil.getStringArg(environment, argumentName);
        assertNotNull(value);
        assertEquals(stringArgValue, value);
    }

    @Test
    public void getStringArg_no_default_value_and_no_args_should_return_default_value() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        String value = graphQLUtil.getStringArg(environment, argumentName);
        assertNotNull(value);
        assertEquals("", value);
    }

    @Test
    public void getStringArg_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, stringArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        String value = graphQLUtil.getStringArg(environment, argumentName, stringArgDefaultValue);
        assertNotNull(value);
        assertEquals(stringArgValue, value);
    }

    @Test
    public void getStringArg_no_args_should_return_default_value() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        String value = graphQLUtil.getStringArg(environment, argumentName, stringArgDefaultValue);
        assertNotNull(value);
        assertEquals(stringArgDefaultValue, value);
    }

    @Test
    public void getIntArg_no_default_value_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, intArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        int value = graphQLUtil.getIntArg(environment, argumentName);
        assertEquals(intArgValue, value);
    }

    @Test
    public void getIntArg_no_default_value_and_no_args_should_return_default_value() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        int value = graphQLUtil.getIntArg(environment, argumentName);
        assertEquals(0, value);
    }

    @Test
    public void getIntArg_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, intArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        int value = graphQLUtil.getIntArg(environment, argumentName, intArgDefaultValue);
        assertEquals(intArgValue, value);
    }

    @Test
    public void getIntArg_no_args_should_return_default_value() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        int value = graphQLUtil.getIntArg(environment, argumentName, intArgDefaultValue);
        assertEquals(intArgDefaultValue, value);
    }

    @Test
    public void getLongArg_no_default_value_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, longArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        long value = graphQLUtil.getLongArg(environment, argumentName);
        assertEquals(longArgValue, value);
    }

    @Test
    public void getLongArg_no_default_value_and_no_args_should_return_default_value() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        long value = graphQLUtil.getLongArg(environment, argumentName);
        assertEquals(0, value);
    }

    @Test
    public void getLongArg_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, longArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        long value = graphQLUtil.getLongArg(environment, argumentName, longArgDefaultValue);
        assertEquals(longArgValue, value);
    }

    @Test
    public void getLongArg_no_args_should_return_default_value() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        long value = graphQLUtil.getLongArg(environment, argumentName, longArgDefaultValue);
        assertEquals(longArgDefaultValue, value);
    }

    @Test
    public void getDoubleArg_no_default_value_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, doubleArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        double value = graphQLUtil.getDoubleArg(environment, argumentName);
        assertEquals(doubleArgValue, value, 0.1);
    }

    @Test
    public void getDoubleArg_no_default_value_and_no_args_should_return_default_value() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        double value = graphQLUtil.getDoubleArg(environment, argumentName);
        assertEquals(0.0, value, 0.1);
    }

    @Test
    public void getDoubleArg_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, doubleArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        double value = graphQLUtil.getDoubleArg(environment, argumentName, doubleArgDefaultValue);
        assertEquals(doubleArgValue, value, 0.1);
    }

    @Test
    public void getDoubleArg_no_args_should_return_default_value() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        double value = graphQLUtil.getDoubleArg(environment, argumentName, doubleArgDefaultValue);
        assertEquals(doubleArgDefaultValue, value, 0.1);
    }

    @Test
    public void getBooleanArg_no_default_value_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, booleanArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        boolean value = graphQLUtil.getBooleanArg(environment, argumentName);
        assertEquals(booleanArgValue, value);
    }

    @Test
    public void getBooleanArg_no_default_value_and_no_args_should_return_default_value() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        boolean value = graphQLUtil.getBooleanArg(environment, argumentName);
        assertFalse(value);
    }

    @Test
    public void getBooleanArg_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, booleanArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        boolean value = graphQLUtil.getBooleanArg(environment, argumentName, booleanArgDefaultValue);
        assertEquals(booleanArgValue, value);
    }

    @Test
    public void getBooleanArg_no_args_should_return_default_value() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        boolean value = graphQLUtil.getBooleanArg(environment, argumentName, booleanArgDefaultValue);
        assertEquals(booleanArgDefaultValue, value);
    }

    @Test
    public void getStringArrayArg_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, stringArrayValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        String[] values = graphQLUtil.getStringArrayArg(environment, argumentName);
        assertNotNull(values);
        assertArrayEquals(stringArrayValue, values);
    }

    @Test
    public void getStringArrayArg_no_args_should_return_empty_array() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        String[] values = graphQLUtil.getStringArrayArg(environment, argumentName);
        assertNotNull(values);
        assertArrayEquals(new String[0], values);
    }

    @Test
    public void getLongArrayArg_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, longArrayValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        long[] values = graphQLUtil.getLongArrayArg(environment, argumentName);
        assertNotNull(values);
        assertArrayEquals(longArrayValue, values);
    }

    @Test
    public void getLongArrayArg_no_args_should_return_empty_array() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        long[] values = graphQLUtil.getLongArrayArg(environment, argumentName);
        assertNotNull(values);
        assertArrayEquals(new long[0], values);
    }

    @Test
    public void getTranslatedArg_should_return_passed_value() {
        // Given
        Map<String, String> translatedArgValue = new HashMap<>();
        translatedArgValue.put("en_US", "Test title");
        translatedArgValue.put("fr_FR", "Titre de test");
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, translatedArgValue);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        PowerMockito.mockStatic(LocaleUtil.class);
        when(LocaleUtil.fromLanguageId("en_US"))
                .thenReturn(LocaleUtil.US);
        when(LocaleUtil.fromLanguageId("fr_FR"))
                .thenReturn(LocaleUtil.FRANCE);

        // Asserts
        Map<Locale, String> value = graphQLUtil.getTranslatedArg(environment, argumentName);
        assertNotNull(value);
        Map<Locale, String> translatedArgValueMapped = new HashMap<>();
        translatedArgValueMapped.put(LocaleUtil.fromLanguageId("en_US"), "Test title");
        translatedArgValueMapped.put(LocaleUtil.fromLanguageId("fr_FR"), "Titre de test");
        assertEquals(translatedArgValueMapped, value);
    }

    @Test
    public void getTranslatedArg_no_args_should_return_empty_map() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        Map<Locale, String> value = graphQLUtil.getTranslatedArg(environment, argumentName);
        assertEquals(Collections.emptyMap(), value);
    }

    @Test
    public void getLocaleArg_should_return_passed_value() {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, "fr_FR");
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        PowerMockito.mockStatic(LocaleUtil.class);
        when(LocaleUtil.fromLanguageId("fr_FR"))
                .thenReturn(LocaleUtil.FRANCE);

        // Asserts
        Locale value = graphQLUtil.getLocaleArg(environment, argumentName);
        assertNotNull(value);
        assertEquals(LocaleUtil.fromLanguageId("fr_FR"), value);
    }

    @Test
    public void getLocaleArg_no_args_should_return_default_locale() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        Locale value = graphQLUtil.getLocaleArg(environment, argumentName);
        assertNotNull(value);
        assertEquals(LocaleUtil.getDefault(), value);
    }

    @Test
    public void getDateArg_should_return_passed_value() {
        // Given
        Date now = new Date();
        Map<String, Object> arguments = new HashMap<>();
        arguments.put(argumentName, now);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        // Nothing

        // Asserts
        Date value = graphQLUtil.getDateArg(environment, argumentName);
        assertNotNull(value);
        assertEquals(now, value);
    }

    @Test
    public void getDateArg_no_args_should_return_null() {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        // Nothing

        // Asserts
        Date value = graphQLUtil.getDateArg(environment, argumentName);
        assertNull(value);
    }
}
