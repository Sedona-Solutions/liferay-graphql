package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.expando.kernel.exception.NoSuchTableException;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.service.ExpandoTableLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import fr.sedona.liferay.graphql.loaders.ExpandoTableBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ExpandoTableResolvers;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link ExpandoTableResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class ExpandoTableResolversImplTest {
    private static final long TABLE_ID = 987L;
    private static final long COMPANY_ID = 456L;
    private static final String CLASS_NAME = "fr.sedona.Test";
    private static final String NAME = "NewTable";
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, ExpandoTable> dataLoader;

    @InjectMocks
    ExpandoTableResolvers resolvers = new ExpandoTableResolversImpl();

    @Mock
    private ExpandoTableLocalService localService;

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
                .getDataLoader(ExpandoTableBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((ExpandoTableResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, boolean isValid) {
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("tableId")))
                    .thenReturn(TABLE_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                    .thenReturn(COMPANY_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("className")))
                    .thenReturn(CLASS_NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
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
    public void getExpandoTablesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoTable> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoTable entity = mock(ExpandoTable.class);
                    entity.setTableId(value);
                    availableObjects.add(entity);
                });
        List<ExpandoTable> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoTables(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoTable> results = resolvers.getExpandoTablesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoTablesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<ExpandoTable> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoTable entity = mock(ExpandoTable.class);
                    entity.setTableId(value);
                    availableObjects.add(entity);
                });
        List<ExpandoTable> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoTables(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoTable> results = resolvers.getExpandoTablesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoTablesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoTable> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoTable entity = mock(ExpandoTable.class);
                    entity.setTableId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoTables(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoTable> results = resolvers.getExpandoTablesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoTablesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoTable> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoTables(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoTable> results = resolvers.getExpandoTablesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoTableDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        ExpandoTable expectedResult = mock(ExpandoTable.class);
        expectedResult.setTableId(TABLE_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("tableId"))
                .thenReturn(TABLE_ID);
        when(dataLoader.load(TABLE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<ExpandoTable> asyncResult = resolvers.getExpandoTableDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ExpandoTable result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getExpandoTableDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("tableId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<ExpandoTable> asyncResult = resolvers.getExpandoTableDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getExpandoTableDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("tableId"))
                .thenReturn(TABLE_ID);
        when(dataLoader.load(TABLE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<ExpandoTable> asyncResult = resolvers.getExpandoTableDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ExpandoTable result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createExpandoTableDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("name", NAME);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoTable expectedResult = mock(ExpandoTable.class);
        expectedResult.setTableId(TABLE_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setName(NAME);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addTable(eq(COMPANY_ID), eq(CLASS_NAME), eq(NAME)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoTable result = resolvers.createExpandoTableDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createExpandoTableDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addTable(anyLong(), anyString(), anyString()))
                .thenThrow(PortalException.class);

        // Asserts
        ExpandoTable result = resolvers.createExpandoTableDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateExpandoTableDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("tableId", TABLE_ID);
        arguments.put("name", NAME);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoTable expectedResult = mock(ExpandoTable.class);
        expectedResult.setTableId(TABLE_ID);
        expectedResult.setName(NAME);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.updateTable(eq(TABLE_ID), eq(NAME)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoTable result = resolvers.updateExpandoTableDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchTableException.class)
    public void updateExpandoTableDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", NAME);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("tableId")))
                .thenReturn(0L);
        when(localService.updateTable(eq(0L), eq(NAME)))
                .thenThrow(NoSuchTableException.class);

        // Asserts
        ExpandoTable result = resolvers.updateExpandoTableDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchTableException.class)
    public void updateExpandoTableDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("tableId", 789456L);
        arguments.put("name", NAME);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("tableId")))
                .thenReturn(789456L);
        when(localService.updateTable(eq(789456L), eq(NAME)))
                .thenThrow(NoSuchTableException.class);

        // Asserts
        ExpandoTable result = resolvers.updateExpandoTableDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateExpandoTableDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("tableId")))
                .thenReturn(TABLE_ID);
        when(localService.updateTable(anyLong(), anyString()))
                .thenThrow(PortalException.class);

        // Asserts
        ExpandoTable result = resolvers.updateExpandoTableDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteExpandoTableDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("tableId", TABLE_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoTable expectedResult = mock(ExpandoTable.class);
        expectedResult.setTableId(TABLE_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteExpandoTable(eq(TABLE_ID)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoTable result = resolvers.deleteExpandoTableDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchTableException.class)
    public void deleteExpandoTableDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        ExpandoTable expectedResult = mock(ExpandoTable.class);
        expectedResult.setTableId(TABLE_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteExpandoTable(eq(TABLE_ID)))
                .thenThrow(NoSuchTableException.class);

        // Asserts
        ExpandoTable result = resolvers.deleteExpandoTableDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchTableException.class)
    public void deleteExpandoTableDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("tableId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoTable expectedResult = mock(ExpandoTable.class);
        expectedResult.setTableId(TABLE_ID);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("tableId")))
                .thenReturn(789456L);
        when(localService.deleteExpandoTable(eq(789456L)))
                .thenThrow(NoSuchTableException.class);

        // Asserts
        ExpandoTable result = resolvers.deleteExpandoTableDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
