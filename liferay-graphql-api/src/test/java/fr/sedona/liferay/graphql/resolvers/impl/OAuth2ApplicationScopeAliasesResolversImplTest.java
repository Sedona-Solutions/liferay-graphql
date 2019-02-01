package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.oauth2.provider.exception.NoSuchOAuth2ApplicationScopeAliasesException;
import com.liferay.oauth2.provider.model.OAuth2ApplicationScopeAliases;
import com.liferay.oauth2.provider.service.OAuth2ApplicationScopeAliasesLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import fr.sedona.liferay.graphql.loaders.OAuth2ApplicationScopeAliasesBatchLoader;
import fr.sedona.liferay.graphql.resolvers.OAuth2ApplicationScopeAliasesResolvers;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link OAuth2ApplicationScopeAliasesResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class OAuth2ApplicationScopeAliasesResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long OAUTH2_APPLICATION_SCOPE_ALIASES_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long COMPANY_ID = 456L;
    private static final String USER_NAME = null;
    private static final long OAUTH2_APPLICATION_ID = 0;
    private static final String[] SCOPE_ALIASES_LIST = new String[]{"alias1", "alias2"};
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, OAuth2ApplicationScopeAliases> dataLoader;

    @InjectMocks
    OAuth2ApplicationScopeAliasesResolvers resolvers = new OAuth2ApplicationScopeAliasesResolversImpl();

    @Mock
    private OAuth2ApplicationScopeAliasesLocalService localService;

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
                .getDataLoader(OAuth2ApplicationScopeAliasesBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((OAuth2ApplicationScopeAliasesResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationScopeAliasesId")))
                    .thenReturn(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                    .thenReturn(COMPANY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("userId")))
                    .thenReturn(USER_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("userName")))
                    .thenReturn(USER_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationId")))
                    .thenReturn(OAUTH2_APPLICATION_ID);
            when(graphQLUtil.getStringArrayArg(eq(environment), eq("scopeAliasesList")))
                    .thenReturn(SCOPE_ALIASES_LIST);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
                    .thenReturn("");
            when(graphQLUtil.getStringArrayArg(eq(environment), anyString()))
                    .thenReturn(new String[0]);
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
    public void getOAuth2ApplicationScopeAliasesesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2ApplicationScopeAliases> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2ApplicationScopeAliases entity = mock(OAuth2ApplicationScopeAliases.class);
                    entity.setOAuth2ApplicationScopeAliasesId(value);
                    availableObjects.add(entity);
                });
        List<OAuth2ApplicationScopeAliases> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2ApplicationScopeAliaseses(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2ApplicationScopeAliases> results = resolvers.getOAuth2ApplicationScopeAliasesesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ApplicationScopeAliasesesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<OAuth2ApplicationScopeAliases> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2ApplicationScopeAliases entity = mock(OAuth2ApplicationScopeAliases.class);
                    entity.setOAuth2ApplicationScopeAliasesId(value);
                    availableObjects.add(entity);
                });
        List<OAuth2ApplicationScopeAliases> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2ApplicationScopeAliaseses(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2ApplicationScopeAliases> results = resolvers.getOAuth2ApplicationScopeAliasesesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ApplicationScopeAliasesesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2ApplicationScopeAliases> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2ApplicationScopeAliases entity = mock(OAuth2ApplicationScopeAliases.class);
                    entity.setOAuth2ApplicationScopeAliasesId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2ApplicationScopeAliaseses(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2ApplicationScopeAliases> results = resolvers.getOAuth2ApplicationScopeAliasesesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ApplicationScopeAliasesesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2ApplicationScopeAliases> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2ApplicationScopeAliaseses(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2ApplicationScopeAliases> results = resolvers.getOAuth2ApplicationScopeAliasesesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ApplicationScopeAliasesDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        OAuth2ApplicationScopeAliases expectedResult = mock(OAuth2ApplicationScopeAliases.class);
        expectedResult.setOAuth2ApplicationScopeAliasesId(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("oAuth2ApplicationScopeAliasesId"))
                .thenReturn(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
        when(dataLoader.load(OAUTH2_APPLICATION_SCOPE_ALIASES_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<OAuth2ApplicationScopeAliases> asyncResult = resolvers.getOAuth2ApplicationScopeAliasesDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        OAuth2ApplicationScopeAliases result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getOAuth2ApplicationScopeAliasesDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("oAuth2ApplicationScopeAliasesId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<OAuth2ApplicationScopeAliases> asyncResult = resolvers.getOAuth2ApplicationScopeAliasesDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getOAuth2ApplicationScopeAliasesDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("oAuth2ApplicationScopeAliasesId"))
                .thenReturn(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
        when(dataLoader.load(OAUTH2_APPLICATION_SCOPE_ALIASES_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<OAuth2ApplicationScopeAliases> asyncResult = resolvers.getOAuth2ApplicationScopeAliasesDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        OAuth2ApplicationScopeAliases result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createOAuth2ApplicationScopeAliasesDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("companyId", COMPANY_ID);
        arguments.put("userName", USER_NAME);
        arguments.put("oAuth2ApplicationId", OAUTH2_APPLICATION_ID);
        arguments.put("scopeAliasesList", SCOPE_ALIASES_LIST);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2ApplicationScopeAliases expectedResult = mock(OAuth2ApplicationScopeAliases.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setUserName(USER_NAME);
        expectedResult.setOAuth2ApplicationId(OAUTH2_APPLICATION_ID);
        expectedResult.setScopeAliasesList(Arrays.asList(SCOPE_ALIASES_LIST));

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addOAuth2ApplicationScopeAliases(eq(COMPANY_ID), eq(USER_ID), eq(USER_NAME), eq(OAUTH2_APPLICATION_ID), eq(Arrays.asList(SCOPE_ALIASES_LIST))))
                .thenReturn(expectedResult);

        // Asserts
        OAuth2ApplicationScopeAliases result = resolvers.createOAuth2ApplicationScopeAliasesDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createOAuth2ApplicationScopeAliasesDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("userId", USER_ID);
        arguments.put("userName", USER_NAME);
        arguments.put("oAuth2ApplicationId", OAUTH2_APPLICATION_ID);
        arguments.put("scopeAliasesList", SCOPE_ALIASES_LIST);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2ApplicationScopeAliases expectedResult = mock(OAuth2ApplicationScopeAliases.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setUserName(USER_NAME);
        expectedResult.setOAuth2ApplicationId(OAUTH2_APPLICATION_ID);
        expectedResult.setScopeAliasesList(Arrays.asList(SCOPE_ALIASES_LIST));

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addOAuth2ApplicationScopeAliases(eq(COMPANY_ID), eq(DEFAULT_USER_ID), eq(USER_NAME), eq(OAUTH2_APPLICATION_ID), eq(Arrays.asList(SCOPE_ALIASES_LIST))))
                .thenReturn(expectedResult);

        // Asserts
        OAuth2ApplicationScopeAliases result = resolvers.createOAuth2ApplicationScopeAliasesDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createOAuth2ApplicationScopeAliasesDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addOAuth2ApplicationScopeAliases(anyLong(), anyLong(), anyString(), anyLong(), anyList()))
                .thenThrow(PortalException.class);

        // Asserts
        OAuth2ApplicationScopeAliases result = resolvers.createOAuth2ApplicationScopeAliasesDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteOAuth2ApplicationScopeAliasesDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("oAuth2ApplicationScopeAliasesId", OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2ApplicationScopeAliases expectedResult = mock(OAuth2ApplicationScopeAliases.class);
        expectedResult.setOAuth2ApplicationScopeAliasesId(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteOAuth2ApplicationScopeAliases(eq(OAUTH2_APPLICATION_SCOPE_ALIASES_ID)))
                .thenReturn(expectedResult);

        // Asserts
        OAuth2ApplicationScopeAliases result = resolvers.deleteOAuth2ApplicationScopeAliasesDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchOAuth2ApplicationScopeAliasesException.class)
    public void deleteOAuth2ApplicationScopeAliasesDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        OAuth2ApplicationScopeAliases expectedResult = mock(OAuth2ApplicationScopeAliases.class);
        expectedResult.setOAuth2ApplicationScopeAliasesId(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteOAuth2ApplicationScopeAliases(eq(OAUTH2_APPLICATION_SCOPE_ALIASES_ID)))
                .thenThrow(NoSuchOAuth2ApplicationScopeAliasesException.class);

        // Asserts
        OAuth2ApplicationScopeAliases result = resolvers.deleteOAuth2ApplicationScopeAliasesDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchOAuth2ApplicationScopeAliasesException.class)
    public void deleteOAuth2ApplicationScopeAliasesDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("oAuth2ApplicationScopeAliasesId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2ApplicationScopeAliases expectedResult = mock(OAuth2ApplicationScopeAliases.class);
        expectedResult.setOAuth2ApplicationScopeAliasesId(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationScopeAliasesId")))
                .thenReturn(789456L);
        when(localService.deleteOAuth2ApplicationScopeAliases(eq(789456L)))
                .thenThrow(NoSuchOAuth2ApplicationScopeAliasesException.class);

        // Asserts
        OAuth2ApplicationScopeAliases result = resolvers.deleteOAuth2ApplicationScopeAliasesDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
