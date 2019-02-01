package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.blogs.exception.NoSuchEntryException;
import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.BlogsEntryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.BlogsEntryResolvers;
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
 * Test suite for {@link BlogsEntryResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class BlogsEntryResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long ENTRY_ID = 987L;
    private static final long USER_ID = 123L;
    private static final String TITLE = "Test blogs entry";
    private static final String SUBTITLE = "subtitle";
    private static final String URL_TITLE = "/test-blogs-entry";
    private static final String DESCRIPTION = "Test description";
    private static final String CONTENT = "Test content";
    private static final int DISPLAY_DATE_MONTH = 0;
    private static final int DISPLAY_DATE_DAY = 0;
    private static final int DISPLAY_DATE_YEAR = 0;
    private static final int DISPLAY_DATE_HOUR = 0;
    private static final int DISPLAY_DATE_MINUTE = 0;
    private static final boolean ALLOW_PINGBACKS = true;
    private static final boolean ALLOW_TRACKBACKS = true;
    private static final String[] TRACKBACKS = new String[]{"trackback 1", "trackback 2"};
    private static final String COVER_IMAGE_CAPTION = "Some caption";
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, BlogsEntry> dataLoader;

    @InjectMocks
    BlogsEntryResolvers resolvers = new BlogsEntryResolversImpl();

    @Mock
    private BlogsEntryLocalService localService;

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
                .getDataLoader(BlogsEntryBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((BlogsEntryResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("entryId")))
                    .thenReturn(ENTRY_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("title")))
                    .thenReturn(TITLE);
            when(graphQLUtil.getStringArg(eq(environment), eq("subtitle")))
                    .thenReturn(SUBTITLE);
            when(graphQLUtil.getStringArg(eq(environment), eq("urlTitle")))
                    .thenReturn(URL_TITLE);
            when(graphQLUtil.getStringArg(eq(environment), eq("description")))
                    .thenReturn(DESCRIPTION);
            when(graphQLUtil.getStringArg(eq(environment), eq("content")))
                    .thenReturn(CONTENT);
            when(graphQLUtil.getIntArg(eq(environment), eq("displayDateMonth")))
                    .thenReturn(DISPLAY_DATE_MONTH);
            when(graphQLUtil.getIntArg(eq(environment), eq("displayDateDay")))
                    .thenReturn(DISPLAY_DATE_DAY);
            when(graphQLUtil.getIntArg(eq(environment), eq("displayDateYear")))
                    .thenReturn(DISPLAY_DATE_YEAR);
            when(graphQLUtil.getIntArg(eq(environment), eq("displayDateHour")))
                    .thenReturn(DISPLAY_DATE_HOUR);
            when(graphQLUtil.getIntArg(eq(environment), eq("displayDateMinute")))
                    .thenReturn(DISPLAY_DATE_MINUTE);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("allowPingbacks")))
                    .thenReturn(ALLOW_PINGBACKS);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("allowTrackbacks")))
                    .thenReturn(ALLOW_TRACKBACKS);
            when(graphQLUtil.getStringArrayArg(eq(environment), eq("trackbacks")))
                    .thenReturn(TRACKBACKS);
            when(graphQLUtil.getStringArg(eq(environment), eq("coverImageCaption")))
                    .thenReturn(COVER_IMAGE_CAPTION);
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
    public void getBlogsEntriesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<BlogsEntry> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    BlogsEntry entity = mock(BlogsEntry.class);
                    entity.setEntryId(value);
                    availableObjects.add(entity);
                });
        List<BlogsEntry> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getBlogsEntries(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<BlogsEntry> results = resolvers.getBlogsEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getBlogsEntriesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<BlogsEntry> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    BlogsEntry entity = mock(BlogsEntry.class);
                    entity.setEntryId(value);
                    availableObjects.add(entity);
                });
        List<BlogsEntry> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getBlogsEntries(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<BlogsEntry> results = resolvers.getBlogsEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getBlogsEntriesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<BlogsEntry> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    BlogsEntry entity = mock(BlogsEntry.class);
                    entity.setEntryId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getBlogsEntries(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<BlogsEntry> results = resolvers.getBlogsEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getBlogsEntriesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<BlogsEntry> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getBlogsEntries(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<BlogsEntry> results = resolvers.getBlogsEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getBlogsEntryDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        BlogsEntry expectedResult = mock(BlogsEntry.class);
        expectedResult.setEntryId(ENTRY_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("entryId"))
                .thenReturn(ENTRY_ID);
        when(dataLoader.load(ENTRY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<BlogsEntry> asyncResult = resolvers.getBlogsEntryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        BlogsEntry result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getBlogsEntryDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("entryId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<BlogsEntry> asyncResult = resolvers.getBlogsEntryDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getBlogsEntryDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("entryId"))
                .thenReturn(ENTRY_ID);
        when(dataLoader.load(ENTRY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<BlogsEntry> asyncResult = resolvers.getBlogsEntryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        BlogsEntry result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createBlogsEntryDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("title", TITLE);
        arguments.put("subtitle", SUBTITLE);
        arguments.put("urlTitle", URL_TITLE);
        arguments.put("description", DESCRIPTION);
        arguments.put("content", CONTENT);
        arguments.put("displayDateMonth", DISPLAY_DATE_MONTH);
        arguments.put("displayDateDay", DISPLAY_DATE_DAY);
        arguments.put("displayDateYear", DISPLAY_DATE_YEAR);
        arguments.put("displayDateHour", DISPLAY_DATE_HOUR);
        arguments.put("displayDateMinute", DISPLAY_DATE_MINUTE);
        arguments.put("allowPingbacks", ALLOW_PINGBACKS);
        arguments.put("allowTrackbacks", ALLOW_TRACKBACKS);
        arguments.put("trackbacks", TRACKBACKS);
        arguments.put("coverImageCaption", COVER_IMAGE_CAPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        BlogsEntry expectedResult = mock(BlogsEntry.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setTitle(TITLE);
        expectedResult.setSubtitle(SUBTITLE);
        expectedResult.setUrlTitle(URL_TITLE);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setContent(CONTENT);
        expectedResult.setAllowPingbacks(ALLOW_PINGBACKS);
        expectedResult.setAllowTrackbacks(ALLOW_TRACKBACKS);
        expectedResult.setTrackbacks(TRACKBACKS[0]);
        expectedResult.setCoverImageCaption(COVER_IMAGE_CAPTION);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addEntry(eq(USER_ID), eq(TITLE), eq(SUBTITLE), eq(URL_TITLE), eq(DESCRIPTION), eq(CONTENT), eq(DISPLAY_DATE_MONTH), eq(DISPLAY_DATE_DAY), eq(DISPLAY_DATE_YEAR), eq(DISPLAY_DATE_HOUR), eq(DISPLAY_DATE_MINUTE), eq(ALLOW_PINGBACKS), eq(ALLOW_TRACKBACKS), eq(TRACKBACKS), eq(COVER_IMAGE_CAPTION), any(), any(), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        BlogsEntry result = resolvers.createBlogsEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createBlogsEntryDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("title", TITLE);
        arguments.put("subtitle", SUBTITLE);
        arguments.put("urlTitle", URL_TITLE);
        arguments.put("description", DESCRIPTION);
        arguments.put("content", CONTENT);
        arguments.put("displayDateMonth", DISPLAY_DATE_MONTH);
        arguments.put("displayDateDay", DISPLAY_DATE_DAY);
        arguments.put("displayDateYear", DISPLAY_DATE_YEAR);
        arguments.put("displayDateHour", DISPLAY_DATE_HOUR);
        arguments.put("displayDateMinute", DISPLAY_DATE_MINUTE);
        arguments.put("allowPingbacks", ALLOW_PINGBACKS);
        arguments.put("allowTrackbacks", ALLOW_TRACKBACKS);
        arguments.put("trackbacks", TRACKBACKS);
        arguments.put("coverImageCaption", COVER_IMAGE_CAPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        BlogsEntry expectedResult = mock(BlogsEntry.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setTitle(TITLE);
        expectedResult.setSubtitle(SUBTITLE);
        expectedResult.setUrlTitle(URL_TITLE);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setContent(CONTENT);
        expectedResult.setAllowPingbacks(ALLOW_PINGBACKS);
        expectedResult.setAllowTrackbacks(ALLOW_TRACKBACKS);
        expectedResult.setTrackbacks(TRACKBACKS[0]);
        expectedResult.setCoverImageCaption(COVER_IMAGE_CAPTION);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addEntry(eq(DEFAULT_USER_ID), eq(TITLE), eq(SUBTITLE), eq(URL_TITLE), eq(DESCRIPTION), eq(CONTENT), eq(DISPLAY_DATE_MONTH), eq(DISPLAY_DATE_DAY), eq(DISPLAY_DATE_YEAR), eq(DISPLAY_DATE_HOUR), eq(DISPLAY_DATE_MINUTE), eq(ALLOW_PINGBACKS), eq(ALLOW_TRACKBACKS), eq(TRACKBACKS), eq(COVER_IMAGE_CAPTION), any(), any(), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        BlogsEntry result = resolvers.createBlogsEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createBlogsEntryDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addEntry(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), anyBoolean(), any(), anyString(), any(), any(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        BlogsEntry result = resolvers.createBlogsEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateBlogsEntryDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("entryId", ENTRY_ID);
        arguments.put("userId", USER_ID);
        arguments.put("title", TITLE);
        arguments.put("subtitle", SUBTITLE);
        arguments.put("urlTitle", URL_TITLE);
        arguments.put("description", DESCRIPTION);
        arguments.put("content", CONTENT);
        arguments.put("displayDateMonth", DISPLAY_DATE_MONTH);
        arguments.put("displayDateDay", DISPLAY_DATE_DAY);
        arguments.put("displayDateYear", DISPLAY_DATE_YEAR);
        arguments.put("displayDateHour", DISPLAY_DATE_HOUR);
        arguments.put("displayDateMinute", DISPLAY_DATE_MINUTE);
        arguments.put("allowPingbacks", ALLOW_PINGBACKS);
        arguments.put("allowTrackbacks", ALLOW_TRACKBACKS);
        arguments.put("trackbacks", TRACKBACKS);
        arguments.put("coverImageCaption", COVER_IMAGE_CAPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        BlogsEntry expectedResult = mock(BlogsEntry.class);
        expectedResult.setEntryId(ENTRY_ID);
        expectedResult.setTitle(TITLE);
        expectedResult.setSubtitle(SUBTITLE);
        expectedResult.setUrlTitle(URL_TITLE);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setContent(CONTENT);
        expectedResult.setAllowPingbacks(ALLOW_PINGBACKS);
        expectedResult.setAllowTrackbacks(ALLOW_TRACKBACKS);
        expectedResult.setTrackbacks(TRACKBACKS[0]);
        expectedResult.setCoverImageCaption(COVER_IMAGE_CAPTION);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateEntry(eq(USER_ID), eq(ENTRY_ID), eq(TITLE), eq(SUBTITLE), eq(URL_TITLE), eq(DESCRIPTION), eq(CONTENT), eq(DISPLAY_DATE_MONTH), eq(DISPLAY_DATE_DAY), eq(DISPLAY_DATE_YEAR), eq(DISPLAY_DATE_HOUR), eq(DISPLAY_DATE_MINUTE), eq(ALLOW_PINGBACKS), eq(ALLOW_TRACKBACKS), eq(TRACKBACKS), eq(COVER_IMAGE_CAPTION), any(), any(), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        BlogsEntry result = resolvers.updateBlogsEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchEntryException.class)
    public void updateBlogsEntryDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("title", TITLE);
        arguments.put("subtitle", SUBTITLE);
        arguments.put("urlTitle", URL_TITLE);
        arguments.put("description", DESCRIPTION);
        arguments.put("content", CONTENT);
        arguments.put("displayDateMonth", DISPLAY_DATE_MONTH);
        arguments.put("displayDateDay", DISPLAY_DATE_DAY);
        arguments.put("displayDateYear", DISPLAY_DATE_YEAR);
        arguments.put("displayDateHour", DISPLAY_DATE_HOUR);
        arguments.put("displayDateMinute", DISPLAY_DATE_MINUTE);
        arguments.put("allowPingbacks", ALLOW_PINGBACKS);
        arguments.put("allowTrackbacks", ALLOW_TRACKBACKS);
        arguments.put("trackbacks", TRACKBACKS);
        arguments.put("coverImageCaption", COVER_IMAGE_CAPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("entryId")))
                .thenReturn(0L);
        when(localService.updateEntry(eq(DEFAULT_USER_ID), eq(0L), eq(TITLE), eq(SUBTITLE), eq(URL_TITLE), eq(DESCRIPTION), eq(CONTENT), eq(DISPLAY_DATE_MONTH), eq(DISPLAY_DATE_DAY), eq(DISPLAY_DATE_YEAR), eq(DISPLAY_DATE_HOUR), eq(DISPLAY_DATE_MINUTE), eq(ALLOW_PINGBACKS), eq(ALLOW_TRACKBACKS), eq(TRACKBACKS), eq(COVER_IMAGE_CAPTION), any(), any(), any(ServiceContext.class)))
                .thenThrow(NoSuchEntryException.class);

        // Asserts
        BlogsEntry result = resolvers.updateBlogsEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchEntryException.class)
    public void updateBlogsEntryDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("entryId", 789456L);
        arguments.put("userId", USER_ID);
        arguments.put("title", TITLE);
        arguments.put("subtitle", SUBTITLE);
        arguments.put("urlTitle", URL_TITLE);
        arguments.put("description", DESCRIPTION);
        arguments.put("content", CONTENT);
        arguments.put("displayDateMonth", DISPLAY_DATE_MONTH);
        arguments.put("displayDateDay", DISPLAY_DATE_DAY);
        arguments.put("displayDateYear", DISPLAY_DATE_YEAR);
        arguments.put("displayDateHour", DISPLAY_DATE_HOUR);
        arguments.put("displayDateMinute", DISPLAY_DATE_MINUTE);
        arguments.put("allowPingbacks", ALLOW_PINGBACKS);
        arguments.put("allowTrackbacks", ALLOW_TRACKBACKS);
        arguments.put("trackbacks", TRACKBACKS);
        arguments.put("coverImageCaption", COVER_IMAGE_CAPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("entryId")))
                .thenReturn(789456L);
        when(localService.updateEntry(eq(DEFAULT_USER_ID), eq(789456L), eq(TITLE), eq(SUBTITLE), eq(URL_TITLE), eq(DESCRIPTION), eq(CONTENT), eq(DISPLAY_DATE_MONTH), eq(DISPLAY_DATE_DAY), eq(DISPLAY_DATE_YEAR), eq(DISPLAY_DATE_HOUR), eq(DISPLAY_DATE_MINUTE), eq(ALLOW_PINGBACKS), eq(ALLOW_TRACKBACKS), eq(TRACKBACKS), eq(COVER_IMAGE_CAPTION), any(), any(), any(ServiceContext.class)))
                .thenThrow(NoSuchEntryException.class);

        // Asserts
        BlogsEntry result = resolvers.updateBlogsEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateBlogsEntryDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("entryId")))
                .thenReturn(ENTRY_ID);
        when(localService.updateEntry(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), anyBoolean(), any(), anyString(), any(), any(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        BlogsEntry result = resolvers.updateBlogsEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteBlogsEntryDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("entryId", ENTRY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        BlogsEntry expectedResult = mock(BlogsEntry.class);
        expectedResult.setEntryId(ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteBlogsEntry(eq(ENTRY_ID)))
                .thenReturn(expectedResult);

        // Asserts
        BlogsEntry result = resolvers.deleteBlogsEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchEntryException.class)
    public void deleteBlogsEntryDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        BlogsEntry expectedResult = mock(BlogsEntry.class);
        expectedResult.setEntryId(ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteBlogsEntry(eq(ENTRY_ID)))
                .thenThrow(NoSuchEntryException.class);

        // Asserts
        BlogsEntry result = resolvers.deleteBlogsEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchEntryException.class)
    public void deleteBlogsEntryDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("entryId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        BlogsEntry expectedResult = mock(BlogsEntry.class);
        expectedResult.setEntryId(ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("entryId")))
                .thenReturn(789456L);
        when(localService.deleteBlogsEntry(eq(789456L)))
                .thenThrow(NoSuchEntryException.class);

        // Asserts
        BlogsEntry result = resolvers.deleteBlogsEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
