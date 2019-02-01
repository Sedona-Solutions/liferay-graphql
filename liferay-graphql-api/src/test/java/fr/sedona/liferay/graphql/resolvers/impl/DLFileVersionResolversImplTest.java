package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.model.DLFileVersion;
import com.liferay.document.library.kernel.service.DLFileVersionLocalService;
import fr.sedona.liferay.graphql.loaders.DLFileVersionBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFileVersionResolvers;
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
 * Test suite for {@link DLFileVersionResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class DLFileVersionResolversImplTest {
    private static final long FILE_VERSION_ID = 987L;

    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, DLFileVersion> dataLoader;

    @InjectMocks
    DLFileVersionResolvers resolvers = new DLFileVersionResolversImpl();

    @Mock
    private DLFileVersionLocalService localService;

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
                .getDataLoader(DLFileVersionBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((DLFileVersionResolversImpl) resolvers).setUtil(new GraphQLUtil());
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
    public void getDLFileVersionsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileVersion> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileVersion entity = mock(DLFileVersion.class);
                    entity.setFileVersionId(value);
                    availableObjects.add(entity);
                });
        List<DLFileVersion> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileVersions(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileVersion> results = resolvers.getDLFileVersionsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileVersionsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<DLFileVersion> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileVersion entity = mock(DLFileVersion.class);
                    entity.setFileVersionId(value);
                    availableObjects.add(entity);
                });
        List<DLFileVersion> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileVersions(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileVersion> results = resolvers.getDLFileVersionsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileVersionsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileVersion> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileVersion entity = mock(DLFileVersion.class);
                    entity.setFileVersionId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileVersions(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileVersion> results = resolvers.getDLFileVersionsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileVersionsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileVersion> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileVersions(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileVersion> results = resolvers.getDLFileVersionsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileVersionDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        DLFileVersion expectedResult = mock(DLFileVersion.class);
        expectedResult.setFileVersionId(FILE_VERSION_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileVersionId"))
                .thenReturn(FILE_VERSION_ID);
        when(dataLoader.load(FILE_VERSION_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<DLFileVersion> asyncResult = resolvers.getDLFileVersionDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFileVersion result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getDLFileVersionDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileVersionId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<DLFileVersion> asyncResult = resolvers.getDLFileVersionDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getDLFileVersionDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileVersionId"))
                .thenReturn(FILE_VERSION_ID);
        when(dataLoader.load(FILE_VERSION_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<DLFileVersion> asyncResult = resolvers.getDLFileVersionDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFileVersion result = asyncResult.get();
        assertNull(result);
    }
}
