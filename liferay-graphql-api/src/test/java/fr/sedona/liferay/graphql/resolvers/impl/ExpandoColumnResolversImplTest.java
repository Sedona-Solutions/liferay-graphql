package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.expando.kernel.exception.NoSuchColumnException;
import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.service.ExpandoColumnLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import fr.sedona.liferay.graphql.loaders.ExpandoColumnBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ExpandoColumnResolvers;
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
 * Test suite for {@link ExpandoColumnResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class ExpandoColumnResolversImplTest {
    private static final long COLUMN_ID = 987L;
    private static final long TABLE_ID = 456L;
    private static final String NAME = "Column1";
    private static final int TYPE = 1;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, ExpandoColumn> dataLoader;

    @InjectMocks
    ExpandoColumnResolvers resolvers = new ExpandoColumnResolversImpl();

    @Mock
    private ExpandoColumnLocalService localService;

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
                .getDataLoader(ExpandoColumnBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((ExpandoColumnResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, boolean isValid) {
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("columnId")))
                    .thenReturn(COLUMN_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("tableId")))
                    .thenReturn(TABLE_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getIntArg(eq(environment), eq("type")))
                    .thenReturn(TYPE);
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
    public void getExpandoColumnsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoColumn> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoColumn entity = mock(ExpandoColumn.class);
                    entity.setColumnId(value);
                    availableObjects.add(entity);
                });
        List<ExpandoColumn> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoColumns(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoColumn> results = resolvers.getExpandoColumnsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoColumnsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<ExpandoColumn> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoColumn entity = mock(ExpandoColumn.class);
                    entity.setColumnId(value);
                    availableObjects.add(entity);
                });
        List<ExpandoColumn> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoColumns(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoColumn> results = resolvers.getExpandoColumnsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoColumnsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoColumn> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ExpandoColumn entity = mock(ExpandoColumn.class);
                    entity.setColumnId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoColumns(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoColumn> results = resolvers.getExpandoColumnsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoColumnsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ExpandoColumn> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getExpandoColumns(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ExpandoColumn> results = resolvers.getExpandoColumnsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getExpandoColumnDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        ExpandoColumn expectedResult = mock(ExpandoColumn.class);
        expectedResult.setColumnId(COLUMN_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("columnId"))
                .thenReturn(COLUMN_ID);
        when(dataLoader.load(COLUMN_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<ExpandoColumn> asyncResult = resolvers.getExpandoColumnDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ExpandoColumn result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getExpandoColumnDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("columnId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<ExpandoColumn> asyncResult = resolvers.getExpandoColumnDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getExpandoColumnDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("columnId"))
                .thenReturn(COLUMN_ID);
        when(dataLoader.load(COLUMN_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<ExpandoColumn> asyncResult = resolvers.getExpandoColumnDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ExpandoColumn result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createExpandoColumnDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("tableId", TABLE_ID);
        arguments.put("name", NAME);
        arguments.put("type", TYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoColumn expectedResult = mock(ExpandoColumn.class);
        expectedResult.setColumnId(COLUMN_ID);
        expectedResult.setTableId(TABLE_ID);
        expectedResult.setName(NAME);
        expectedResult.setType(TYPE);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addColumn(eq(TABLE_ID), eq(NAME), eq(TYPE), any()))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoColumn result = resolvers.createExpandoColumnDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createExpandoColumnDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addColumn(anyLong(), anyString(), anyInt(), any()))
                .thenThrow(PortalException.class);

        // Asserts
        ExpandoColumn result = resolvers.createExpandoColumnDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateExpandoColumnDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("columnId", COLUMN_ID);
        arguments.put("name", NAME);
        arguments.put("type", TYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoColumn expectedResult = mock(ExpandoColumn.class);
        expectedResult.setColumnId(COLUMN_ID);
        expectedResult.setName(NAME);
        expectedResult.setType(TYPE);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.updateColumn(eq(COLUMN_ID), eq(NAME), eq(TYPE), any()))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoColumn result = resolvers.updateExpandoColumnDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchColumnException.class)
    public void updateExpandoColumnDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", NAME);
        arguments.put("type", TYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("columnId")))
                .thenReturn(0L);
        when(localService.updateColumn(eq(0L), eq(NAME), eq(TYPE), any()))
                .thenThrow(NoSuchColumnException.class);

        // Asserts
        ExpandoColumn result = resolvers.updateExpandoColumnDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchColumnException.class)
    public void updateExpandoColumnDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("columnId", 789456L);
        arguments.put("name", NAME);
        arguments.put("type", TYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("columnId")))
                .thenReturn(789456L);
        when(localService.updateColumn(eq(789456L), eq(NAME), eq(TYPE), any()))
                .thenThrow(NoSuchColumnException.class);

        // Asserts
        ExpandoColumn result = resolvers.updateExpandoColumnDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateExpandoColumnDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("columnId")))
                .thenReturn(0L);
        when(localService.updateColumn(anyLong(), anyString(), anyInt(), any()))
                .thenThrow(PortalException.class);

        // Asserts
        ExpandoColumn result = resolvers.updateExpandoColumnDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteExpandoColumnDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("columnId", COLUMN_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoColumn expectedResult = mock(ExpandoColumn.class);
        expectedResult.setColumnId(COLUMN_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteExpandoColumn(eq(COLUMN_ID)))
                .thenReturn(expectedResult);

        // Asserts
        ExpandoColumn result = resolvers.deleteExpandoColumnDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchColumnException.class)
    public void deleteExpandoColumnDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        ExpandoColumn expectedResult = mock(ExpandoColumn.class);
        expectedResult.setColumnId(COLUMN_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteExpandoColumn(eq(COLUMN_ID)))
                .thenThrow(NoSuchColumnException.class);

        // Asserts
        ExpandoColumn result = resolvers.deleteExpandoColumnDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchColumnException.class)
    public void deleteExpandoColumnDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("columnId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ExpandoColumn expectedResult = mock(ExpandoColumn.class);
        expectedResult.setColumnId(COLUMN_ID);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("columnId")))
                .thenReturn(789456L);
        when(localService.deleteExpandoColumn(eq(789456L)))
                .thenThrow(NoSuchColumnException.class);

        // Asserts
        ExpandoColumn result = resolvers.deleteExpandoColumnDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
