package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.oauth2.provider.exception.NoSuchOAuth2AuthorizationException;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.service.OAuth2AuthorizationLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import fr.sedona.liferay.graphql.loaders.OAuth2AuthorizationBatchLoader;
import fr.sedona.liferay.graphql.resolvers.OAuth2AuthorizationResolvers;
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
 * Test suite for {@link OAuth2AuthorizationResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class OAuth2AuthorizationResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long OAUTH2_AUTHORIZATION_ID = 987L;
    private static final long COMPANY_ID = 456L;
    private static final long USER_ID = 123L;
    private static final String USER_NAME = "Paul Mars";
    private static final long OAUTH2_APPLICATION_ID = 457L;
    private static final long OAUTH2_APPLICATION_SCOPE_ALIASES_ID = 458L;
    private static final String ACCESS_TOKEN_CONTENT = "accessToken";
    private static final Date ACCESS_TOKEN_CREATE_DATE = new Date();
    private static final Date ACCESS_TOKEN_EXPIRATION_DATE = new Date();
    private static final String REMOTE_IP_INFO = "127.0.0.1";
    private static final String REFRESH_TOKEN_CONTENT = "refreshToken";
    private static final Date REFRESH_TOKEN_CREATE_DATE = new Date();
    private static final Date REFRESH_TOKEN_EXPIRATION_DATE = new Date();
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, OAuth2Authorization> dataLoader;

    @InjectMocks
    OAuth2AuthorizationResolvers resolvers = new OAuth2AuthorizationResolversImpl();

    @Mock
    private OAuth2AuthorizationLocalService localService;

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
                .getDataLoader(OAuth2AuthorizationBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((OAuth2AuthorizationResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2AuthorizationId")))
                    .thenReturn(OAUTH2_AUTHORIZATION_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                    .thenReturn(COMPANY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("userId")))
                    .thenReturn(USER_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("userName")))
                    .thenReturn(USER_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationId")))
                    .thenReturn(OAUTH2_APPLICATION_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationScopeAliasesId")))
                    .thenReturn(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("accessTokenContent")))
                    .thenReturn(ACCESS_TOKEN_CONTENT);
            when(graphQLUtil.getDateArg(eq(environment), eq("accessTokenCreateDate")))
                    .thenReturn(ACCESS_TOKEN_CREATE_DATE);
            when(graphQLUtil.getDateArg(eq(environment), eq("accessTokenExpirationDate")))
                    .thenReturn(ACCESS_TOKEN_EXPIRATION_DATE);
            when(graphQLUtil.getStringArg(eq(environment), eq("remoteIPInfo")))
                    .thenReturn(REMOTE_IP_INFO);
            when(graphQLUtil.getStringArg(eq(environment), eq("refreshTokenContent")))
                    .thenReturn(REFRESH_TOKEN_CONTENT);
            when(graphQLUtil.getDateArg(eq(environment), eq("refreshTokenCreateDate")))
                    .thenReturn(REFRESH_TOKEN_CREATE_DATE);
            when(graphQLUtil.getDateArg(eq(environment), eq("refreshTokenExpirationDate")))
                    .thenReturn(REFRESH_TOKEN_EXPIRATION_DATE);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
                    .thenReturn("");
            when(graphQLUtil.getDateArg(eq(environment), anyString()))
                    .thenReturn(null);
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
    public void getOAuth2AuthorizationsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2Authorization> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2Authorization entity = mock(OAuth2Authorization.class);
                    entity.setOAuth2AuthorizationId(value);
                    availableObjects.add(entity);
                });
        List<OAuth2Authorization> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2Authorizations(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2Authorization> results = resolvers.getOAuth2AuthorizationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2AuthorizationsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<OAuth2Authorization> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2Authorization entity = mock(OAuth2Authorization.class);
                    entity.setOAuth2AuthorizationId(value);
                    availableObjects.add(entity);
                });
        List<OAuth2Authorization> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2Authorizations(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2Authorization> results = resolvers.getOAuth2AuthorizationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2AuthorizationsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2Authorization> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2Authorization entity = mock(OAuth2Authorization.class);
                    entity.setOAuth2AuthorizationId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2Authorizations(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2Authorization> results = resolvers.getOAuth2AuthorizationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2AuthorizationsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2Authorization> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2Authorizations(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2Authorization> results = resolvers.getOAuth2AuthorizationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2AuthorizationDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        OAuth2Authorization expectedResult = mock(OAuth2Authorization.class);
        expectedResult.setOAuth2AuthorizationId(OAUTH2_AUTHORIZATION_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("oAuth2AuthorizationId"))
                .thenReturn(OAUTH2_AUTHORIZATION_ID);
        when(dataLoader.load(OAUTH2_AUTHORIZATION_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<OAuth2Authorization> asyncResult = resolvers.getOAuth2AuthorizationDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        OAuth2Authorization result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getOAuth2AuthorizationDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("oAuth2AuthorizationId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<OAuth2Authorization> asyncResult = resolvers.getOAuth2AuthorizationDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getOAuth2AuthorizationDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("oAuth2AuthorizationId"))
                .thenReturn(OAUTH2_AUTHORIZATION_ID);
        when(dataLoader.load(OAUTH2_AUTHORIZATION_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<OAuth2Authorization> asyncResult = resolvers.getOAuth2AuthorizationDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        OAuth2Authorization result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createOAuth2AuthorizationDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("companyId", COMPANY_ID);
        arguments.put("userName", USER_NAME);
        arguments.put("oAuth2ApplicationId", OAUTH2_APPLICATION_ID);
        arguments.put("oAuth2ApplicationScopeAliasesId", OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
        arguments.put("accessTokenContent", ACCESS_TOKEN_CONTENT);
        arguments.put("accessTokenCreateDate", ACCESS_TOKEN_CREATE_DATE);
        arguments.put("accessTokenExpirationDate", ACCESS_TOKEN_EXPIRATION_DATE);
        arguments.put("remoteIPInfo", REMOTE_IP_INFO);
        arguments.put("refreshTokenContent", REFRESH_TOKEN_CONTENT);
        arguments.put("refreshTokenCreateDate", REFRESH_TOKEN_CREATE_DATE);
        arguments.put("refreshTokenExpirationDate", REFRESH_TOKEN_EXPIRATION_DATE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2Authorization expectedResult = mock(OAuth2Authorization.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setUserName(USER_NAME);
        expectedResult.setOAuth2ApplicationId(OAUTH2_APPLICATION_ID);
        expectedResult.setOAuth2ApplicationScopeAliasesId(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
        expectedResult.setAccessTokenContent(ACCESS_TOKEN_CONTENT);
        expectedResult.setAccessTokenCreateDate(ACCESS_TOKEN_CREATE_DATE);
        expectedResult.setAccessTokenExpirationDate(ACCESS_TOKEN_EXPIRATION_DATE);
        expectedResult.setRemoteIPInfo(REMOTE_IP_INFO);
        expectedResult.setRefreshTokenContent(REFRESH_TOKEN_CONTENT);
        expectedResult.setRefreshTokenCreateDate(REFRESH_TOKEN_CREATE_DATE);
        expectedResult.setRefreshTokenExpirationDate(REFRESH_TOKEN_EXPIRATION_DATE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addOAuth2Authorization(eq(COMPANY_ID), eq(USER_ID), eq(USER_NAME), eq(OAUTH2_APPLICATION_ID), eq(OAUTH2_APPLICATION_SCOPE_ALIASES_ID), eq(ACCESS_TOKEN_CONTENT), eq(ACCESS_TOKEN_CREATE_DATE), eq(ACCESS_TOKEN_EXPIRATION_DATE), eq(REMOTE_IP_INFO), eq(REFRESH_TOKEN_CONTENT), eq(REFRESH_TOKEN_CREATE_DATE), eq(REFRESH_TOKEN_EXPIRATION_DATE)))
                .thenReturn(expectedResult);

        // Asserts
        OAuth2Authorization result = resolvers.createOAuth2AuthorizationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createOAuth2AuthorizationDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("userName", USER_NAME);
        arguments.put("oAuth2ApplicationId", OAUTH2_APPLICATION_ID);
        arguments.put("oAuth2ApplicationScopeAliasesId", OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
        arguments.put("accessTokenContent", ACCESS_TOKEN_CONTENT);
        arguments.put("accessTokenCreateDate", ACCESS_TOKEN_CREATE_DATE);
        arguments.put("accessTokenExpirationDate", ACCESS_TOKEN_EXPIRATION_DATE);
        arguments.put("remoteIPInfo", REMOTE_IP_INFO);
        arguments.put("refreshTokenContent", REFRESH_TOKEN_CONTENT);
        arguments.put("refreshTokenCreateDate", REFRESH_TOKEN_CREATE_DATE);
        arguments.put("refreshTokenExpirationDate", REFRESH_TOKEN_EXPIRATION_DATE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2Authorization expectedResult = mock(OAuth2Authorization.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setUserName(USER_NAME);
        expectedResult.setOAuth2ApplicationId(OAUTH2_APPLICATION_ID);
        expectedResult.setOAuth2ApplicationScopeAliasesId(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
        expectedResult.setAccessTokenContent(ACCESS_TOKEN_CONTENT);
        expectedResult.setAccessTokenCreateDate(ACCESS_TOKEN_CREATE_DATE);
        expectedResult.setAccessTokenExpirationDate(ACCESS_TOKEN_EXPIRATION_DATE);
        expectedResult.setRemoteIPInfo(REMOTE_IP_INFO);
        expectedResult.setRefreshTokenContent(REFRESH_TOKEN_CONTENT);
        expectedResult.setRefreshTokenCreateDate(REFRESH_TOKEN_CREATE_DATE);
        expectedResult.setRefreshTokenExpirationDate(REFRESH_TOKEN_EXPIRATION_DATE);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addOAuth2Authorization(eq(COMPANY_ID), eq(DEFAULT_USER_ID), eq(USER_NAME), eq(OAUTH2_APPLICATION_ID), eq(OAUTH2_APPLICATION_SCOPE_ALIASES_ID), eq(ACCESS_TOKEN_CONTENT), eq(ACCESS_TOKEN_CREATE_DATE), eq(ACCESS_TOKEN_EXPIRATION_DATE), eq(REMOTE_IP_INFO), eq(REFRESH_TOKEN_CONTENT), eq(REFRESH_TOKEN_CREATE_DATE), eq(REFRESH_TOKEN_EXPIRATION_DATE)))
                .thenReturn(expectedResult);

        // Asserts
        OAuth2Authorization result = resolvers.createOAuth2AuthorizationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createOAuth2AuthorizationDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addOAuth2Authorization(anyLong(), anyLong(), anyString(), anyLong(), anyLong(), anyString(), any(Date.class), any(Date.class), anyString(), anyString(), any(Date.class), any(Date.class)))
                .thenThrow(PortalException.class);

        // Asserts
        OAuth2Authorization result = resolvers.createOAuth2AuthorizationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteOAuth2AuthorizationDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("oAuth2AuthorizationId", OAUTH2_AUTHORIZATION_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2Authorization expectedResult = mock(OAuth2Authorization.class);
        expectedResult.setOAuth2AuthorizationId(OAUTH2_AUTHORIZATION_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteOAuth2Authorization(eq(OAUTH2_AUTHORIZATION_ID)))
                .thenReturn(expectedResult);

        // Asserts
        OAuth2Authorization result = resolvers.deleteOAuth2AuthorizationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchOAuth2AuthorizationException.class)
    public void deleteOAuth2AuthorizationDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        OAuth2Authorization expectedResult = mock(OAuth2Authorization.class);
        expectedResult.setOAuth2AuthorizationId(OAUTH2_AUTHORIZATION_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteOAuth2Authorization(eq(OAUTH2_AUTHORIZATION_ID)))
                .thenThrow(NoSuchOAuth2AuthorizationException.class);

        // Asserts
        OAuth2Authorization result = resolvers.deleteOAuth2AuthorizationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchOAuth2AuthorizationException.class)
    public void deleteOAuth2AuthorizationDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("oAuth2AuthorizationId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2Authorization expectedResult = mock(OAuth2Authorization.class);
        expectedResult.setOAuth2AuthorizationId(OAUTH2_AUTHORIZATION_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2AuthorizationId")))
                .thenReturn(789456L);
        when(localService.deleteOAuth2Authorization(eq(789456L)))
                .thenThrow(NoSuchOAuth2AuthorizationException.class);

        // Asserts
        OAuth2Authorization result = resolvers.deleteOAuth2AuthorizationDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
