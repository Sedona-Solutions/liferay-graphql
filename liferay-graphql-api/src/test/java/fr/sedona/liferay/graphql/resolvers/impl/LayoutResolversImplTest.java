package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchLayoutException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import fr.sedona.liferay.graphql.loaders.LayoutBatchLoader;
import fr.sedona.liferay.graphql.resolvers.LayoutResolvers;
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
 * Test suite for {@link LayoutResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class LayoutResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long LAYOUT_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final boolean PRIVATE_LAYOUT = true;
    private static final long PARENT_LAYOUT_ID = 0;
    private static final Map<Locale, String> NAME_MAP;
    private static final Map<Locale, String> TITLE_MAP;
    private static final Map<Locale, String> DESCRIPTION_MAP;
    private static final Map<Locale, String> KEYWORDS_MAP;
    private static final Map<Locale, String> ROBOTS_MAP;
    private static final String TYPE = "portlet";
    private static final String TYPE_SETTINGS = "some-settings";
    private static final boolean HIDDEN = false;
    private static final Map<Locale, String> FRIENDLY_URL_MAP;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, Layout> dataLoader;

    static {
        NAME_MAP = new HashMap<>();
        NAME_MAP.put(LocaleUtil.US, "Test name");
        NAME_MAP.put(LocaleUtil.FRANCE, "Nom de test");

        TITLE_MAP = new HashMap<>();
        TITLE_MAP.put(LocaleUtil.US, "Test title");
        TITLE_MAP.put(LocaleUtil.FRANCE, "Titre de test");

        DESCRIPTION_MAP = new HashMap<>();
        DESCRIPTION_MAP.put(LocaleUtil.US, "Test description");
        DESCRIPTION_MAP.put(LocaleUtil.FRANCE, "Description de test");

        KEYWORDS_MAP = new HashMap<>();
        KEYWORDS_MAP.put(LocaleUtil.US, "test");
        KEYWORDS_MAP.put(LocaleUtil.FRANCE, "test");

        ROBOTS_MAP = new HashMap<>();
        ROBOTS_MAP.put(LocaleUtil.US, "robots...");
        ROBOTS_MAP.put(LocaleUtil.FRANCE, "robots...");

        FRIENDLY_URL_MAP = new HashMap<>();
        FRIENDLY_URL_MAP.put(LocaleUtil.US, "/test-page");
        FRIENDLY_URL_MAP.put(LocaleUtil.FRANCE, "/page-de-test");
    }

    @InjectMocks
    LayoutResolvers resolvers = new LayoutResolversImpl();

    @Mock
    private LayoutLocalService localService;

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
                .getDataLoader(LayoutBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((LayoutResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("layoutId")))
                    .thenReturn(LAYOUT_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("privateLayout")))
                    .thenReturn(PRIVATE_LAYOUT);
            when(graphQLUtil.getLongArg(eq(environment), eq("parentLayoutId")))
                    .thenReturn(PARENT_LAYOUT_ID);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("nameMap")))
                    .thenReturn(NAME_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("titleMap")))
                    .thenReturn(TITLE_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("descriptionMap")))
                    .thenReturn(DESCRIPTION_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("keywordsMap")))
                    .thenReturn(KEYWORDS_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("robotsMap")))
                    .thenReturn(ROBOTS_MAP);
            when(graphQLUtil.getStringArg(eq(environment), eq("type")))
                    .thenReturn(TYPE);
            when(graphQLUtil.getStringArg(eq(environment), eq("typeSettings")))
                    .thenReturn(TYPE_SETTINGS);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("hidden")))
                    .thenReturn(HIDDEN);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("friendlyURLMap")))
                    .thenReturn(FRIENDLY_URL_MAP);
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
    public void getLayoutsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Layout> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Layout entity = mock(Layout.class);
                    entity.setLayoutId(value);
                    availableObjects.add(entity);
                });
        List<Layout> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getLayouts(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<Layout> results = resolvers.getLayoutsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getLayoutsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Layout> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Layout entity = mock(Layout.class);
                    entity.setLayoutId(value);
                    availableObjects.add(entity);
                });
        List<Layout> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getLayouts(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<Layout> results = resolvers.getLayoutsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getLayoutsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Layout> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Layout entity = mock(Layout.class);
                    entity.setLayoutId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getLayouts(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Layout> results = resolvers.getLayoutsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getLayoutsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Layout> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getLayouts(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Layout> results = resolvers.getLayoutsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getLayoutDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Layout expectedResult = mock(Layout.class);
        expectedResult.setLayoutId(LAYOUT_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("layoutId"))
                .thenReturn(LAYOUT_ID);
        when(dataLoader.load(LAYOUT_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<Layout> asyncResult = resolvers.getLayoutDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Layout result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getLayoutDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("layoutId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<Layout> asyncResult = resolvers.getLayoutDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getLayoutDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("layoutId"))
                .thenReturn(LAYOUT_ID);
        when(dataLoader.load(LAYOUT_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<Layout> asyncResult = resolvers.getLayoutDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Layout result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createLayoutDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("privateLayout", PRIVATE_LAYOUT);
        arguments.put("parentLayoutId", PARENT_LAYOUT_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("keywordsMap", KEYWORDS_MAP);
        arguments.put("robotsMap", ROBOTS_MAP);
        arguments.put("type", TYPE);
        arguments.put("typeSettings", TYPE_SETTINGS);
        arguments.put("hidden", HIDDEN);
        arguments.put("friendlyURLMap", FRIENDLY_URL_MAP);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Layout expectedResult = mock(Layout.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setPrivateLayout(PRIVATE_LAYOUT);
        expectedResult.setLayoutId(LAYOUT_ID);
        expectedResult.setParentLayoutId(PARENT_LAYOUT_ID);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setKeywordsMap(KEYWORDS_MAP);
        expectedResult.setRobotsMap(ROBOTS_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setTypeSettings(TYPE_SETTINGS);
        expectedResult.setHidden(HIDDEN);
        expectedResult.setFriendlyURL(FRIENDLY_URL_MAP.get(LocaleUtil.US));

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addLayout(eq(USER_ID), eq(GROUP_ID), eq(PRIVATE_LAYOUT), eq(PARENT_LAYOUT_ID), eq(NAME_MAP), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(KEYWORDS_MAP), eq(ROBOTS_MAP), eq(TYPE), eq(TYPE_SETTINGS), eq(HIDDEN), eq(FRIENDLY_URL_MAP), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Layout result = resolvers.createLayoutDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createLayoutDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("privateLayout", PRIVATE_LAYOUT);
        arguments.put("parentLayoutId", PARENT_LAYOUT_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("keywordsMap", KEYWORDS_MAP);
        arguments.put("robotsMap", ROBOTS_MAP);
        arguments.put("type", TYPE);
        arguments.put("typeSettings", TYPE_SETTINGS);
        arguments.put("hidden", HIDDEN);
        arguments.put("friendlyURLMap", FRIENDLY_URL_MAP);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Layout expectedResult = mock(Layout.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setPrivateLayout(PRIVATE_LAYOUT);
        expectedResult.setLayoutId(LAYOUT_ID);
        expectedResult.setParentLayoutId(PARENT_LAYOUT_ID);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setKeywordsMap(KEYWORDS_MAP);
        expectedResult.setRobotsMap(ROBOTS_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setTypeSettings(TYPE_SETTINGS);
        expectedResult.setHidden(HIDDEN);
        expectedResult.setFriendlyURL(FRIENDLY_URL_MAP.get(LocaleUtil.US));

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addLayout(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(PRIVATE_LAYOUT), eq(PARENT_LAYOUT_ID), eq(NAME_MAP), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(KEYWORDS_MAP), eq(ROBOTS_MAP), eq(TYPE), eq(TYPE_SETTINGS), eq(HIDDEN), eq(FRIENDLY_URL_MAP), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Layout result = resolvers.createLayoutDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createLayoutDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addLayout(anyLong(), anyLong(), anyBoolean(), anyLong(), anyMap(), anyMap(), anyMap(), anyMap(), anyMap(), anyString(), anyString(), anyBoolean(), anyMap(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Layout result = resolvers.createLayoutDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateLayoutDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("layoutId", LAYOUT_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("privateLayout", PRIVATE_LAYOUT);
        arguments.put("parentLayoutId", PARENT_LAYOUT_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("keywordsMap", KEYWORDS_MAP);
        arguments.put("robotsMap", ROBOTS_MAP);
        arguments.put("type", TYPE);
        arguments.put("typeSettings", TYPE_SETTINGS);
        arguments.put("hidden", HIDDEN);
        arguments.put("friendlyURLMap", FRIENDLY_URL_MAP);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Layout expectedResult = mock(Layout.class);
        expectedResult.setLayoutId(LAYOUT_ID);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setPrivateLayout(PRIVATE_LAYOUT);
        expectedResult.setLayoutId(LAYOUT_ID);
        expectedResult.setParentLayoutId(PARENT_LAYOUT_ID);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setKeywordsMap(KEYWORDS_MAP);
        expectedResult.setRobotsMap(ROBOTS_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setTypeSettings(TYPE_SETTINGS);
        expectedResult.setHidden(HIDDEN);
        expectedResult.setFriendlyURL(FRIENDLY_URL_MAP.get(LocaleUtil.US));

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateLayout(eq(GROUP_ID), eq(PRIVATE_LAYOUT), eq(LAYOUT_ID), eq(PARENT_LAYOUT_ID), eq(NAME_MAP), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(KEYWORDS_MAP), eq(ROBOTS_MAP), eq(TYPE), eq(HIDDEN), eq(FRIENDLY_URL_MAP), anyBoolean(), any(), any(ServiceContext.class)))
                .thenReturn(expectedResult);
        when(localService.updateLayout(eq(GROUP_ID), eq(PRIVATE_LAYOUT), eq(LAYOUT_ID), eq(TYPE_SETTINGS)))
                .thenReturn(expectedResult);

        // Asserts
        Layout result = resolvers.updateLayoutDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchLayoutException.class)
    public void updateLayoutDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("privateLayout", PRIVATE_LAYOUT);
        arguments.put("parentLayoutId", PARENT_LAYOUT_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("keywordsMap", KEYWORDS_MAP);
        arguments.put("robotsMap", ROBOTS_MAP);
        arguments.put("type", TYPE);
        arguments.put("typeSettings", TYPE_SETTINGS);
        arguments.put("hidden", HIDDEN);
        arguments.put("friendlyURLMap", FRIENDLY_URL_MAP);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("layoutId")))
                .thenReturn(0L);
        when(localService.updateLayout(eq(GROUP_ID), eq(PRIVATE_LAYOUT), eq(LAYOUT_ID), eq(PARENT_LAYOUT_ID), eq(NAME_MAP), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(KEYWORDS_MAP), eq(ROBOTS_MAP), eq(TYPE), eq(HIDDEN), eq(FRIENDLY_URL_MAP), anyBoolean(), any(), any(ServiceContext.class)))
                .thenThrow(NoSuchLayoutException.class);

        // Asserts
        Layout result = resolvers.updateLayoutDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchLayoutException.class)
    public void updateLayoutDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("layoutId", 789456L);
        arguments.put("groupId", GROUP_ID);
        arguments.put("privateLayout", PRIVATE_LAYOUT);
        arguments.put("parentLayoutId", PARENT_LAYOUT_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("keywordsMap", KEYWORDS_MAP);
        arguments.put("robotsMap", ROBOTS_MAP);
        arguments.put("type", TYPE);
        arguments.put("typeSettings", TYPE_SETTINGS);
        arguments.put("hidden", HIDDEN);
        arguments.put("friendlyURLMap", FRIENDLY_URL_MAP);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("layoutId")))
                .thenReturn(789456L);
        when(localService.updateLayout(eq(GROUP_ID), eq(PRIVATE_LAYOUT), eq(789456L), eq(PARENT_LAYOUT_ID), eq(NAME_MAP), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(KEYWORDS_MAP), eq(ROBOTS_MAP), eq(TYPE), eq(HIDDEN), eq(FRIENDLY_URL_MAP), anyBoolean(), any(), any(ServiceContext.class)))
                .thenThrow(NoSuchLayoutException.class);

        // Asserts
        Layout result = resolvers.updateLayoutDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateLayoutDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("layoutId")))
                .thenReturn(0L);
        when(localService.updateLayout(anyLong(), anyBoolean(), anyLong(), anyLong(), anyMap(), anyMap(), anyMap(), anyMap(), anyMap(), anyString(), anyBoolean(), anyMap(), anyBoolean(), any(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Layout result = resolvers.updateLayoutDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteLayoutDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("layoutId", LAYOUT_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Layout expectedResult = mock(Layout.class);
        expectedResult.setLayoutId(LAYOUT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteLayout(eq(LAYOUT_ID)))
                .thenReturn(expectedResult);

        // Asserts
        Layout result = resolvers.deleteLayoutDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchLayoutException.class)
    public void deleteLayoutDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        Layout expectedResult = mock(Layout.class);
        expectedResult.setLayoutId(LAYOUT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteLayout(eq(LAYOUT_ID)))
                .thenThrow(NoSuchLayoutException.class);

        // Asserts
        Layout result = resolvers.deleteLayoutDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchLayoutException.class)
    public void deleteLayoutDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("layoutId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Layout expectedResult = mock(Layout.class);
        expectedResult.setLayoutId(LAYOUT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("layoutId")))
                .thenReturn(789456L);
        when(localService.deleteLayout(eq(789456L)))
                .thenThrow(NoSuchLayoutException.class);

        // Asserts
        Layout result = resolvers.deleteLayoutDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
