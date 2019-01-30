package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchListTypeException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ListType;
import com.liferay.portal.kernel.service.ListTypeLocalService;
import fr.sedona.liferay.graphql.loaders.ListTypeBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ListTypeResolvers;
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
 * Test suite for {@link ListTypeResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class ListTypeResolversImplTest {
    private static final long LIST_TYPE_ID = 987L;
    private static final String NAME = "Test";
    private static final String TYPE = "fr.sedona.Test.type";
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, ListType> dataLoader;

    @InjectMocks
    ListTypeResolvers resolvers = new ListTypeResolversImpl();

    @Mock
    private ListTypeLocalService localService;

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
                .getDataLoader(ListTypeBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((ListTypeResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, boolean isValid) {
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("listTypeId")))
                    .thenReturn(LIST_TYPE_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("type")))
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
    public void getListTypesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ListType> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ListType entity = mock(ListType.class);
                    entity.setListTypeId(value);
                    availableObjects.add(entity);
                });
        List<ListType> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getListTypes(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<ListType> results = resolvers.getListTypesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getListTypesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<ListType> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ListType entity = mock(ListType.class);
                    entity.setListTypeId(value);
                    availableObjects.add(entity);
                });
        List<ListType> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getListTypes(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<ListType> results = resolvers.getListTypesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getListTypesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ListType> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ListType entity = mock(ListType.class);
                    entity.setListTypeId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getListTypes(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ListType> results = resolvers.getListTypesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getListTypesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ListType> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getListTypes(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ListType> results = resolvers.getListTypesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getListTypeDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        ListType expectedResult = mock(ListType.class);
        expectedResult.setListTypeId(LIST_TYPE_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("listTypeId"))
                .thenReturn(LIST_TYPE_ID);
        when(dataLoader.load(LIST_TYPE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<ListType> asyncResult = resolvers.getListTypeDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ListType result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getListTypeDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("listTypeId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<ListType> asyncResult = resolvers.getListTypeDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getListTypeDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("listTypeId"))
                .thenReturn(LIST_TYPE_ID);
        when(dataLoader.load(LIST_TYPE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<ListType> asyncResult = resolvers.getListTypeDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ListType result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createListTypeDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", NAME);
        arguments.put("type", TYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ListType expectedResult = mock(ListType.class);
        expectedResult.setName(NAME);
        expectedResult.setType(TYPE);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addListType(eq(NAME), eq(TYPE)))
                .thenReturn(expectedResult);

        // Asserts
        ListType result = resolvers.createListTypeDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createListTypeDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addListType(anyString(), anyString()))
                .thenThrow(PortalException.class);

        // Asserts
        ListType result = resolvers.createListTypeDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteListTypeDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("listTypeId", LIST_TYPE_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ListType expectedResult = mock(ListType.class);
        expectedResult.setListTypeId(LIST_TYPE_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteListType(eq(LIST_TYPE_ID)))
                .thenReturn(expectedResult);

        // Asserts
        ListType result = resolvers.deleteListTypeDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchListTypeException.class)
    public void deleteListTypeDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        ListType expectedResult = mock(ListType.class);
        expectedResult.setListTypeId(LIST_TYPE_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteListType(eq(LIST_TYPE_ID)))
                .thenThrow(NoSuchListTypeException.class);

        // Asserts
        ListType result = resolvers.deleteListTypeDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchListTypeException.class)
    public void deleteListTypeDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("listTypeId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        ListType expectedResult = mock(ListType.class);
        expectedResult.setListTypeId(LIST_TYPE_ID);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("listTypeId")))
                .thenReturn(789456L);
        when(localService.deleteListType(eq(789456L)))
                .thenThrow(NoSuchListTypeException.class);

        // Asserts
        ListType result = resolvers.deleteListTypeDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
