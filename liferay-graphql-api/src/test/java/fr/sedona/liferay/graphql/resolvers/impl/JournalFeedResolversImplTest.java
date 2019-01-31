package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.journal.exception.NoSuchFeedException;
import com.liferay.journal.model.JournalFeed;
import com.liferay.journal.service.JournalFeedLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.JournalFeedBatchLoader;
import fr.sedona.liferay.graphql.resolvers.JournalFeedResolvers;
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
 * Test suite for {@link JournalFeedResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class JournalFeedResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final String FEED_ID = "FEED-ID";
    private static final boolean AUTO_FEED_ID = false;
    private static final String NAME = "Feed 1";
    private static final String DESCRIPTION = "Description";
    private static final String DDM_STRUCTURE_KEY = "FEED-STRUCTURE";
    private static final String DDM_TEMPLATE_KEY = "FEED-TEMPLATE";
    private static final String DDM_RENDERER_TEMPLATE_KEY = "FEED-RENDERER-TEMPLATE";
    private static final int DELTA = 10;
    private static final String ORDER_BY_COL = "col1";
    private static final String ORDER_BY_TYPE = "ASC";
    private static final String TARGET_LAYOUT_FRIENDLY_URL = "/test";
    private static final String TARGET_PORTLET_ID = "fr.sedona.somePortletId";
    private static final String CONTENT_FIELD = "field";
    private static final String FEED_FORMAT = "format";
    private static final double FEED_VERSION = 1.0;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, JournalFeed> dataLoader;

    @InjectMocks
    JournalFeedResolvers resolvers = new JournalFeedResolversImpl();

    @Mock
    private JournalFeedLocalService localService;

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
                .getDataLoader(JournalFeedBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((JournalFeedResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("id")))
                    .thenReturn(ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("feedId")))
                    .thenReturn(FEED_ID);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("autoFeedId")))
                    .thenReturn(AUTO_FEED_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("description")))
                    .thenReturn(DESCRIPTION);
            when(graphQLUtil.getStringArg(eq(environment), eq("ddmStructureKey")))
                    .thenReturn(DDM_STRUCTURE_KEY);
            when(graphQLUtil.getStringArg(eq(environment), eq("ddmTemplateKey")))
                    .thenReturn(DDM_TEMPLATE_KEY);
            when(graphQLUtil.getStringArg(eq(environment), eq("ddmRendererTemplateKey")))
                    .thenReturn(DDM_RENDERER_TEMPLATE_KEY);
            when(graphQLUtil.getIntArg(eq(environment), eq("delta")))
                    .thenReturn(DELTA);
            when(graphQLUtil.getStringArg(eq(environment), eq("orderByCol")))
                    .thenReturn(ORDER_BY_COL);
            when(graphQLUtil.getStringArg(eq(environment), eq("orderByType")))
                    .thenReturn(ORDER_BY_TYPE);
            when(graphQLUtil.getStringArg(eq(environment), eq("targetLayoutFriendlyUrl")))
                    .thenReturn(TARGET_LAYOUT_FRIENDLY_URL);
            when(graphQLUtil.getStringArg(eq(environment), eq("targetPortletId")))
                    .thenReturn(TARGET_PORTLET_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("contentField")))
                    .thenReturn(CONTENT_FIELD);
            when(graphQLUtil.getStringArg(eq(environment), eq("feedFormat")))
                    .thenReturn(FEED_FORMAT);
            when(graphQLUtil.getDoubleArg(eq(environment), eq("feedVersion")))
                    .thenReturn(FEED_VERSION);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getIntArg(eq(environment), anyString()))
                    .thenReturn(0);
            when(graphQLUtil.getDoubleArg(eq(environment), anyString()))
                    .thenReturn(0.0);
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
    public void getJournalFeedsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<JournalFeed> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    JournalFeed entity = mock(JournalFeed.class);
                    entity.setId(value);
                    availableObjects.add(entity);
                });
        List<JournalFeed> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalFeeds(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalFeed> results = resolvers.getJournalFeedsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalFeedsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<JournalFeed> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    JournalFeed entity = mock(JournalFeed.class);
                    entity.setId(value);
                    availableObjects.add(entity);
                });
        List<JournalFeed> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalFeeds(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalFeed> results = resolvers.getJournalFeedsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalFeedsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<JournalFeed> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    JournalFeed entity = mock(JournalFeed.class);
                    entity.setId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalFeeds(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalFeed> results = resolvers.getJournalFeedsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalFeedsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<JournalFeed> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalFeeds(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalFeed> results = resolvers.getJournalFeedsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalFeedDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        JournalFeed expectedResult = mock(JournalFeed.class);
        expectedResult.setId(ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("id"))
                .thenReturn(ID);
        when(dataLoader.load(ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<JournalFeed> asyncResult = resolvers.getJournalFeedDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        JournalFeed result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getJournalFeedDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("id"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<JournalFeed> asyncResult = resolvers.getJournalFeedDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getJournalFeedDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("id"))
                .thenReturn(ID);
        when(dataLoader.load(ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<JournalFeed> asyncResult = resolvers.getJournalFeedDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        JournalFeed result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createJournalFeedDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("feedId", FEED_ID);
        arguments.put("autoFeedId", AUTO_FEED_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("ddmStructureKey", DDM_STRUCTURE_KEY);
        arguments.put("ddmTemplateKey", DDM_TEMPLATE_KEY);
        arguments.put("ddmRendererTemplateKey", DDM_RENDERER_TEMPLATE_KEY);
        arguments.put("delta", DELTA);
        arguments.put("orderByCol", ORDER_BY_COL);
        arguments.put("orderByType", ORDER_BY_TYPE);
        arguments.put("targetLayoutFriendlyUrl", TARGET_LAYOUT_FRIENDLY_URL);
        arguments.put("targetPortletId", TARGET_PORTLET_ID);
        arguments.put("contentField", CONTENT_FIELD);
        arguments.put("feedFormat", FEED_FORMAT);
        arguments.put("feedVersion", FEED_VERSION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalFeed expectedResult = mock(JournalFeed.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setFeedId(FEED_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setDDMStructureKey(DDM_STRUCTURE_KEY);
        expectedResult.setDDMTemplateKey(DDM_TEMPLATE_KEY);
        expectedResult.setDDMRendererTemplateKey(DDM_RENDERER_TEMPLATE_KEY);
        expectedResult.setDelta(DELTA);
        expectedResult.setOrderByCol(ORDER_BY_COL);
        expectedResult.setOrderByType(ORDER_BY_TYPE);
        expectedResult.setTargetLayoutFriendlyUrl(TARGET_LAYOUT_FRIENDLY_URL);
        expectedResult.setTargetPortletId(TARGET_PORTLET_ID);
        expectedResult.setContentField(CONTENT_FIELD);
        expectedResult.setFeedFormat(FEED_FORMAT);
        expectedResult.setFeedVersion(FEED_VERSION);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addFeed(eq(USER_ID), eq(GROUP_ID), eq(FEED_ID), eq(AUTO_FEED_ID), eq(NAME), eq(DESCRIPTION), eq(DDM_STRUCTURE_KEY), eq(DDM_TEMPLATE_KEY), eq(DDM_RENDERER_TEMPLATE_KEY), eq(DELTA), eq(ORDER_BY_COL), eq(ORDER_BY_TYPE), eq(TARGET_LAYOUT_FRIENDLY_URL), eq(TARGET_PORTLET_ID), eq(CONTENT_FIELD), eq(FEED_FORMAT), eq(FEED_VERSION), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        JournalFeed result = resolvers.createJournalFeedDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createJournalFeedDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("feedId", FEED_ID);
        arguments.put("autoFeedId", AUTO_FEED_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("ddmStructureKey", DDM_STRUCTURE_KEY);
        arguments.put("ddmTemplateKey", DDM_TEMPLATE_KEY);
        arguments.put("ddmRendererTemplateKey", DDM_RENDERER_TEMPLATE_KEY);
        arguments.put("delta", DELTA);
        arguments.put("orderByCol", ORDER_BY_COL);
        arguments.put("orderByType", ORDER_BY_TYPE);
        arguments.put("targetLayoutFriendlyUrl", TARGET_LAYOUT_FRIENDLY_URL);
        arguments.put("targetPortletId", TARGET_PORTLET_ID);
        arguments.put("contentField", CONTENT_FIELD);
        arguments.put("feedFormat", FEED_FORMAT);
        arguments.put("feedVersion", FEED_VERSION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalFeed expectedResult = mock(JournalFeed.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setFeedId(FEED_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setDDMStructureKey(DDM_STRUCTURE_KEY);
        expectedResult.setDDMTemplateKey(DDM_TEMPLATE_KEY);
        expectedResult.setDDMRendererTemplateKey(DDM_RENDERER_TEMPLATE_KEY);
        expectedResult.setDelta(DELTA);
        expectedResult.setOrderByCol(ORDER_BY_COL);
        expectedResult.setOrderByType(ORDER_BY_TYPE);
        expectedResult.setTargetLayoutFriendlyUrl(TARGET_LAYOUT_FRIENDLY_URL);
        expectedResult.setTargetPortletId(TARGET_PORTLET_ID);
        expectedResult.setContentField(CONTENT_FIELD);
        expectedResult.setFeedFormat(FEED_FORMAT);
        expectedResult.setFeedVersion(FEED_VERSION);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addFeed(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(FEED_ID), eq(AUTO_FEED_ID), eq(NAME), eq(DESCRIPTION), eq(DDM_STRUCTURE_KEY), eq(DDM_TEMPLATE_KEY), eq(DDM_RENDERER_TEMPLATE_KEY), eq(DELTA), eq(ORDER_BY_COL), eq(ORDER_BY_TYPE), eq(TARGET_LAYOUT_FRIENDLY_URL), eq(TARGET_PORTLET_ID), eq(CONTENT_FIELD), eq(FEED_FORMAT), eq(FEED_VERSION), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        JournalFeed result = resolvers.createJournalFeedDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createJournalFeedDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addFeed(anyLong(), anyLong(), anyString(), anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyDouble(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        JournalFeed result = resolvers.createJournalFeedDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateJournalFeedDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("feedId", FEED_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("ddmStructureKey", DDM_STRUCTURE_KEY);
        arguments.put("ddmTemplateKey", DDM_TEMPLATE_KEY);
        arguments.put("ddmRendererTemplateKey", DDM_RENDERER_TEMPLATE_KEY);
        arguments.put("delta", DELTA);
        arguments.put("orderByCol", ORDER_BY_COL);
        arguments.put("orderByType", ORDER_BY_TYPE);
        arguments.put("targetLayoutFriendlyUrl", TARGET_LAYOUT_FRIENDLY_URL);
        arguments.put("targetPortletId", TARGET_PORTLET_ID);
        arguments.put("contentField", CONTENT_FIELD);
        arguments.put("feedFormat", FEED_FORMAT);
        arguments.put("feedVersion", FEED_VERSION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalFeed expectedResult = mock(JournalFeed.class);
        expectedResult.setId(ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setFeedId(FEED_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setDDMStructureKey(DDM_STRUCTURE_KEY);
        expectedResult.setDDMTemplateKey(DDM_TEMPLATE_KEY);
        expectedResult.setDDMRendererTemplateKey(DDM_RENDERER_TEMPLATE_KEY);
        expectedResult.setDelta(DELTA);
        expectedResult.setOrderByCol(ORDER_BY_COL);
        expectedResult.setOrderByType(ORDER_BY_TYPE);
        expectedResult.setTargetLayoutFriendlyUrl(TARGET_LAYOUT_FRIENDLY_URL);
        expectedResult.setTargetPortletId(TARGET_PORTLET_ID);
        expectedResult.setContentField(CONTENT_FIELD);
        expectedResult.setFeedFormat(FEED_FORMAT);
        expectedResult.setFeedVersion(FEED_VERSION);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateFeed(eq(GROUP_ID), eq(FEED_ID), eq(NAME), eq(DESCRIPTION), eq(DDM_STRUCTURE_KEY), eq(DDM_TEMPLATE_KEY), eq(DDM_RENDERER_TEMPLATE_KEY), eq(DELTA), eq(ORDER_BY_COL), eq(ORDER_BY_TYPE), eq(TARGET_LAYOUT_FRIENDLY_URL), eq(TARGET_PORTLET_ID), eq(CONTENT_FIELD), eq(FEED_FORMAT), eq(FEED_VERSION), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        JournalFeed result = resolvers.updateJournalFeedDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFeedException.class)
    public void updateJournalFeedDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("ddmStructureKey", DDM_STRUCTURE_KEY);
        arguments.put("ddmTemplateKey", DDM_TEMPLATE_KEY);
        arguments.put("ddmRendererTemplateKey", DDM_RENDERER_TEMPLATE_KEY);
        arguments.put("delta", DELTA);
        arguments.put("orderByCol", ORDER_BY_COL);
        arguments.put("orderByType", ORDER_BY_TYPE);
        arguments.put("targetLayoutFriendlyUrl", TARGET_LAYOUT_FRIENDLY_URL);
        arguments.put("targetPortletId", TARGET_PORTLET_ID);
        arguments.put("contentField", CONTENT_FIELD);
        arguments.put("feedFormat", FEED_FORMAT);
        arguments.put("feedVersion", FEED_VERSION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getStringArg(eq(environment), eq("feedId")))
                .thenReturn("");
        when(localService.updateFeed(eq(GROUP_ID), eq(""), eq(NAME), eq(DESCRIPTION), eq(DDM_STRUCTURE_KEY), eq(DDM_TEMPLATE_KEY), eq(DDM_RENDERER_TEMPLATE_KEY), eq(DELTA), eq(ORDER_BY_COL), eq(ORDER_BY_TYPE), eq(TARGET_LAYOUT_FRIENDLY_URL), eq(TARGET_PORTLET_ID), eq(CONTENT_FIELD), eq(FEED_FORMAT), eq(FEED_VERSION), any(ServiceContext.class)))
                .thenThrow(NoSuchFeedException.class);

        // Asserts
        JournalFeed result = resolvers.updateJournalFeedDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFeedException.class)
    public void updateJournalFeedDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("feedId", "FakeId");
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("ddmStructureKey", DDM_STRUCTURE_KEY);
        arguments.put("ddmTemplateKey", DDM_TEMPLATE_KEY);
        arguments.put("ddmRendererTemplateKey", DDM_RENDERER_TEMPLATE_KEY);
        arguments.put("delta", DELTA);
        arguments.put("orderByCol", ORDER_BY_COL);
        arguments.put("orderByType", ORDER_BY_TYPE);
        arguments.put("targetLayoutFriendlyUrl", TARGET_LAYOUT_FRIENDLY_URL);
        arguments.put("targetPortletId", TARGET_PORTLET_ID);
        arguments.put("contentField", CONTENT_FIELD);
        arguments.put("feedFormat", FEED_FORMAT);
        arguments.put("feedVersion", FEED_VERSION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getStringArg(eq(environment), eq("feedId")))
                .thenReturn("FakeId");
        when(localService.updateFeed(eq(GROUP_ID), eq("FakeId"), eq(NAME), eq(DESCRIPTION), eq(DDM_STRUCTURE_KEY), eq(DDM_TEMPLATE_KEY), eq(DDM_RENDERER_TEMPLATE_KEY), eq(DELTA), eq(ORDER_BY_COL), eq(ORDER_BY_TYPE), eq(TARGET_LAYOUT_FRIENDLY_URL), eq(TARGET_PORTLET_ID), eq(CONTENT_FIELD), eq(FEED_FORMAT), eq(FEED_VERSION), any(ServiceContext.class)))
                .thenThrow(NoSuchFeedException.class);

        // Asserts
        JournalFeed result = resolvers.updateJournalFeedDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateJournalFeedDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("id")))
                .thenReturn(ID);
        when(localService.updateFeed(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyDouble(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        JournalFeed result = resolvers.updateJournalFeedDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteJournalFeedDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("id", ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalFeed expectedResult = mock(JournalFeed.class);
        expectedResult.setId(ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteJournalFeed(eq(ID)))
                .thenReturn(expectedResult);

        // Asserts
        JournalFeed result = resolvers.deleteJournalFeedDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFeedException.class)
    public void deleteJournalFeedDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        JournalFeed expectedResult = mock(JournalFeed.class);
        expectedResult.setId(ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteJournalFeed(eq(ID)))
                .thenThrow(NoSuchFeedException.class);

        // Asserts
        JournalFeed result = resolvers.deleteJournalFeedDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFeedException.class)
    public void deleteJournalFeedDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("id", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalFeed expectedResult = mock(JournalFeed.class);
        expectedResult.setId(ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("id")))
                .thenReturn(789456L);
        when(localService.deleteJournalFeed(eq(789456L)))
                .thenThrow(NoSuchFeedException.class);

        // Asserts
        JournalFeed result = resolvers.deleteJournalFeedDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
