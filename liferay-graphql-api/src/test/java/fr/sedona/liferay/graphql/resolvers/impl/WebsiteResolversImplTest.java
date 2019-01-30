package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchWebsiteException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Website;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.WebsiteLocalService;
import fr.sedona.liferay.graphql.loaders.WebsiteBatchLoader;
import fr.sedona.liferay.graphql.resolvers.WebsiteResolvers;
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
 * Test suite for {@link WebsiteResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class WebsiteResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long WEBSITE_ID = 987L;
    private static final long USER_ID = 123L;
    private static final String CLASS_NAME = "fr.sedona.Test";
    private static final long CLASS_PK = 123L;
    private static final String URL = "https://sedona.fr";
    private static final long TYPE_ID = 15L;
    private static final boolean PRIMARY = true;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, Website> dataLoader;

    @InjectMocks
    WebsiteResolvers resolvers = new WebsiteResolversImpl();

    @Mock
    private WebsiteLocalService localService;

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
                .getDataLoader(WebsiteBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((WebsiteResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("websiteId")))
                    .thenReturn(WEBSITE_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("className")))
                    .thenReturn(CLASS_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getStringArg(eq(environment), eq("url")))
                    .thenReturn(URL);
            when(graphQLUtil.getLongArg(eq(environment), eq("typeId")))
                    .thenReturn(TYPE_ID);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("primary")))
                    .thenReturn(PRIMARY);

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
    public void getWebsitesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Website> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Website entity = mock(Website.class);
                    entity.setWebsiteId(value);
                    availableObjects.add(entity);
                });
        List<Website> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getWebsites(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<Website> results = resolvers.getWebsitesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getWebsitesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Website> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Website entity = mock(Website.class);
                    entity.setWebsiteId(value);
                    availableObjects.add(entity);
                });
        List<Website> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getWebsites(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<Website> results = resolvers.getWebsitesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getWebsitesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Website> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Website entity = mock(Website.class);
                    entity.setWebsiteId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getWebsites(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Website> results = resolvers.getWebsitesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getWebsitesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Website> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getWebsites(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Website> results = resolvers.getWebsitesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getWebsiteDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Website expectedResult = mock(Website.class);
        expectedResult.setWebsiteId(WEBSITE_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("websiteId"))
                .thenReturn(WEBSITE_ID);
        when(dataLoader.load(WEBSITE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<Website> asyncResult = resolvers.getWebsiteDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Website result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getWebsiteDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("websiteId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<Website> asyncResult = resolvers.getWebsiteDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getWebsiteDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("websiteId"))
                .thenReturn(WEBSITE_ID);
        when(dataLoader.load(WEBSITE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<Website> asyncResult = resolvers.getWebsiteDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Website result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createWebsiteDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("url", URL);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Website expectedResult = mock(Website.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setUrl(URL);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addWebsite(eq(USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(URL), eq(TYPE_ID), eq(PRIMARY), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Website result = resolvers.createWebsiteDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createWebsiteDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("url", URL);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Website expectedResult = mock(Website.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setUrl(URL);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addWebsite(eq(DEFAULT_USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(URL), eq(TYPE_ID), eq(PRIMARY), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Website result = resolvers.createWebsiteDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createWebsiteDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addWebsite(anyLong(), anyString(), anyLong(), anyString(), anyLong(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Website result = resolvers.createWebsiteDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateWebsiteDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("websiteId", WEBSITE_ID);
        arguments.put("url", URL);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Website expectedResult = mock(Website.class);
        expectedResult.setWebsiteId(WEBSITE_ID);
        expectedResult.setUrl(URL);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateWebsite(eq(WEBSITE_ID), eq(URL), eq(TYPE_ID), eq(PRIMARY)))
                .thenReturn(expectedResult);

        // Asserts
        Website result = resolvers.updateWebsiteDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchWebsiteException.class)
    public void updateWebsiteDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("url", URL);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);

        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("websiteId")))
                .thenReturn(0L);
        when(localService.updateWebsite(eq(0L), eq(URL), eq(TYPE_ID), eq(PRIMARY)))
                .thenThrow(NoSuchWebsiteException.class);

        // Asserts
        Website result = resolvers.updateWebsiteDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchWebsiteException.class)
    public void updateWebsiteDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("websiteId", 789456L);
        arguments.put("url", URL);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("websiteId")))
                .thenReturn(789456L);
        when(localService.updateWebsite(eq(789456L), eq(URL), eq(TYPE_ID), eq(PRIMARY)))
                .thenThrow(NoSuchWebsiteException.class);

        // Asserts
        Website result = resolvers.updateWebsiteDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateWebsiteDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("websiteId")))
                .thenReturn(WEBSITE_ID);
        when(localService.updateWebsite(anyLong(), anyString(), anyLong(), anyBoolean()))
                .thenThrow(PortalException.class);

        // Asserts
        Website result = resolvers.updateWebsiteDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteWebsiteDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("websiteId", WEBSITE_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Website expectedResult = mock(Website.class);
        expectedResult.setWebsiteId(WEBSITE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteWebsite(eq(WEBSITE_ID)))
                .thenReturn(expectedResult);

        // Asserts
        Website result = resolvers.deleteWebsiteDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchWebsiteException.class)
    public void deleteWebsiteDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        Website expectedResult = mock(Website.class);
        expectedResult.setWebsiteId(WEBSITE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteWebsite(eq(WEBSITE_ID)))
                .thenThrow(NoSuchWebsiteException.class);

        // Asserts
        Website result = resolvers.deleteWebsiteDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchWebsiteException.class)
    public void deleteWebsiteDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("websiteId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Website expectedResult = mock(Website.class);
        expectedResult.setWebsiteId(WEBSITE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("websiteId")))
                .thenReturn(789456L);
        when(localService.deleteWebsite(eq(789456L)))
                .thenThrow(NoSuchWebsiteException.class);

        // Asserts
        Website result = resolvers.deleteWebsiteDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
