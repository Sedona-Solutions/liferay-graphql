package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.expando.kernel.exception.NoSuchValueException;
import com.liferay.expando.kernel.model.ExpandoValue;
import com.liferay.expando.kernel.service.ExpandoValueLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import fr.sedona.liferay.graphql.loaders.ExpandoValueBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ExpandoValueResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionContextBuilder;
import graphql.execution.ExecutionId;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import org.dataloader.DataLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link ExpandoValueResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class ExpandoValueResolversImplTest {
    private static final long VALUE_ID = 987L;
    private static final long COMPANY_ID = 456L;
    private static final String CLASS_NAME = "fr.sedona.Test";
    private static final String TABLE_NAME = "NewTable";
    private static final String COLUMN_NAME = "NewField";
    private static final long CLASS_PK = 123L;
    private static final String DATA_STRING = "Test data";
    private static final int DATA_INT = 10;
    private static final long DATA_LONG = 951L;
    private static final double DATA_DOUBLE = 41.0;
    private static final boolean DATA_BOOLEAN = true;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, ExpandoValue> dataLoader;

    @InjectMocks
    ExpandoValueResolvers resolvers = new ExpandoValueResolversImpl();

    @Mock
    private ExpandoValueLocalService localService;

    @Mock
    private GraphQLUtil graphQLUtil;

    @Before
    public void setUp() {
        executionId = ExecutionId.from("execution-1");
        executionContext = ExecutionContextBuilder.newExecutionContextBuilder()
                .executionId(executionId)
                .build();

        dataLoader = mock(DataLoader.class);
        mockEnvironment = mock(DataFetchingEnvironment.class);
        doReturn(dataLoader)
                .when(mockEnvironment)
                .getDataLoader(ExpandoValueBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((ExpandoValueResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, boolean isValid) {
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("valueId")))
                    .thenReturn(VALUE_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                    .thenReturn(COMPANY_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("className")))
                    .thenReturn(CLASS_NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("tableName")))
                    .thenReturn(TABLE_NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("columnName")))
                    .thenReturn(COLUMN_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getStringArg(eq(environment), eq("data")))
                    .thenReturn(DATA_STRING);
            when(graphQLUtil.getIntArg(eq(environment), eq("data")))
                    .thenReturn(DATA_INT);
            when(graphQLUtil.getLongArg(eq(environment), eq("data")))
                    .thenReturn(DATA_LONG);
            when(graphQLUtil.getDoubleArg(eq(environment), eq("data")))
                    .thenReturn(DATA_DOUBLE);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("data")))
                    .thenReturn(DATA_BOOLEAN);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getIntArg(eq(environment), anyString()))
                    .thenReturn(0);
            when(graphQLUtil.getDoubleArg(eq(environment), anyString()))
                    .thenReturn(0.0);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
                    .thenReturn("");
        }
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
    public void getExpandoValuesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoValue> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoValue entity = mock(ExpandoValue.class);
                    entity.setValueId(value);
                    availableObjects.add(entity);
                });
        List<ExpandoValue> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoValues(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoValue> results = resolvers.getExpandoValuesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoValuesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<ExpandoValue> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoValue entity = mock(ExpandoValue.class);
                    entity.setValueId(value);
                    availableObjects.add(entity);
                });
        List<ExpandoValue> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoValues(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoValue> results = resolvers.getExpandoValuesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoValuesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoValue> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoValue entity = mock(ExpandoValue.class);
                    entity.setValueId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoValues(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoValue> results = resolvers.getExpandoValuesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoValuesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoValue> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoValues(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoValue> results = resolvers.getExpandoValuesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoValueDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        ExpandoValue expectedResult = mock(ExpandoValue.class);
        expectedResult.setValueId(VALUE_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("valueId"))
                .thenReturn(VALUE_ID);
        when(dataLoader.load(VALUE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<ExpandoValue> asyncResult = resolvers.getExpandoValueDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ExpandoValue result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getExpandoValueDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("valueId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<ExpandoValue> asyncResult = resolvers.getExpandoValueDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getExpandoValueDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("valueId"))
                .thenReturn(VALUE_ID);
        when(dataLoader.load(VALUE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<ExpandoValue> asyncResult = resolvers.getExpandoValueDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ExpandoValue result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createExpandoValueForStringDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("tableName", TABLE_NAME);
        arguments.put("columnName", COLUMN_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("data", DATA_STRING);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoValue expectedResult = mock(ExpandoValue.class);
        expectedResult.setValueId(VALUE_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setData(DATA_STRING);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addValue(eq(COMPANY_ID), eq(CLASS_NAME), eq(TABLE_NAME), eq(COLUMN_NAME), eq(CLASS_PK), eq(DATA_STRING)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoValue result = resolvers.createExpandoValueForStringDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createExpandoValueForStringDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addValue(anyLong(), anyString(), anyString(), anyString(), anyLong(), anyString()))
                .thenThrow(PortalException.class);

        // Asserts
        ExpandoValue result = resolvers.createExpandoValueForStringDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void createExpandoValueForIntDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("tableName", TABLE_NAME);
        arguments.put("columnName", COLUMN_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("data", DATA_INT);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoValue expectedResult = mock(ExpandoValue.class);
        expectedResult.setValueId(VALUE_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setInteger(DATA_INT);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addValue(eq(COMPANY_ID), eq(CLASS_NAME), eq(TABLE_NAME), eq(COLUMN_NAME), eq(CLASS_PK), eq(DATA_INT)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoValue result = resolvers.createExpandoValueForIntDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createExpandoValueForIntDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addValue(anyLong(), anyString(), anyString(), anyString(), anyLong(), anyInt()))
                .thenThrow(PortalException.class);

        // Asserts
        ExpandoValue result = resolvers.createExpandoValueForIntDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void createExpandoValueForLongDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("tableName", TABLE_NAME);
        arguments.put("columnName", COLUMN_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("data", DATA_LONG);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoValue expectedResult = mock(ExpandoValue.class);
        expectedResult.setValueId(VALUE_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setLong(DATA_LONG);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addValue(eq(COMPANY_ID), eq(CLASS_NAME), eq(TABLE_NAME), eq(COLUMN_NAME), eq(CLASS_PK), eq(DATA_LONG)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoValue result = resolvers.createExpandoValueForLongDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createExpandoValueForLongDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addValue(anyLong(), anyString(), anyString(), anyString(), anyLong(), anyLong()))
                .thenThrow(PortalException.class);

        // Asserts
        ExpandoValue result = resolvers.createExpandoValueForLongDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void createExpandoValueForDoubleDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("tableName", TABLE_NAME);
        arguments.put("columnName", COLUMN_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("data", DATA_DOUBLE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoValue expectedResult = mock(ExpandoValue.class);
        expectedResult.setValueId(VALUE_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setDouble(DATA_DOUBLE);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addValue(eq(COMPANY_ID), eq(CLASS_NAME), eq(TABLE_NAME), eq(COLUMN_NAME), eq(CLASS_PK), eq(DATA_DOUBLE)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoValue result = resolvers.createExpandoValueForDoubleDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createExpandoValueForDoubleDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addValue(anyLong(), anyString(), anyString(), anyString(), anyLong(), anyDouble()))
                .thenThrow(PortalException.class);

        // Asserts
        ExpandoValue result = resolvers.createExpandoValueForDoubleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void createExpandoValueForBooleanDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("tableName", TABLE_NAME);
        arguments.put("columnName", COLUMN_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("data", DATA_BOOLEAN);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoValue expectedResult = mock(ExpandoValue.class);
        expectedResult.setValueId(VALUE_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setBoolean(DATA_BOOLEAN);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addValue(eq(COMPANY_ID), eq(CLASS_NAME), eq(TABLE_NAME), eq(COLUMN_NAME), eq(CLASS_PK), eq(DATA_BOOLEAN)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoValue result = resolvers.createExpandoValueForBooleanDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createExpandoValueForBooleanDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addValue(anyLong(), anyString(), anyString(), anyString(), anyLong(), anyBoolean()))
                .thenThrow(PortalException.class);

        // Asserts
        ExpandoValue result = resolvers.createExpandoValueForBooleanDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteExpandoValueDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("valueId", VALUE_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoValue expectedResult = mock(ExpandoValue.class);
        expectedResult.setValueId(VALUE_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteExpandoValue(eq(VALUE_ID)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoValue result = resolvers.deleteExpandoValueDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchValueException.class)
    public void deleteExpandoValueDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        ExpandoValue expectedResult = mock(ExpandoValue.class);
        expectedResult.setValueId(VALUE_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteExpandoValue(eq(VALUE_ID)))
                .thenThrow(NoSuchValueException.class);

        // Asserts
        ExpandoValue result = resolvers.deleteExpandoValueDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchValueException.class)
    public void deleteExpandoValueDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("valueId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoValue expectedResult = mock(ExpandoValue.class);
        expectedResult.setValueId(VALUE_ID);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("valueId")))
                .thenReturn(789456L);
        when(localService.deleteExpandoValue(eq(789456L)))
                .thenThrow(NoSuchValueException.class);

        // Asserts
        ExpandoValue result = resolvers.deleteExpandoValueDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
