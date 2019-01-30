package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.model.DLFileEntryMetadata;
import com.liferay.document.library.kernel.service.DLFileEntryMetadataLocalService;
import fr.sedona.liferay.graphql.loaders.DLFileEntryMetadataBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFileEntryMetadataResolvers;
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
 * Test suite for {@link DLFileEntryMetadataResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class DLFileEntryMetadataResolversImplTest {
    private static final long FILE_ENTRY_METADATA_ID = 987L;

    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, DLFileEntryMetadata> dataLoader;

    @InjectMocks
    DLFileEntryMetadataResolvers resolvers = new DLFileEntryMetadataResolversImpl();

    @Mock
    private DLFileEntryMetadataLocalService localService;

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
                .getDataLoader(DLFileEntryMetadataBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((DLFileEntryMetadataResolversImpl) resolvers).setUtil(new GraphQLUtil());
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
    public void getDLFileEntryMetadatasDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileEntryMetadata> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileEntryMetadata entity = mock(DLFileEntryMetadata.class);
                    entity.setFileEntryMetadataId(value);
                    availableObjects.add(entity);
                });
        List<DLFileEntryMetadata> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntryMetadatas(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntryMetadata> results = resolvers.getDLFileEntryMetadatasDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntryMetadatasDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<DLFileEntryMetadata> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileEntryMetadata entity = mock(DLFileEntryMetadata.class);
                    entity.setFileEntryMetadataId(value);
                    availableObjects.add(entity);
                });
        List<DLFileEntryMetadata> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntryMetadatas(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntryMetadata> results = resolvers.getDLFileEntryMetadatasDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntryMetadatasDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileEntryMetadata> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileEntryMetadata entity = mock(DLFileEntryMetadata.class);
                    entity.setFileEntryMetadataId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntryMetadatas(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntryMetadata> results = resolvers.getDLFileEntryMetadatasDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntryMetadatasDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileEntryMetadata> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntryMetadatas(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntryMetadata> results = resolvers.getDLFileEntryMetadatasDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntryMetadataDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        DLFileEntryMetadata expectedResult = mock(DLFileEntryMetadata.class);
        expectedResult.setFileEntryMetadataId(FILE_ENTRY_METADATA_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileEntryMetadataId"))
                .thenReturn(FILE_ENTRY_METADATA_ID);
        when(dataLoader.load(FILE_ENTRY_METADATA_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<DLFileEntryMetadata> asyncResult = resolvers.getDLFileEntryMetadataDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFileEntryMetadata result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getDLFileEntryMetadataDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("fileEntryMetadataId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<DLFileEntryMetadata> asyncResult = resolvers.getDLFileEntryMetadataDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getDLFileEntryMetadataDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileEntryMetadataId"))
                .thenReturn(FILE_ENTRY_METADATA_ID);
        when(dataLoader.load(FILE_ENTRY_METADATA_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<DLFileEntryMetadata> asyncResult = resolvers.getDLFileEntryMetadataDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFileEntryMetadata result = asyncResult.get();
        assertNull(result);
    }
}
