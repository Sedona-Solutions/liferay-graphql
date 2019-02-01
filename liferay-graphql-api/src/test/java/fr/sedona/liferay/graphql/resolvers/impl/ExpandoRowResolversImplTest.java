package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.expando.kernel.exception.NoSuchRowException;
import com.liferay.expando.kernel.model.ExpandoRow;
import com.liferay.expando.kernel.service.ExpandoRowLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import fr.sedona.liferay.graphql.loaders.ExpandoRowBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ExpandoRowResolvers;
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
 * Test suite for {@link ExpandoRowResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class ExpandoRowResolversImplTest {
    private static final long ROW_ID = 987L;
    private static final long TABLE_ID = 1L;
    private static final long CLASS_PK = 456L;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, ExpandoRow> dataLoader;

    @InjectMocks
    ExpandoRowResolvers resolvers = new ExpandoRowResolversImpl();

    @Mock
    private ExpandoRowLocalService localService;

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
                .getDataLoader(ExpandoRowBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((ExpandoRowResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, boolean isValid) {
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("rowId")))
                    .thenReturn(ROW_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("tableId")))
                    .thenReturn(TABLE_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
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
    public void getExpandoRowsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoRow> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoRow entity = mock(ExpandoRow.class);
                    entity.setRowId(value);
                    availableObjects.add(entity);
                });
        List<ExpandoRow> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoRows(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoRow> results = resolvers.getExpandoRowsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoRowsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<ExpandoRow> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoRow entity = mock(ExpandoRow.class);
                    entity.setRowId(value);
                    availableObjects.add(entity);
                });
        List<ExpandoRow> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoRows(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoRow> results = resolvers.getExpandoRowsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoRowsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoRow> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoRow entity = mock(ExpandoRow.class);
                    entity.setRowId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoRows(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoRow> results = resolvers.getExpandoRowsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoRowsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoRow> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoRows(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoRow> results = resolvers.getExpandoRowsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoRowDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        ExpandoRow expectedResult = mock(ExpandoRow.class);
        expectedResult.setRowId(ROW_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("rowId"))
                .thenReturn(ROW_ID);
        when(dataLoader.load(ROW_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<ExpandoRow> asyncResult = resolvers.getExpandoRowDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ExpandoRow result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getExpandoRowDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("rowId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<ExpandoRow> asyncResult = resolvers.getExpandoRowDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getExpandoRowDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("rowId"))
                .thenReturn(ROW_ID);
        when(dataLoader.load(ROW_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<ExpandoRow> asyncResult = resolvers.getExpandoRowDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ExpandoRow result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createExpandoRowDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("tableId", TABLE_ID);
        arguments.put("classPK", CLASS_PK);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoRow expectedResult = mock(ExpandoRow.class);
        expectedResult.setRowId(ROW_ID);
        expectedResult.setTableId(TABLE_ID);
        expectedResult.setClassPK(CLASS_PK);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addRow(eq(TABLE_ID), eq(CLASS_PK)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoRow result = resolvers.createExpandoRowDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createExpandoRowDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addRow(anyLong(), anyLong()))
                .thenThrow(PortalException.class);

        // Asserts
        ExpandoRow result = resolvers.createExpandoRowDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteExpandoRowDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("rowId", ROW_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoRow expectedResult = mock(ExpandoRow.class);
        expectedResult.setRowId(ROW_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteExpandoRow(eq(ROW_ID)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoRow result = resolvers.deleteExpandoRowDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchRowException.class)
    public void deleteExpandoRowDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        ExpandoRow expectedResult = mock(ExpandoRow.class);
        expectedResult.setRowId(ROW_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteExpandoRow(eq(ROW_ID)))
                .thenThrow(NoSuchRowException.class);

        // Asserts
        ExpandoRow result = resolvers.deleteExpandoRowDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchRowException.class)
    public void deleteExpandoRowDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("rowId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoRow expectedResult = mock(ExpandoRow.class);
        expectedResult.setRowId(ROW_ID);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("rowId")))
                .thenReturn(789456L);
        when(localService.deleteExpandoRow(eq(789456L)))
                .thenThrow(NoSuchRowException.class);

        // Asserts
        ExpandoRow result = resolvers.deleteExpandoRowDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
