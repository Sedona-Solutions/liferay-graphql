package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.oauth2.provider.model.OAuth2ScopeGrant;
import com.liferay.oauth2.provider.service.OAuth2ScopeGrantLocalService;
import fr.sedona.liferay.graphql.loaders.OAuth2ScopeGrantBatchLoader;
import fr.sedona.liferay.graphql.resolvers.OAuth2ScopeGrantResolvers;
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
 * Test suite for {@link OAuth2ScopeGrantResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class OAuth2ScopeGrantResolversImplTest {
    private static final long OAUTH2_SCOPE_GRANT_ID = 987L;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, OAuth2ScopeGrant> dataLoader;

    @InjectMocks
    OAuth2ScopeGrantResolvers resolvers = new OAuth2ScopeGrantResolversImpl();

    @Mock
    private OAuth2ScopeGrantLocalService localService;

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
                .getDataLoader(OAuth2ScopeGrantBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((OAuth2ScopeGrantResolversImpl) resolvers).setUtil(new GraphQLUtil());
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
    public void getOAuth2ScopeGrantsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2ScopeGrant> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2ScopeGrant entity = mock(OAuth2ScopeGrant.class);
                    entity.setOAuth2ScopeGrantId(value);
                    availableObjects.add(entity);
                });
        List<OAuth2ScopeGrant> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2ScopeGrants(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2ScopeGrant> results = resolvers.getOAuth2ScopeGrantsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ScopeGrantsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<OAuth2ScopeGrant> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2ScopeGrant entity = mock(OAuth2ScopeGrant.class);
                    entity.setOAuth2ScopeGrantId(value);
                    availableObjects.add(entity);
                });
        List<OAuth2ScopeGrant> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2ScopeGrants(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2ScopeGrant> results = resolvers.getOAuth2ScopeGrantsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ScopeGrantsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2ScopeGrant> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2ScopeGrant entity = mock(OAuth2ScopeGrant.class);
                    entity.setOAuth2ScopeGrantId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2ScopeGrants(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2ScopeGrant> results = resolvers.getOAuth2ScopeGrantsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ScopeGrantsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2ScopeGrant> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2ScopeGrants(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2ScopeGrant> results = resolvers.getOAuth2ScopeGrantsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ScopeGrantDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        OAuth2ScopeGrant expectedResult = mock(OAuth2ScopeGrant.class);
        expectedResult.setOAuth2ScopeGrantId(OAUTH2_SCOPE_GRANT_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("oAuth2ScopeGrantId"))
                .thenReturn(OAUTH2_SCOPE_GRANT_ID);
        when(dataLoader.load(OAUTH2_SCOPE_GRANT_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<OAuth2ScopeGrant> asyncResult = resolvers.getOAuth2ScopeGrantDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        OAuth2ScopeGrant result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getOAuth2ScopeGrantDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("oAuth2ScopeGrantId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<OAuth2ScopeGrant> asyncResult = resolvers.getOAuth2ScopeGrantDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getOAuth2ScopeGrantDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("oAuth2ScopeGrantId"))
                .thenReturn(OAUTH2_SCOPE_GRANT_ID);
        when(dataLoader.load(OAUTH2_SCOPE_GRANT_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<OAuth2ScopeGrant> asyncResult = resolvers.getOAuth2ScopeGrantDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        OAuth2ScopeGrant result = asyncResult.get();
        assertNull(result);
    }

    // TODO: Implement unit tests for associate/dissociate
}
