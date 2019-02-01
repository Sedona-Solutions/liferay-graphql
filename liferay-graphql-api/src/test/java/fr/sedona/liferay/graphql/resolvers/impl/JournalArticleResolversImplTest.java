package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.journal.exception.NoSuchArticleException;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import fr.sedona.liferay.graphql.loaders.JournalArticleBatchLoader;
import fr.sedona.liferay.graphql.resolvers.JournalArticleResolvers;
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
 * Test suite for {@link JournalArticleResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class JournalArticleResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final long FOLDER_ID = 457L;
    private static final long CLASS_NAME_ID = 123L;
    private static final long CLASS_PK = 1L;
    private static final String ARTICLE_ID = "ARTICLE-ID";
    private static final boolean AUTO_ARTICLE_ID = false;
    private static final double VERSION = 1.0;
    private static final Map<Locale, String> TITLE_MAP;
    private static final Map<Locale, String> DESCRIPTION_MAP;
    private static final Map<Locale, String> FRIENDLY_URL_MAP;
    private static final String CONTENT = "content";
    private static final String DDM_STRUCTURE_KEY = "STRUCTURE-KEY";
    private static final String DDM_TEMPLATE_KEY = "TEMPLATE-KEY";
    private static final String LAYOUT_UUID = "";
    private static final int DISPLAY_DATE_MONTH = 0;
    private static final int DISPLAY_DATE_DAY = 0;
    private static final int DISPLAY_DATE_YEAR = 0;
    private static final int DISPLAY_DATE_HOUR = 0;
    private static final int DISPLAY_DATE_MINUTE = 0;
    private static final int EXPIRATION_DATE_MONTH = 0;
    private static final int EXPIRATION_DATE_DAY = 0;
    private static final int EXPIRATION_DATE_YEAR = 0;
    private static final int EXPIRATION_DATE_HOUR = 0;
    private static final int EXPIRATION_DATE_MINUTE = 0;
    private static final boolean NEVER_EXPIRE = true;
    private static final int REVIEW_DATE_MONTH = 0;
    private static final int REVIEW_DATE_DAY = 0;
    private static final int REVIEW_DATE_YEAR = 0;
    private static final int REVIEW_DATE_HOUR = 0;
    private static final int REVIEW_DATE_MINUTE = 0;
    private static final boolean NEVER_REVIEW = true;
    private static final boolean INDEXABLE = true;
    private static final String ARTICLE_URL = "/article-url";
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, JournalArticle> dataLoader;

    static {
        TITLE_MAP = new HashMap<>();
        TITLE_MAP.put(LocaleUtil.US, "Test title");
        TITLE_MAP.put(LocaleUtil.FRANCE, "Titre de test");

        DESCRIPTION_MAP = new HashMap<>();
        DESCRIPTION_MAP.put(LocaleUtil.US, "Test description");
        DESCRIPTION_MAP.put(LocaleUtil.FRANCE, "Description de test");

        FRIENDLY_URL_MAP = new HashMap<>();
        FRIENDLY_URL_MAP.put(LocaleUtil.US, "/test-title");
        FRIENDLY_URL_MAP.put(LocaleUtil.FRANCE, "/titre-de-test");
    }

    @InjectMocks
    JournalArticleResolvers resolvers = new JournalArticleResolversImpl();

    @Mock
    private JournalArticleLocalService localService;

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
                .getDataLoader(JournalArticleBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((JournalArticleResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("id")))
                    .thenReturn(ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                    .thenReturn(FOLDER_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("classNameId"), anyLong()))
                    .thenReturn(CLASS_NAME_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getStringArg(eq(environment), eq("articleId")))
                    .thenReturn(ARTICLE_ID);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("autoArticleId")))
                    .thenReturn(AUTO_ARTICLE_ID);
            when(graphQLUtil.getDoubleArg(eq(environment), eq("version"), anyDouble()))
                    .thenReturn(VERSION);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("titleMap")))
                    .thenReturn(TITLE_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("descriptionMap")))
                    .thenReturn(DESCRIPTION_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("friendlyURLMap")))
                    .thenReturn(FRIENDLY_URL_MAP);
            when(graphQLUtil.getStringArg(eq(environment), eq("content")))
                    .thenReturn(CONTENT);
            when(graphQLUtil.getStringArg(eq(environment), eq("ddmStructureKey")))
                    .thenReturn(DDM_STRUCTURE_KEY);
            when(graphQLUtil.getStringArg(eq(environment), eq("ddmTemplateKey")))
                    .thenReturn(DDM_TEMPLATE_KEY);
            when(graphQLUtil.getStringArg(eq(environment), eq("layoutUuid")))
                    .thenReturn(LAYOUT_UUID);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("neverExpire"), anyBoolean()))
                    .thenReturn(NEVER_EXPIRE);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("neverReview"), anyBoolean()))
                    .thenReturn(NEVER_REVIEW);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("indexable"), anyBoolean()))
                    .thenReturn(INDEXABLE);
            when(graphQLUtil.getStringArg(eq(environment), eq("articleURL")))
                    .thenReturn(ARTICLE_URL);
            when(graphQLUtil.getIntArg(eq(environment), anyString()))
                    .thenReturn(0);
        } else {
            when(graphQLUtil.getIntArg(eq(environment), anyString()))
                    .thenReturn(0);
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getLongArg(eq(environment), anyString(), anyLong()))
                    .thenReturn(0L);
            when(graphQLUtil.getDoubleArg(eq(environment), anyString(), anyDouble()))
                    .thenReturn(0.0);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString(), anyBoolean()))
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
    public void getJournalArticlesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<JournalArticle> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    JournalArticle entity = mock(JournalArticle.class);
                    entity.setId(value);
                    availableObjects.add(entity);
                });
        List<JournalArticle> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalArticles(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalArticle> results = resolvers.getJournalArticlesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalArticlesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<JournalArticle> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    JournalArticle entity = mock(JournalArticle.class);
                    entity.setId(value);
                    availableObjects.add(entity);
                });
        List<JournalArticle> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalArticles(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalArticle> results = resolvers.getJournalArticlesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalArticlesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<JournalArticle> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    JournalArticle entity = mock(JournalArticle.class);
                    entity.setId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalArticles(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalArticle> results = resolvers.getJournalArticlesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalArticlesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<JournalArticle> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalArticles(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalArticle> results = resolvers.getJournalArticlesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalArticleDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        JournalArticle expectedResult = mock(JournalArticle.class);
        expectedResult.setId(ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("id"))
                .thenReturn(ID);
        when(dataLoader.load(ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<JournalArticle> asyncResult = resolvers.getJournalArticleDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        JournalArticle result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getJournalArticleDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("id"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<JournalArticle> asyncResult = resolvers.getJournalArticleDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getJournalArticleDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("id"))
                .thenReturn(ID);
        when(dataLoader.load(ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<JournalArticle> asyncResult = resolvers.getJournalArticleDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        JournalArticle result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createJournalArticleDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("classNameId", CLASS_NAME_ID);
        arguments.put("classPK", CLASS_PK);
        arguments.put("articleId", ARTICLE_ID);
        arguments.put("autoArticleId", AUTO_ARTICLE_ID);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("friendlyURLMap", FRIENDLY_URL_MAP);
        arguments.put("content", CONTENT);
        arguments.put("ddmStructureKey", DDM_STRUCTURE_KEY);
        arguments.put("ddmTemplateKey", DDM_TEMPLATE_KEY);
        arguments.put("layoutUuid", LAYOUT_UUID);
        arguments.put("displayDateMonth", DISPLAY_DATE_MONTH);
        arguments.put("displayDateDay", DISPLAY_DATE_DAY);
        arguments.put("displayDateYear", DISPLAY_DATE_YEAR);
        arguments.put("displayDateHour", DISPLAY_DATE_HOUR);
        arguments.put("displayDateMinute", DISPLAY_DATE_MINUTE);
        arguments.put("expirationDateMonth", EXPIRATION_DATE_MONTH);
        arguments.put("expirationDateDay", EXPIRATION_DATE_DAY);
        arguments.put("expirationDateYear", EXPIRATION_DATE_YEAR);
        arguments.put("expirationDateHour", EXPIRATION_DATE_HOUR);
        arguments.put("expirationDateMinute", EXPIRATION_DATE_MINUTE);
        arguments.put("neverExpire", NEVER_EXPIRE);
        arguments.put("reviewDateMonth", REVIEW_DATE_MONTH);
        arguments.put("reviewDateDay", REVIEW_DATE_DAY);
        arguments.put("reviewDateYear", REVIEW_DATE_YEAR);
        arguments.put("reviewDateHour", REVIEW_DATE_HOUR);
        arguments.put("reviewDateMinute", REVIEW_DATE_MINUTE);
        arguments.put("neverReview", NEVER_REVIEW);
        arguments.put("indexable", INDEXABLE);
        arguments.put("articleURL", ARTICLE_URL);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalArticle expectedResult = mock(JournalArticle.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setClassNameId(CLASS_NAME_ID);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setArticleId(ARTICLE_ID);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setContent(CONTENT);
        expectedResult.setDDMStructureKey(DDM_STRUCTURE_KEY);
        expectedResult.setDDMTemplateKey(DDM_TEMPLATE_KEY);
        expectedResult.setLayoutUuid(LAYOUT_UUID);
        expectedResult.setIndexable(INDEXABLE);
        expectedResult.setUrlTitle(ARTICLE_URL);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addArticle(eq(USER_ID), eq(GROUP_ID), eq(FOLDER_ID), eq(CLASS_NAME_ID), eq(CLASS_PK), eq(ARTICLE_ID), eq(AUTO_ARTICLE_ID), anyDouble(), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(FRIENDLY_URL_MAP), eq(CONTENT), eq(DDM_STRUCTURE_KEY), eq(DDM_TEMPLATE_KEY), eq(LAYOUT_UUID), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(NEVER_EXPIRE), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(NEVER_REVIEW), eq(INDEXABLE), anyBoolean(), any(), any(), any(), eq(ARTICLE_URL), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        JournalArticle result = resolvers.createJournalArticleDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createJournalArticleDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("classNameId", CLASS_NAME_ID);
        arguments.put("classPK", CLASS_PK);
        arguments.put("articleId", ARTICLE_ID);
        arguments.put("autoArticleId", AUTO_ARTICLE_ID);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("friendlyURLMap", FRIENDLY_URL_MAP);
        arguments.put("content", CONTENT);
        arguments.put("ddmStructureKey", DDM_STRUCTURE_KEY);
        arguments.put("ddmTemplateKey", DDM_TEMPLATE_KEY);
        arguments.put("layoutUuid", LAYOUT_UUID);
        arguments.put("displayDateMonth", DISPLAY_DATE_MONTH);
        arguments.put("displayDateDay", DISPLAY_DATE_DAY);
        arguments.put("displayDateYear", DISPLAY_DATE_YEAR);
        arguments.put("displayDateHour", DISPLAY_DATE_HOUR);
        arguments.put("displayDateMinute", DISPLAY_DATE_MINUTE);
        arguments.put("expirationDateMonth", EXPIRATION_DATE_MONTH);
        arguments.put("expirationDateDay", EXPIRATION_DATE_DAY);
        arguments.put("expirationDateYear", EXPIRATION_DATE_YEAR);
        arguments.put("expirationDateHour", EXPIRATION_DATE_HOUR);
        arguments.put("expirationDateMinute", EXPIRATION_DATE_MINUTE);
        arguments.put("neverExpire", NEVER_EXPIRE);
        arguments.put("reviewDateMonth", REVIEW_DATE_MONTH);
        arguments.put("reviewDateDay", REVIEW_DATE_DAY);
        arguments.put("reviewDateYear", REVIEW_DATE_YEAR);
        arguments.put("reviewDateHour", REVIEW_DATE_HOUR);
        arguments.put("reviewDateMinute", REVIEW_DATE_MINUTE);
        arguments.put("neverReview", NEVER_REVIEW);
        arguments.put("indexable", INDEXABLE);
        arguments.put("articleURL", ARTICLE_URL);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalArticle expectedResult = mock(JournalArticle.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setClassNameId(CLASS_NAME_ID);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setArticleId(ARTICLE_ID);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setContent(CONTENT);
        expectedResult.setDDMStructureKey(DDM_STRUCTURE_KEY);
        expectedResult.setDDMTemplateKey(DDM_TEMPLATE_KEY);
        expectedResult.setLayoutUuid(LAYOUT_UUID);
        expectedResult.setIndexable(INDEXABLE);
        expectedResult.setUrlTitle(ARTICLE_URL);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addArticle(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(FOLDER_ID), eq(CLASS_NAME_ID), eq(CLASS_PK), eq(ARTICLE_ID), eq(AUTO_ARTICLE_ID), anyDouble(), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(FRIENDLY_URL_MAP), eq(CONTENT), eq(DDM_STRUCTURE_KEY), eq(DDM_TEMPLATE_KEY), eq(LAYOUT_UUID), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(NEVER_EXPIRE), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(NEVER_REVIEW), eq(INDEXABLE), anyBoolean(), any(), any(), any(), eq(ARTICLE_URL), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        JournalArticle result = resolvers.createJournalArticleDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createJournalArticleDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addArticle(anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), anyString(), anyBoolean(), anyDouble(), anyMap(), anyMap(), anyMap(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), anyBoolean(), anyBoolean(), any(), any(), any(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        JournalArticle result = resolvers.createJournalArticleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateJournalArticleDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("articleId", ARTICLE_ID);
        arguments.put("version", VERSION);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("friendlyURLMap", FRIENDLY_URL_MAP);
        arguments.put("content", CONTENT);
        arguments.put("ddmStructureKey", DDM_STRUCTURE_KEY);
        arguments.put("ddmTemplateKey", DDM_TEMPLATE_KEY);
        arguments.put("layoutUuid", LAYOUT_UUID);
        arguments.put("displayDateMonth", DISPLAY_DATE_MONTH);
        arguments.put("displayDateDay", DISPLAY_DATE_DAY);
        arguments.put("displayDateYear", DISPLAY_DATE_YEAR);
        arguments.put("displayDateHour", DISPLAY_DATE_HOUR);
        arguments.put("displayDateMinute", DISPLAY_DATE_MINUTE);
        arguments.put("expirationDateMonth", EXPIRATION_DATE_MONTH);
        arguments.put("expirationDateDay", EXPIRATION_DATE_DAY);
        arguments.put("expirationDateYear", EXPIRATION_DATE_YEAR);
        arguments.put("expirationDateHour", EXPIRATION_DATE_HOUR);
        arguments.put("expirationDateMinute", EXPIRATION_DATE_MINUTE);
        arguments.put("neverExpire", NEVER_EXPIRE);
        arguments.put("reviewDateMonth", REVIEW_DATE_MONTH);
        arguments.put("reviewDateDay", REVIEW_DATE_DAY);
        arguments.put("reviewDateYear", REVIEW_DATE_YEAR);
        arguments.put("reviewDateHour", REVIEW_DATE_HOUR);
        arguments.put("reviewDateMinute", REVIEW_DATE_MINUTE);
        arguments.put("neverReview", NEVER_REVIEW);
        arguments.put("indexable", INDEXABLE);
        arguments.put("articleURL", ARTICLE_URL);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalArticle expectedResult = mock(JournalArticle.class);
        expectedResult.setId(ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setArticleId(ARTICLE_ID);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setContent(CONTENT);
        expectedResult.setDDMStructureKey(DDM_STRUCTURE_KEY);
        expectedResult.setDDMTemplateKey(DDM_TEMPLATE_KEY);
        expectedResult.setLayoutUuid(LAYOUT_UUID);
        expectedResult.setIndexable(INDEXABLE);
        expectedResult.setUrlTitle(ARTICLE_URL);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateArticle(eq(USER_ID), eq(GROUP_ID), eq(FOLDER_ID), eq(ARTICLE_ID), eq(VERSION), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(FRIENDLY_URL_MAP), eq(CONTENT), eq(DDM_STRUCTURE_KEY), eq(DDM_TEMPLATE_KEY), eq(LAYOUT_UUID), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(NEVER_EXPIRE), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(NEVER_REVIEW), eq(INDEXABLE), anyBoolean(), any(), any(), any(), eq(ARTICLE_URL), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        JournalArticle result = resolvers.updateJournalArticleDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchArticleException.class)
    public void updateJournalArticleDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("version", VERSION);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("friendlyURLMap", FRIENDLY_URL_MAP);
        arguments.put("content", CONTENT);
        arguments.put("ddmStructureKey", DDM_STRUCTURE_KEY);
        arguments.put("ddmTemplateKey", DDM_TEMPLATE_KEY);
        arguments.put("layoutUuid", LAYOUT_UUID);
        arguments.put("displayDateMonth", DISPLAY_DATE_MONTH);
        arguments.put("displayDateDay", DISPLAY_DATE_DAY);
        arguments.put("displayDateYear", DISPLAY_DATE_YEAR);
        arguments.put("displayDateHour", DISPLAY_DATE_HOUR);
        arguments.put("displayDateMinute", DISPLAY_DATE_MINUTE);
        arguments.put("expirationDateMonth", EXPIRATION_DATE_MONTH);
        arguments.put("expirationDateDay", EXPIRATION_DATE_DAY);
        arguments.put("expirationDateYear", EXPIRATION_DATE_YEAR);
        arguments.put("expirationDateHour", EXPIRATION_DATE_HOUR);
        arguments.put("expirationDateMinute", EXPIRATION_DATE_MINUTE);
        arguments.put("neverExpire", NEVER_EXPIRE);
        arguments.put("reviewDateMonth", REVIEW_DATE_MONTH);
        arguments.put("reviewDateDay", REVIEW_DATE_DAY);
        arguments.put("reviewDateYear", REVIEW_DATE_YEAR);
        arguments.put("reviewDateHour", REVIEW_DATE_HOUR);
        arguments.put("reviewDateMinute", REVIEW_DATE_MINUTE);
        arguments.put("neverReview", NEVER_REVIEW);
        arguments.put("indexable", INDEXABLE);
        arguments.put("articleURL", ARTICLE_URL);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getStringArg(eq(environment), eq("articleId")))
                .thenReturn("");
        when(localService.updateArticle(eq(USER_ID), eq(GROUP_ID), eq(FOLDER_ID), eq(""), eq(VERSION), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(FRIENDLY_URL_MAP), eq(CONTENT), eq(DDM_STRUCTURE_KEY), eq(DDM_TEMPLATE_KEY), eq(LAYOUT_UUID), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(NEVER_EXPIRE), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(NEVER_REVIEW), eq(INDEXABLE), anyBoolean(), any(), any(), any(), eq(ARTICLE_URL), any(ServiceContext.class)))
                .thenThrow(NoSuchArticleException.class);

        // Asserts
        JournalArticle result = resolvers.updateJournalArticleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchArticleException.class)
    public void updateJournalArticleDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("articleId", "FakeId");
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("version", VERSION);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("friendlyURLMap", FRIENDLY_URL_MAP);
        arguments.put("content", CONTENT);
        arguments.put("ddmStructureKey", DDM_STRUCTURE_KEY);
        arguments.put("ddmTemplateKey", DDM_TEMPLATE_KEY);
        arguments.put("layoutUuid", LAYOUT_UUID);
        arguments.put("displayDateMonth", DISPLAY_DATE_MONTH);
        arguments.put("displayDateDay", DISPLAY_DATE_DAY);
        arguments.put("displayDateYear", DISPLAY_DATE_YEAR);
        arguments.put("displayDateHour", DISPLAY_DATE_HOUR);
        arguments.put("displayDateMinute", DISPLAY_DATE_MINUTE);
        arguments.put("expirationDateMonth", EXPIRATION_DATE_MONTH);
        arguments.put("expirationDateDay", EXPIRATION_DATE_DAY);
        arguments.put("expirationDateYear", EXPIRATION_DATE_YEAR);
        arguments.put("expirationDateHour", EXPIRATION_DATE_HOUR);
        arguments.put("expirationDateMinute", EXPIRATION_DATE_MINUTE);
        arguments.put("neverExpire", NEVER_EXPIRE);
        arguments.put("reviewDateMonth", REVIEW_DATE_MONTH);
        arguments.put("reviewDateDay", REVIEW_DATE_DAY);
        arguments.put("reviewDateYear", REVIEW_DATE_YEAR);
        arguments.put("reviewDateHour", REVIEW_DATE_HOUR);
        arguments.put("reviewDateMinute", REVIEW_DATE_MINUTE);
        arguments.put("neverReview", NEVER_REVIEW);
        arguments.put("indexable", INDEXABLE);
        arguments.put("articleURL", ARTICLE_URL);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getStringArg(eq(environment), eq("articleId")))
                .thenReturn("FakeId");
        when(localService.updateArticle(eq(USER_ID), eq(GROUP_ID), eq(FOLDER_ID), eq("FakeId"), eq(VERSION), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(FRIENDLY_URL_MAP), eq(CONTENT), eq(DDM_STRUCTURE_KEY), eq(DDM_TEMPLATE_KEY), eq(LAYOUT_UUID), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(NEVER_EXPIRE), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), eq(NEVER_REVIEW), eq(INDEXABLE), anyBoolean(), any(), any(), any(), eq(ARTICLE_URL), any(ServiceContext.class)))
                .thenThrow(NoSuchArticleException.class);

        // Asserts
        JournalArticle result = resolvers.updateJournalArticleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateJournalArticleDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.updateArticle(anyLong(), anyLong(), anyLong(), anyString(), anyDouble(), anyMap(), anyMap(), anyMap(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), anyBoolean(), anyBoolean(), any(), any(), any(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        JournalArticle result = resolvers.updateJournalArticleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteJournalArticleDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("id", ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalArticle expectedResult = mock(JournalArticle.class);
        expectedResult.setId(ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteJournalArticle(eq(ID)))
                .thenReturn(expectedResult);

        // Asserts
        JournalArticle result = resolvers.deleteJournalArticleDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchArticleException.class)
    public void deleteJournalArticleDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        JournalArticle expectedResult = mock(JournalArticle.class);
        expectedResult.setId(ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteJournalArticle(eq(ID)))
                .thenThrow(NoSuchArticleException.class);

        // Asserts
        JournalArticle result = resolvers.deleteJournalArticleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchArticleException.class)
    public void deleteJournalArticleDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("id", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalArticle expectedResult = mock(JournalArticle.class);
        expectedResult.setId(ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("id")))
                .thenReturn(789456L);
        when(localService.deleteJournalArticle(eq(789456L)))
                .thenThrow(NoSuchArticleException.class);

        // Asserts
        JournalArticle result = resolvers.deleteJournalArticleDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
