package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import fr.sedona.liferay.graphql.loaders.ClassNameBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ClassNameResolvers;
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
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link ClassNameResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class ClassNameResolversImplTest {
    private static final long CLASS_NAME_ID = 987L;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, ClassName> dataLoader;

    @InjectMocks
    ClassNameResolvers resolvers = new ClassNameResolversImpl();

    @Mock
    private ClassNameLocalService localService;

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
                .getDataLoader(ClassNameBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((ClassNameResolversImpl) resolvers).setUtil(new GraphQLUtil());
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
    public void getClassNamesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ClassName> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ClassName entity = mock(ClassName.class);
                    entity.setClassNameId(value);
                    availableObjects.add(entity);
                });
        List<ClassName> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getClassNames(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<ClassName> results = resolvers.getClassNamesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getClassNamesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<ClassName> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ClassName entity = mock(ClassName.class);
                    entity.setClassNameId(value);
                    availableObjects.add(entity);
                });
        List<ClassName> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getClassNames(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<ClassName> results = resolvers.getClassNamesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getClassNamesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ClassName> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    ClassName entity = mock(ClassName.class);
                    entity.setClassNameId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getClassNames(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ClassName> results = resolvers.getClassNamesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getClassNamesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<ClassName> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getClassNames(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<ClassName> results = resolvers.getClassNamesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getClassNameDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        ClassName expectedResult = mock(ClassName.class);
        expectedResult.setClassNameId(CLASS_NAME_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("classNameId"))
                .thenReturn(CLASS_NAME_ID);
        when(dataLoader.load(CLASS_NAME_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<ClassName> asyncResult = resolvers.getClassNameDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ClassName result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getClassNameDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("classNameId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<ClassName> asyncResult = resolvers.getClassNameDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getClassNameDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("classNameId"))
                .thenReturn(CLASS_NAME_ID);
        when(dataLoader.load(CLASS_NAME_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<ClassName> asyncResult = resolvers.getClassNameDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        ClassName result = asyncResult.get();
        assertNull(result);
    }
}
