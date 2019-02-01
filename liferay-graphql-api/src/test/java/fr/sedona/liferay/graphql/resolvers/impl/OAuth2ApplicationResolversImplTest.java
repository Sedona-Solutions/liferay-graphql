package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.oauth2.provider.constants.GrantType;
import com.liferay.oauth2.provider.exception.NoSuchOAuth2ApplicationException;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.OAuth2ApplicationBatchLoader;
import fr.sedona.liferay.graphql.resolvers.OAuth2ApplicationResolvers;
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
 * Test suite for {@link OAuth2ApplicationResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class OAuth2ApplicationResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long OAUTH2_APPLICATION_ID = 987L;
    private static final long COMPANY_ID = 456L;
    private static final long USER_ID = 123L;
    private static final String USER_NAME = "Paul Mars";
    private static final List<GrantType> ALLOWED_GRANT_TYPES_LIST = Collections.singletonList(GrantType.CLIENT_CREDENTIALS);
    private static final String CLIENT_ID = "clientId";
    private static final int CLIENT_PROFILE = 0;
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String DESCRIPTION = "Description";
    private static final String[] FEATURES_LIST = new String[]{"feature1", "feature2", "feature3"};
    private static final String HOME_PAGE_URL = "https://sedona.fr";
    private static final long ICON_FILE_ENTRY_ID = 0;
    private static final String NAME = "Sedona App";
    private static final String PRIVACY_POLICY_URL = "https://privacy.sedona.fr";
    private static final String[] REDIRECT_URIS_LIST = new String[]{"https://redirect.sedona.fr"};
    private static final String[] SCOPE_ALIASES_LIST = new String[]{"alias1", "alias2"};
    private static final long OAUTH2_APPLICATION_SCOPE_ALIASES_ID = 1L;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, OAuth2Application> dataLoader;

    @InjectMocks
    OAuth2ApplicationResolvers resolvers = new OAuth2ApplicationResolversImpl();

    @Mock
    private OAuth2ApplicationLocalService localService;

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
                .getDataLoader(OAuth2ApplicationBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((OAuth2ApplicationResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationId")))
                    .thenReturn(OAUTH2_APPLICATION_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                    .thenReturn(COMPANY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("userId")))
                    .thenReturn(USER_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("userName")))
                    .thenReturn(USER_NAME);
            when(mockEnvironment.getArgument("allowedGrantTypesList"))
                    .thenReturn(ALLOWED_GRANT_TYPES_LIST);
            when(graphQLUtil.getStringArg(eq(environment), eq("clientId")))
                    .thenReturn(CLIENT_ID);
            when(graphQLUtil.getIntArg(eq(environment), eq("clientProfile")))
                    .thenReturn(CLIENT_PROFILE);
            when(graphQLUtil.getStringArg(eq(environment), eq("clientSecret")))
                    .thenReturn(CLIENT_SECRET);
            when(graphQLUtil.getStringArg(eq(environment), eq("description")))
                    .thenReturn(DESCRIPTION);
            when(graphQLUtil.getStringArrayArg(eq(environment), eq("featuresList")))
                    .thenReturn(FEATURES_LIST);
            when(graphQLUtil.getStringArg(eq(environment), eq("homePageURL")))
                    .thenReturn(HOME_PAGE_URL);
            when(graphQLUtil.getLongArg(eq(environment), eq("iconFileEntryId")))
                    .thenReturn(ICON_FILE_ENTRY_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("privacyPolicyURL")))
                    .thenReturn(PRIVACY_POLICY_URL);
            when(graphQLUtil.getStringArrayArg(eq(environment), eq("redirectURIsList")))
                    .thenReturn(REDIRECT_URIS_LIST);
            when(graphQLUtil.getStringArrayArg(eq(environment), eq("scopeAliasesList")))
                    .thenReturn(SCOPE_ALIASES_LIST);
            when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationScopeAliasesId")))
                    .thenReturn(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getIntArg(eq(environment), anyString()))
                    .thenReturn(0);
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
    public void getOAuth2ApplicationsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2Application> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2Application entity = mock(OAuth2Application.class);
                    entity.setOAuth2ApplicationId(value);
                    availableObjects.add(entity);
                });
        List<OAuth2Application> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2Applications(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2Application> results = resolvers.getOAuth2ApplicationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ApplicationsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<OAuth2Application> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2Application entity = mock(OAuth2Application.class);
                    entity.setOAuth2ApplicationId(value);
                    availableObjects.add(entity);
                });
        List<OAuth2Application> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2Applications(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2Application> results = resolvers.getOAuth2ApplicationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ApplicationsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2Application> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    OAuth2Application entity = mock(OAuth2Application.class);
                    entity.setOAuth2ApplicationId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2Applications(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2Application> results = resolvers.getOAuth2ApplicationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ApplicationsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<OAuth2Application> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOAuth2Applications(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<OAuth2Application> results = resolvers.getOAuth2ApplicationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOAuth2ApplicationDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        OAuth2Application expectedResult = mock(OAuth2Application.class);
        expectedResult.setOAuth2ApplicationId(OAUTH2_APPLICATION_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("oAuth2ApplicationId"))
                .thenReturn(OAUTH2_APPLICATION_ID);
        when(dataLoader.load(OAUTH2_APPLICATION_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<OAuth2Application> asyncResult = resolvers.getOAuth2ApplicationDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        OAuth2Application result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getOAuth2ApplicationDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("oAuth2ApplicationId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<OAuth2Application> asyncResult = resolvers.getOAuth2ApplicationDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getOAuth2ApplicationDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("oAuth2ApplicationId"))
                .thenReturn(OAUTH2_APPLICATION_ID);
        when(dataLoader.load(OAUTH2_APPLICATION_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<OAuth2Application> asyncResult = resolvers.getOAuth2ApplicationDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        OAuth2Application result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createOAuth2ApplicationDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("companyId", COMPANY_ID);
        arguments.put("userName", USER_NAME);
        arguments.put("allowedGrantTypesList", ALLOWED_GRANT_TYPES_LIST);
        arguments.put("clientId", CLIENT_ID);
        arguments.put("clientProfile", CLIENT_PROFILE);
        arguments.put("clientSecret", CLIENT_SECRET);
        arguments.put("description", DESCRIPTION);
        arguments.put("featuresList", FEATURES_LIST);
        arguments.put("homePageURL", HOME_PAGE_URL);
        arguments.put("iconFileEntryId", ICON_FILE_ENTRY_ID);
        arguments.put("name", NAME);
        arguments.put("privacyPolicyURL", PRIVACY_POLICY_URL);
        arguments.put("redirectURIsList", REDIRECT_URIS_LIST);
        arguments.put("scopeAliasesList", SCOPE_ALIASES_LIST);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2Application expectedResult = mock(OAuth2Application.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setUserName(USER_NAME);
        expectedResult.setAllowedGrantTypesList(ALLOWED_GRANT_TYPES_LIST);
        expectedResult.setClientId(CLIENT_ID);
        expectedResult.setClientProfile(CLIENT_PROFILE);
        expectedResult.setClientSecret(CLIENT_SECRET);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setFeaturesList(Arrays.asList(FEATURES_LIST));
        expectedResult.setHomePageURL(HOME_PAGE_URL);
        expectedResult.setIconFileEntryId(ICON_FILE_ENTRY_ID);
        expectedResult.setName(NAME);
        expectedResult.setPrivacyPolicyURL(PRIVACY_POLICY_URL);
        expectedResult.setRedirectURIsList(Arrays.asList(REDIRECT_URIS_LIST));

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addOAuth2Application(eq(COMPANY_ID), eq(USER_ID), eq(USER_NAME), eq(ALLOWED_GRANT_TYPES_LIST), eq(CLIENT_ID), eq(CLIENT_PROFILE), eq(CLIENT_SECRET), eq(DESCRIPTION), eq(Arrays.asList(FEATURES_LIST)), eq(HOME_PAGE_URL), eq(ICON_FILE_ENTRY_ID), eq(NAME), eq(PRIVACY_POLICY_URL), eq(Arrays.asList(REDIRECT_URIS_LIST)), eq(Arrays.asList(SCOPE_ALIASES_LIST)), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        OAuth2Application result = resolvers.createOAuth2ApplicationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createOAuth2ApplicationDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("userId", USER_ID);
        arguments.put("userName", USER_NAME);
        arguments.put("allowedGrantTypesList", ALLOWED_GRANT_TYPES_LIST);
        arguments.put("clientId", CLIENT_ID);
        arguments.put("clientProfile", CLIENT_PROFILE);
        arguments.put("clientSecret", CLIENT_SECRET);
        arguments.put("description", DESCRIPTION);
        arguments.put("featuresList", FEATURES_LIST);
        arguments.put("homePageURL", HOME_PAGE_URL);
        arguments.put("iconFileEntryId", ICON_FILE_ENTRY_ID);
        arguments.put("name", NAME);
        arguments.put("privacyPolicyURL", PRIVACY_POLICY_URL);
        arguments.put("redirectURIsList", REDIRECT_URIS_LIST);
        arguments.put("scopeAliasesList", SCOPE_ALIASES_LIST);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2Application expectedResult = mock(OAuth2Application.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setUserName(USER_NAME);
        expectedResult.setAllowedGrantTypesList(ALLOWED_GRANT_TYPES_LIST);
        expectedResult.setClientId(CLIENT_ID);
        expectedResult.setClientProfile(CLIENT_PROFILE);
        expectedResult.setClientSecret(CLIENT_SECRET);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setFeaturesList(Arrays.asList(FEATURES_LIST));
        expectedResult.setHomePageURL(HOME_PAGE_URL);
        expectedResult.setIconFileEntryId(ICON_FILE_ENTRY_ID);
        expectedResult.setName(NAME);
        expectedResult.setPrivacyPolicyURL(PRIVACY_POLICY_URL);
        expectedResult.setRedirectURIsList(Arrays.asList(REDIRECT_URIS_LIST));

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addOAuth2Application(eq(COMPANY_ID), eq(DEFAULT_USER_ID), eq(USER_NAME), eq(ALLOWED_GRANT_TYPES_LIST), eq(CLIENT_ID), eq(CLIENT_PROFILE), eq(CLIENT_SECRET), eq(DESCRIPTION), eq(Arrays.asList(FEATURES_LIST)), eq(HOME_PAGE_URL), eq(ICON_FILE_ENTRY_ID), eq(NAME), eq(PRIVACY_POLICY_URL), eq(Arrays.asList(REDIRECT_URIS_LIST)), eq(Arrays.asList(SCOPE_ALIASES_LIST)), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        OAuth2Application result = resolvers.createOAuth2ApplicationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createOAuth2ApplicationDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addOAuth2Application(anyLong(), anyLong(), anyString(), anyList(), anyString(), anyInt(), anyString(), anyString(), anyList(), anyString(), anyLong(), anyString(), anyString(), anyList(), anyList(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        OAuth2Application result = resolvers.createOAuth2ApplicationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateOAuth2ApplicationDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("oAuth2ApplicationId", OAUTH2_APPLICATION_ID);
        arguments.put("allowedGrantTypesList", ALLOWED_GRANT_TYPES_LIST);
        arguments.put("clientId", CLIENT_ID);
        arguments.put("clientProfile", CLIENT_PROFILE);
        arguments.put("clientSecret", CLIENT_SECRET);
        arguments.put("description", DESCRIPTION);
        arguments.put("featuresList", FEATURES_LIST);
        arguments.put("homePageURL", HOME_PAGE_URL);
        arguments.put("iconFileEntryId", ICON_FILE_ENTRY_ID);
        arguments.put("name", NAME);
        arguments.put("privacyPolicyURL", PRIVACY_POLICY_URL);
        arguments.put("redirectURIsList", REDIRECT_URIS_LIST);
        arguments.put("oAuth2ApplicationScopeAliasesId", OAUTH2_APPLICATION_SCOPE_ALIASES_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2Application expectedResult = mock(OAuth2Application.class);
        expectedResult.setOAuth2ApplicationId(OAUTH2_APPLICATION_ID);
        expectedResult.setAllowedGrantTypesList(ALLOWED_GRANT_TYPES_LIST);
        expectedResult.setClientId(CLIENT_ID);
        expectedResult.setClientProfile(CLIENT_PROFILE);
        expectedResult.setClientSecret(CLIENT_SECRET);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setFeaturesList(Arrays.asList(FEATURES_LIST));
        expectedResult.setHomePageURL(HOME_PAGE_URL);
        expectedResult.setIconFileEntryId(ICON_FILE_ENTRY_ID);
        expectedResult.setName(NAME);
        expectedResult.setPrivacyPolicyURL(PRIVACY_POLICY_URL);
        expectedResult.setRedirectURIsList(Arrays.asList(REDIRECT_URIS_LIST));
        expectedResult.setOAuth2ApplicationScopeAliasesId(OAUTH2_APPLICATION_SCOPE_ALIASES_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateOAuth2Application(eq(OAUTH2_APPLICATION_ID), eq(ALLOWED_GRANT_TYPES_LIST), eq(CLIENT_ID), eq(CLIENT_PROFILE), eq(CLIENT_SECRET), eq(DESCRIPTION), eq(Arrays.asList(FEATURES_LIST)), eq(HOME_PAGE_URL), eq(ICON_FILE_ENTRY_ID), eq(NAME), eq(PRIVACY_POLICY_URL), eq(Arrays.asList(REDIRECT_URIS_LIST)), eq(OAUTH2_APPLICATION_SCOPE_ALIASES_ID), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        OAuth2Application result = resolvers.updateOAuth2ApplicationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchOAuth2ApplicationException.class)
    public void updateOAuth2ApplicationDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("userId", USER_ID);
        arguments.put("userName", USER_NAME);
        arguments.put("allowedGrantTypesList", ALLOWED_GRANT_TYPES_LIST);
        arguments.put("clientId", CLIENT_ID);
        arguments.put("clientProfile", CLIENT_PROFILE);
        arguments.put("clientSecret", CLIENT_SECRET);
        arguments.put("description", DESCRIPTION);
        arguments.put("featuresList", FEATURES_LIST);
        arguments.put("homePageURL", HOME_PAGE_URL);
        arguments.put("iconFileEntryId", ICON_FILE_ENTRY_ID);
        arguments.put("name", NAME);
        arguments.put("privacyPolicyURL", PRIVACY_POLICY_URL);
        arguments.put("redirectURIsList", REDIRECT_URIS_LIST);

        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationId")))
                .thenReturn(0L);
        when(localService.updateOAuth2Application(eq(0L), eq(ALLOWED_GRANT_TYPES_LIST), eq(CLIENT_ID), eq(CLIENT_PROFILE), eq(CLIENT_SECRET), eq(DESCRIPTION), eq(Arrays.asList(FEATURES_LIST)), eq(HOME_PAGE_URL), eq(ICON_FILE_ENTRY_ID), eq(NAME), eq(PRIVACY_POLICY_URL), eq(Arrays.asList(REDIRECT_URIS_LIST)), eq(OAUTH2_APPLICATION_SCOPE_ALIASES_ID), any(ServiceContext.class)))
                .thenThrow(NoSuchOAuth2ApplicationException.class);

        // Asserts
        OAuth2Application result = resolvers.updateOAuth2ApplicationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchOAuth2ApplicationException.class)
    public void updateOAuth2ApplicationDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("oAuth2ApplicationId", 789456L);
        arguments.put("companyId", COMPANY_ID);
        arguments.put("userId", USER_ID);
        arguments.put("userName", USER_NAME);
        arguments.put("allowedGrantTypesList", ALLOWED_GRANT_TYPES_LIST);
        arguments.put("clientId", CLIENT_ID);
        arguments.put("clientProfile", CLIENT_PROFILE);
        arguments.put("clientSecret", CLIENT_SECRET);
        arguments.put("description", DESCRIPTION);
        arguments.put("featuresList", FEATURES_LIST);
        arguments.put("homePageURL", HOME_PAGE_URL);
        arguments.put("iconFileEntryId", ICON_FILE_ENTRY_ID);
        arguments.put("name", NAME);
        arguments.put("privacyPolicyURL", PRIVACY_POLICY_URL);
        arguments.put("redirectURIsList", REDIRECT_URIS_LIST);

        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationId")))
                .thenReturn(789456L);
        when(localService.updateOAuth2Application(eq(789456L), eq(ALLOWED_GRANT_TYPES_LIST), eq(CLIENT_ID), eq(CLIENT_PROFILE), eq(CLIENT_SECRET), eq(DESCRIPTION), eq(Arrays.asList(FEATURES_LIST)), eq(HOME_PAGE_URL), eq(ICON_FILE_ENTRY_ID), eq(NAME), eq(PRIVACY_POLICY_URL), eq(Arrays.asList(REDIRECT_URIS_LIST)), eq(OAUTH2_APPLICATION_SCOPE_ALIASES_ID), any(ServiceContext.class)))
                .thenThrow(NoSuchOAuth2ApplicationException.class);

        // Asserts
        OAuth2Application result = resolvers.updateOAuth2ApplicationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateOAuth2ApplicationDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationId")))
                .thenReturn(0L);
        when(localService.updateOAuth2Application(anyLong(), anyList(), anyString(), anyInt(), anyString(), anyString(), anyList(), anyString(), anyLong(), anyString(), anyString(), anyList(), anyLong(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        OAuth2Application result = resolvers.updateOAuth2ApplicationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteOAuth2ApplicationDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("oAuth2ApplicationId", OAUTH2_APPLICATION_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2Application expectedResult = mock(OAuth2Application.class);
        expectedResult.setOAuth2ApplicationId(OAUTH2_APPLICATION_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteOAuth2Application(eq(OAUTH2_APPLICATION_ID)))
                .thenReturn(expectedResult);

        // Asserts
        OAuth2Application result = resolvers.deleteOAuth2ApplicationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchOAuth2ApplicationException.class)
    public void deleteOAuth2ApplicationDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        OAuth2Application expectedResult = mock(OAuth2Application.class);
        expectedResult.setOAuth2ApplicationId(OAUTH2_APPLICATION_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteOAuth2Application(eq(OAUTH2_APPLICATION_ID)))
                .thenThrow(NoSuchOAuth2ApplicationException.class);

        // Asserts
        OAuth2Application result = resolvers.deleteOAuth2ApplicationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchOAuth2ApplicationException.class)
    public void deleteOAuth2ApplicationDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("oAuth2ApplicationId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        OAuth2Application expectedResult = mock(OAuth2Application.class);
        expectedResult.setOAuth2ApplicationId(OAUTH2_APPLICATION_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("oAuth2ApplicationId")))
                .thenReturn(789456L);
        when(localService.deleteOAuth2Application(eq(789456L)))
                .thenThrow(NoSuchOAuth2ApplicationException.class);

        // Asserts
        OAuth2Application result = resolvers.deleteOAuth2ApplicationDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
