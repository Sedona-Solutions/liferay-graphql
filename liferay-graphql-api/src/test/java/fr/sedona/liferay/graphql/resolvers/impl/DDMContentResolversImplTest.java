package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.dynamic.data.mapping.exception.NoSuchContentException;
import com.liferay.dynamic.data.mapping.model.DDMContent;
import com.liferay.dynamic.data.mapping.service.DDMContentLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.DDMContentBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DDMContentResolvers;
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
 * Test suite for {@link DDMContentResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class DDMContentResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long CONTENT_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final String NAME = "Content";
    private static final String DESCRIPTION = "Description";
    private static final String DATA = "Data";
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, DDMContent> dataLoader;

    @InjectMocks
    DDMContentResolvers resolvers = new DDMContentResolversImpl();

    @Mock
    private DDMContentLocalService localService;

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
                .getDataLoader(DDMContentBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((DDMContentResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("contentId")))
                    .thenReturn(CONTENT_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("description")))
                    .thenReturn(DESCRIPTION);
            when(graphQLUtil.getStringArg(eq(environment), eq("data")))
                    .thenReturn(DATA);
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
    public void getDDMContentsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DDMContent> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DDMContent entity = mock(DDMContent.class);
                    entity.setContentId(value);
                    availableObjects.add(entity);
                });
        List<DDMContent> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMContents(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMContent> results = resolvers.getDDMContentsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMContentsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<DDMContent> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DDMContent entity = mock(DDMContent.class);
                    entity.setContentId(value);
                    availableObjects.add(entity);
                });
        List<DDMContent> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMContents(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMContent> results = resolvers.getDDMContentsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMContentsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DDMContent> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DDMContent entity = mock(DDMContent.class);
                    entity.setContentId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMContents(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMContent> results = resolvers.getDDMContentsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMContentsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DDMContent> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMContents(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMContent> results = resolvers.getDDMContentsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMContentDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        DDMContent expectedResult = mock(DDMContent.class);
        expectedResult.setContentId(CONTENT_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("contentId"))
                .thenReturn(CONTENT_ID);
        when(dataLoader.load(CONTENT_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<DDMContent> asyncResult = resolvers.getDDMContentDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DDMContent result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getDDMContentDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("contentId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<DDMContent> asyncResult = resolvers.getDDMContentDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getDDMContentDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("contentId"))
                .thenReturn(CONTENT_ID);
        when(dataLoader.load(CONTENT_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<DDMContent> asyncResult = resolvers.getDDMContentDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DDMContent result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createDDMContentDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("data", DATA);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMContent expectedResult = mock(DDMContent.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setContentId(CONTENT_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setData(DATA);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addContent(eq(USER_ID), eq(GROUP_ID), eq(NAME), eq(DESCRIPTION), eq(DATA), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DDMContent result = resolvers.createDDMContentDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createDDMContentDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("data", DATA);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMContent expectedResult = mock(DDMContent.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setContentId(CONTENT_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setData(DATA);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addContent(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(NAME), eq(DESCRIPTION), eq(DATA), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DDMContent result = resolvers.createDDMContentDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createDDMContentDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addContent(anyLong(), anyLong(), anyString(), anyString(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DDMContent result = resolvers.createDDMContentDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateDDMContentDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("contentId", CONTENT_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("data", DATA);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMContent expectedResult = mock(DDMContent.class);
        expectedResult.setContentId(CONTENT_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setData(DATA);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateContent(eq(CONTENT_ID), eq(NAME), eq(DESCRIPTION), eq(DATA), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DDMContent result = resolvers.updateDDMContentDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchContentException.class)
    public void updateDDMContentDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("data", DATA);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("contentId")))
                .thenReturn(0L);
        when(localService.updateContent(eq(0L), eq(NAME), eq(DESCRIPTION), eq(DATA), any(ServiceContext.class)))
                .thenThrow(NoSuchContentException.class);

        // Asserts
        DDMContent result = resolvers.updateDDMContentDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchContentException.class)
    public void updateDDMContentDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("contentId", 789456L);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("data", DATA);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("contentId")))
                .thenReturn(789456L);
        when(localService.updateContent(eq(789456L), eq(NAME), eq(DESCRIPTION), eq(DATA), any(ServiceContext.class)))
                .thenThrow(NoSuchContentException.class);

        // Asserts
        DDMContent result = resolvers.updateDDMContentDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateDDMContentDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("contentId")))
                .thenReturn(CONTENT_ID);
        when(localService.updateContent(anyLong(), anyString(), anyString(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DDMContent result = resolvers.updateDDMContentDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteDDMContentDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("contentId", CONTENT_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMContent expectedResult = mock(DDMContent.class);
        expectedResult.setContentId(CONTENT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDDMContent(eq(CONTENT_ID)))
                .thenReturn(expectedResult);

        // Asserts
        DDMContent result = resolvers.deleteDDMContentDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchContentException.class)
    public void deleteDDMContentDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        DDMContent expectedResult = mock(DDMContent.class);
        expectedResult.setContentId(CONTENT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDDMContent(eq(CONTENT_ID)))
                .thenThrow(NoSuchContentException.class);

        // Asserts
        DDMContent result = resolvers.deleteDDMContentDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchContentException.class)
    public void deleteDDMContentDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("contentId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMContent expectedResult = mock(DDMContent.class);
        expectedResult.setContentId(CONTENT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("contentId")))
                .thenReturn(789456L);
        when(localService.deleteDDMContent(eq(789456L)))
                .thenThrow(NoSuchContentException.class);

        // Asserts
        DDMContent result = resolvers.deleteDDMContentDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
