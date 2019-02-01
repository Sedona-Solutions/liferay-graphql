package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import fr.sedona.liferay.graphql.loaders.UserBatchLoader;
import fr.sedona.liferay.graphql.resolvers.UserResolvers;
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
 * Test suite for {@link UserResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class UserResolversImplTest {
    private static final long DEFAULT_CREATOR_ID = 456456L;
    private static final long CREATOR_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long COMPANY_ID = 456L;
    private static final boolean AUTO_PASSWORD = false;
    private static final String PASSWORD = "Password!";
    private static final String OLD_PASSWORD = "oldPassword";
    private static final String NEW_PASSWORD1 = "newPassword!";
    private static final String NEW_PASSWORD2 = "newPassword!";
    private static final boolean PASSWORD_RESET = false;
    private static final String REMINDER_QUERY_QUESTION = "what-is-your-first-pet-name";
    private static final String REMINDER_QUERY_ANSWER = "Astro";
    private static final boolean AUTO_SCREEN_NAME = false;
    private static final String SCREEN_NAME = "pmars";
    private static final String EMAIL_ADDRESS = "pmars@sedona.fr";
    private static final long FACEBOOK_ID = 0;
    private static final String OPEN_ID = null;
    private static final Locale LOCALE = new Locale("en", "US");
    private static final String LANGUAGE_ID = "en_US";
    private static final String TIME_ZONE_ID = "GMT";
    private static final String GREETING = "Welcome Polo!";
    private static final String COMMENTS = null;
    private static final String FIRST_NAME = "Paul";
    private static final String MIDDLE_NAME = null;
    private static final String LAST_NAME = "Mars";
    private static final long PREFIX_ID = 1L;
    private static final long SUFFIX_ID = 0;
    private static final boolean MALE = true;
    private static final int BIRTHDAY_MONTH = 0;
    private static final int BIRTHDAY_DAY = 0;
    private static final int BIRTHDAY_YEAR = 0;
    private static final String SMS_SN = null;
    private static final String FACEBOOK_SN = null;
    private static final String JABBER_SN = null;
    private static final String SKYPE_SN = null;
    private static final String TWITTER_SN = null;
    private static final String JOB_TITLE = "Architect";
    private static final long[] GROUP_IDS = new long[]{1L, 2L, 3L};
    private static final long[] ORGANIZATION_IDS = new long[]{1L, 3L};
    private static final long[] ROLE_IDS = new long[]{2L, 3L};
    private static final long[] USER_GROUP_IDS = new long[]{1L, 2L};
    private static final boolean SEND_EMAIL = false;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, User> dataLoader;

    @InjectMocks
    UserResolvers resolvers = new UserResolversImpl();

    @Mock
    private UserLocalService localService;

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
                .getDataLoader(UserBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((UserResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("creatorUserId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("userId")))
                    .thenReturn(USER_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                    .thenReturn(COMPANY_ID);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("autoPassword")))
                    .thenReturn(AUTO_PASSWORD);
            when(graphQLUtil.getStringArg(eq(environment), eq("password")))
                    .thenReturn(PASSWORD);
            when(graphQLUtil.getStringArg(eq(environment), eq("oldPassword")))
                    .thenReturn(OLD_PASSWORD);
            when(graphQLUtil.getStringArg(eq(environment), eq("newPassword1")))
                    .thenReturn(NEW_PASSWORD1);
            when(graphQLUtil.getStringArg(eq(environment), eq("newPassword2")))
                    .thenReturn(NEW_PASSWORD2);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("passwordReset")))
                    .thenReturn(PASSWORD_RESET);
            when(graphQLUtil.getStringArg(eq(environment), eq("reminderQueryQuestion")))
                    .thenReturn(REMINDER_QUERY_QUESTION);
            when(graphQLUtil.getStringArg(eq(environment), eq("reminderQueryAnswer")))
                    .thenReturn(REMINDER_QUERY_ANSWER);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("autoScreenName")))
                    .thenReturn(AUTO_SCREEN_NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("screenName")))
                    .thenReturn(SCREEN_NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("emailAddress")))
                    .thenReturn(EMAIL_ADDRESS);
            when(graphQLUtil.getLongArg(eq(environment), eq("facebookId")))
                    .thenReturn(FACEBOOK_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("openId")))
                    .thenReturn(OPEN_ID);
            when(graphQLUtil.getLocaleArg(eq(environment), eq("locale")))
                    .thenReturn(LOCALE);
            when(graphQLUtil.getStringArg(eq(environment), eq("languageId")))
                    .thenReturn(LANGUAGE_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("timeZoneId")))
                    .thenReturn(TIME_ZONE_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("greeting")))
                    .thenReturn(GREETING);
            when(graphQLUtil.getStringArg(eq(environment), eq("comments")))
                    .thenReturn(COMMENTS);
            when(graphQLUtil.getStringArg(eq(environment), eq("firstName")))
                    .thenReturn(FIRST_NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("middleName")))
                    .thenReturn(MIDDLE_NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("lastName")))
                    .thenReturn(LAST_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("prefixId")))
                    .thenReturn(PREFIX_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("suffixId")))
                    .thenReturn(SUFFIX_ID);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("male"), anyBoolean()))
                    .thenReturn(MALE);
            when(graphQLUtil.getIntArg(eq(environment), eq("birthdayMonth")))
                    .thenReturn(BIRTHDAY_MONTH);
            when(graphQLUtil.getIntArg(eq(environment), eq("birthdayDay")))
                    .thenReturn(BIRTHDAY_DAY);
            when(graphQLUtil.getIntArg(eq(environment), eq("birthdayYear")))
                    .thenReturn(BIRTHDAY_YEAR);
            when(graphQLUtil.getStringArg(eq(environment), eq("smsSn")))
                    .thenReturn(SMS_SN);
            when(graphQLUtil.getStringArg(eq(environment), eq("facebookSn")))
                    .thenReturn(FACEBOOK_SN);
            when(graphQLUtil.getStringArg(eq(environment), eq("jabberSn")))
                    .thenReturn(JABBER_SN);
            when(graphQLUtil.getStringArg(eq(environment), eq("skypeSn")))
                    .thenReturn(SKYPE_SN);
            when(graphQLUtil.getStringArg(eq(environment), eq("twitterSn")))
                    .thenReturn(TWITTER_SN);
            when(graphQLUtil.getStringArg(eq(environment), eq("jobTitle")))
                    .thenReturn(JOB_TITLE);
            when(graphQLUtil.getLongArrayArg(eq(environment), eq("groupIds")))
                    .thenReturn(GROUP_IDS);
            when(graphQLUtil.getLongArrayArg(eq(environment), eq("organizationIds")))
                    .thenReturn(ORGANIZATION_IDS);
            when(graphQLUtil.getLongArrayArg(eq(environment), eq("roleIds")))
                    .thenReturn(ROLE_IDS);
            when(graphQLUtil.getLongArrayArg(eq(environment), eq("userGroupIds")))
                    .thenReturn(USER_GROUP_IDS);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("sendEmail")))
                    .thenReturn(SEND_EMAIL);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getIntArg(eq(environment), anyString()))
                    .thenReturn(0);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString(), anyBoolean()))
                    .thenReturn(false);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
                    .thenReturn("");
            when(graphQLUtil.getLongArrayArg(eq(environment), anyString()))
                    .thenReturn(new long[0]);
            when(graphQLUtil.getLocaleArg(eq(environment), anyString()))
                    .thenReturn(LocaleUtil.US);
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
    public void getUsersDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<User> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    User entity = mock(User.class);
                    entity.setUserId(value);
                    availableObjects.add(entity);
                });
        List<User> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getUsers(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<User> results = resolvers.getUsersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getUsersDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<User> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    User entity = mock(User.class);
                    entity.setUserId(value);
                    availableObjects.add(entity);
                });
        List<User> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getUsers(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<User> results = resolvers.getUsersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getUsersDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<User> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    User entity = mock(User.class);
                    entity.setUserId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getUsers(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<User> results = resolvers.getUsersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getUsersDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<User> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getUsers(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<User> results = resolvers.getUsersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getUserDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        User expectedResult = mock(User.class);
        expectedResult.setUserId(USER_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("userId"))
                .thenReturn(USER_ID);
        when(dataLoader.load(USER_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<User> asyncResult = resolvers.getUserDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        User result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getUserDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("userId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<User> asyncResult = resolvers.getUserDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getUserDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("userId"))
                .thenReturn(USER_ID);
        when(dataLoader.load(USER_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<User> asyncResult = resolvers.getUserDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        User result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createUserDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("creatorUserId", CREATOR_ID);
        arguments.put("companyId", COMPANY_ID);
        arguments.put("autoPassword", AUTO_PASSWORD);
        arguments.put("password", PASSWORD);
        arguments.put("autoScreenName", AUTO_SCREEN_NAME);
        arguments.put("screenName", SCREEN_NAME);
        arguments.put("emailAddress", EMAIL_ADDRESS);
        arguments.put("facebookId", FACEBOOK_ID);
        arguments.put("openId", OPEN_ID);
        arguments.put("locale", LOCALE);
        arguments.put("firstName", FIRST_NAME);
        arguments.put("middleName", MIDDLE_NAME);
        arguments.put("lastName", LAST_NAME);
        arguments.put("prefixId", PREFIX_ID);
        arguments.put("suffixId", SUFFIX_ID);
        arguments.put("male", MALE);
        arguments.put("birthdayMonth", BIRTHDAY_MONTH);
        arguments.put("birthdayDay", BIRTHDAY_DAY);
        arguments.put("birthdayYear", BIRTHDAY_YEAR);
        arguments.put("jobTitle", JOB_TITLE);
        arguments.put("groupIds", GROUP_IDS);
        arguments.put("organizationIds", ORGANIZATION_IDS);
        arguments.put("roleIds", ROLE_IDS);
        arguments.put("userGroupIds", USER_GROUP_IDS);
        arguments.put("sendEmail", SEND_EMAIL);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        User expectedResult = mock(User.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setPassword(PASSWORD);
        expectedResult.setScreenName(SCREEN_NAME);
        expectedResult.setEmailAddress(EMAIL_ADDRESS);
        expectedResult.setFacebookId(FACEBOOK_ID);
        expectedResult.setOpenId(OPEN_ID);
        expectedResult.setLanguageId(LOCALE.toString());
        expectedResult.setFirstName(FIRST_NAME);
        expectedResult.setMiddleName(MIDDLE_NAME);
        expectedResult.setLastName(LAST_NAME);
        expectedResult.setJobTitle(JOB_TITLE);

        // When / Then
        useMockGraphQLUtil(environment, CREATOR_ID, true);
        when(localService.addUser(eq(CREATOR_ID), eq(COMPANY_ID), eq(AUTO_PASSWORD), eq(PASSWORD), eq(PASSWORD), eq(AUTO_SCREEN_NAME), eq(SCREEN_NAME), eq(EMAIL_ADDRESS), eq(FACEBOOK_ID), eq(OPEN_ID), eq(LOCALE), eq(FIRST_NAME), eq(MIDDLE_NAME), eq(LAST_NAME), eq(PREFIX_ID), eq(SUFFIX_ID), eq(MALE), eq(BIRTHDAY_MONTH), eq(BIRTHDAY_DAY), eq(BIRTHDAY_YEAR), eq(JOB_TITLE), any(), any(), any(), any(), eq(SEND_EMAIL), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        User result = resolvers.createUserDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createUserDataFetcher_without_creator_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("autoPassword", AUTO_PASSWORD);
        arguments.put("password", PASSWORD);
        arguments.put("autoScreenName", AUTO_SCREEN_NAME);
        arguments.put("screenName", SCREEN_NAME);
        arguments.put("emailAddress", EMAIL_ADDRESS);
        arguments.put("facebookId", FACEBOOK_ID);
        arguments.put("openId", OPEN_ID);
        arguments.put("locale", LOCALE);
        arguments.put("firstName", FIRST_NAME);
        arguments.put("middleName", MIDDLE_NAME);
        arguments.put("lastName", LAST_NAME);
        arguments.put("prefixId", PREFIX_ID);
        arguments.put("suffixId", SUFFIX_ID);
        arguments.put("male", MALE);
        arguments.put("birthdayMonth", BIRTHDAY_MONTH);
        arguments.put("birthdayDay", BIRTHDAY_DAY);
        arguments.put("birthdayYear", BIRTHDAY_YEAR);
        arguments.put("jobTitle", JOB_TITLE);
        arguments.put("groupIds", GROUP_IDS);
        arguments.put("organizationIds", ORGANIZATION_IDS);
        arguments.put("roleIds", ROLE_IDS);
        arguments.put("userGroupIds", USER_GROUP_IDS);
        arguments.put("sendEmail", SEND_EMAIL);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        User expectedResult = mock(User.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setPassword(PASSWORD);
        expectedResult.setScreenName(SCREEN_NAME);
        expectedResult.setEmailAddress(EMAIL_ADDRESS);
        expectedResult.setFacebookId(FACEBOOK_ID);
        expectedResult.setOpenId(OPEN_ID);
        expectedResult.setLanguageId(LOCALE.toString());
        expectedResult.setFirstName(FIRST_NAME);
        expectedResult.setMiddleName(MIDDLE_NAME);
        expectedResult.setLastName(LAST_NAME);
        expectedResult.setJobTitle(JOB_TITLE);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_CREATOR_ID, true);
        when(localService.addUser(eq(DEFAULT_CREATOR_ID), eq(COMPANY_ID), eq(AUTO_PASSWORD), eq(PASSWORD), eq(PASSWORD), eq(AUTO_SCREEN_NAME), eq(SCREEN_NAME), eq(EMAIL_ADDRESS), eq(FACEBOOK_ID), eq(OPEN_ID), eq(LOCALE), eq(FIRST_NAME), eq(MIDDLE_NAME), eq(LAST_NAME), eq(PREFIX_ID), eq(SUFFIX_ID), eq(MALE), eq(BIRTHDAY_MONTH), eq(BIRTHDAY_DAY), eq(BIRTHDAY_YEAR), eq(JOB_TITLE), any(), any(), any(), any(), eq(SEND_EMAIL), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        User result = resolvers.createUserDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createUserDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_CREATOR_ID, false);
        when(localService.addUser(anyLong(), anyLong(), anyBoolean(), anyString(), anyString(), anyBoolean(), anyString(), anyString(), anyLong(), anyString(), any(Locale.class), anyString(), anyString(), anyString(), anyLong(), anyLong(), anyBoolean(), anyInt(), anyInt(), anyInt(), anyString(), any(), any(), any(), any(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        User result = resolvers.createUserDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateUserDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("oldPassword", OLD_PASSWORD);
        arguments.put("newPassword1", NEW_PASSWORD1);
        arguments.put("newPassword2", NEW_PASSWORD2);
        arguments.put("passwordReset", PASSWORD_RESET);
        arguments.put("reminderQueryQuestion", REMINDER_QUERY_QUESTION);
        arguments.put("reminderQueryAnswer", REMINDER_QUERY_ANSWER);
        arguments.put("screenName", SCREEN_NAME);
        arguments.put("emailAddress", EMAIL_ADDRESS);
        arguments.put("facebookId", FACEBOOK_ID);
        arguments.put("openId", OPEN_ID);
        arguments.put("languageId", LANGUAGE_ID);
        arguments.put("timeZoneId", TIME_ZONE_ID);
        arguments.put("greeting", GREETING);
        arguments.put("comments", COMMENTS);
        arguments.put("firstName", FIRST_NAME);
        arguments.put("middleName", MIDDLE_NAME);
        arguments.put("lastName", LAST_NAME);
        arguments.put("prefixId", PREFIX_ID);
        arguments.put("suffixId", SUFFIX_ID);
        arguments.put("male", MALE);
        arguments.put("birthdayMonth", BIRTHDAY_MONTH);
        arguments.put("birthdayDay", BIRTHDAY_DAY);
        arguments.put("birthdayYear", BIRTHDAY_YEAR);
        arguments.put("smsSn", SMS_SN);
        arguments.put("facebookSn", FACEBOOK_SN);
        arguments.put("jabberSn", JABBER_SN);
        arguments.put("skypeSn", SKYPE_SN);
        arguments.put("twitterSn", TWITTER_SN);
        arguments.put("jobTitle", JOB_TITLE);
        arguments.put("groupIds", GROUP_IDS);
        arguments.put("organizationIds", ORGANIZATION_IDS);
        arguments.put("roleIds", ROLE_IDS);
        arguments.put("userGroupIds", USER_GROUP_IDS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        User expectedResult = mock(User.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setPassword(NEW_PASSWORD2);
        expectedResult.setPasswordReset(PASSWORD_RESET);
        expectedResult.setReminderQueryQuestion(REMINDER_QUERY_QUESTION);
        expectedResult.setReminderQueryAnswer(REMINDER_QUERY_ANSWER);
        expectedResult.setScreenName(SCREEN_NAME);
        expectedResult.setEmailAddress(EMAIL_ADDRESS);
        expectedResult.setFacebookId(FACEBOOK_ID);
        expectedResult.setOpenId(OPEN_ID);
        expectedResult.setLanguageId(LANGUAGE_ID);
        expectedResult.setTimeZoneId(TIME_ZONE_ID);
        expectedResult.setGreeting(GREETING);
        expectedResult.setComments(COMMENTS);
        expectedResult.setFirstName(FIRST_NAME);
        expectedResult.setMiddleName(MIDDLE_NAME);
        expectedResult.setLastName(LAST_NAME);
        expectedResult.setJobTitle(JOB_TITLE);

        // When / Then
        useMockGraphQLUtil(environment, CREATOR_ID, true);
        when(localService.updateUser(eq(USER_ID), eq(OLD_PASSWORD), eq(NEW_PASSWORD1), eq(NEW_PASSWORD2), eq(PASSWORD_RESET), eq(REMINDER_QUERY_QUESTION), eq(REMINDER_QUERY_ANSWER), eq(SCREEN_NAME), eq(EMAIL_ADDRESS), eq(FACEBOOK_ID), eq(OPEN_ID), anyBoolean(), any(), eq(LANGUAGE_ID), eq(TIME_ZONE_ID), eq(GREETING), eq(COMMENTS), eq(FIRST_NAME), eq(MIDDLE_NAME), eq(LAST_NAME), eq(PREFIX_ID), eq(SUFFIX_ID), eq(MALE), eq(BIRTHDAY_MONTH), eq(BIRTHDAY_DAY), eq(BIRTHDAY_YEAR), eq(SMS_SN), eq(FACEBOOK_SN), eq(JABBER_SN), eq(SKYPE_SN), eq(TWITTER_SN), eq(JOB_TITLE), eq(GROUP_IDS), eq(ORGANIZATION_IDS), eq(ROLE_IDS), anyList(), eq(USER_GROUP_IDS), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        User result = resolvers.updateUserDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchUserException.class)
    public void updateUserDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("oldPassword", OLD_PASSWORD);
        arguments.put("newPassword1", NEW_PASSWORD1);
        arguments.put("newPassword2", NEW_PASSWORD2);
        arguments.put("passwordReset", PASSWORD_RESET);
        arguments.put("reminderQueryQuestion", REMINDER_QUERY_QUESTION);
        arguments.put("reminderQueryAnswer", REMINDER_QUERY_ANSWER);
        arguments.put("screenName", SCREEN_NAME);
        arguments.put("emailAddress", EMAIL_ADDRESS);
        arguments.put("facebookId", FACEBOOK_ID);
        arguments.put("openId", OPEN_ID);
        arguments.put("languageId", LANGUAGE_ID);
        arguments.put("timeZoneId", TIME_ZONE_ID);
        arguments.put("greeting", GREETING);
        arguments.put("comments", COMMENTS);
        arguments.put("firstName", FIRST_NAME);
        arguments.put("middleName", MIDDLE_NAME);
        arguments.put("lastName", LAST_NAME);
        arguments.put("prefixId", PREFIX_ID);
        arguments.put("suffixId", SUFFIX_ID);
        arguments.put("male", MALE);
        arguments.put("birthdayMonth", BIRTHDAY_MONTH);
        arguments.put("birthdayDay", BIRTHDAY_DAY);
        arguments.put("birthdayYear", BIRTHDAY_YEAR);
        arguments.put("smsSn", SMS_SN);
        arguments.put("facebookSn", FACEBOOK_SN);
        arguments.put("jabberSn", JABBER_SN);
        arguments.put("skypeSn", SKYPE_SN);
        arguments.put("twitterSn", TWITTER_SN);
        arguments.put("jobTitle", JOB_TITLE);
        arguments.put("groupIds", GROUP_IDS);
        arguments.put("organizationIds", ORGANIZATION_IDS);
        arguments.put("roleIds", ROLE_IDS);
        arguments.put("userGroupIds", USER_GROUP_IDS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_CREATOR_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("userId")))
                .thenReturn(0L);
        when(localService.updateUser(eq(0L), eq(OLD_PASSWORD), eq(NEW_PASSWORD1), eq(NEW_PASSWORD2), eq(PASSWORD_RESET), eq(REMINDER_QUERY_QUESTION), eq(REMINDER_QUERY_ANSWER), eq(SCREEN_NAME), eq(EMAIL_ADDRESS), eq(FACEBOOK_ID), eq(OPEN_ID), anyBoolean(), any(), eq(LANGUAGE_ID), eq(TIME_ZONE_ID), eq(GREETING), eq(COMMENTS), eq(FIRST_NAME), eq(MIDDLE_NAME), eq(LAST_NAME), eq(PREFIX_ID), eq(SUFFIX_ID), eq(MALE), eq(BIRTHDAY_MONTH), eq(BIRTHDAY_DAY), eq(BIRTHDAY_YEAR), eq(SMS_SN), eq(FACEBOOK_SN), eq(JABBER_SN), eq(SKYPE_SN), eq(TWITTER_SN), eq(JOB_TITLE), eq(GROUP_IDS), eq(ORGANIZATION_IDS), eq(ROLE_IDS), anyList(), eq(USER_GROUP_IDS), any(ServiceContext.class)))
                .thenThrow(NoSuchUserException.class);

        // Asserts
        User result = resolvers.updateUserDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchUserException.class)
    public void updateUserDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", 789456L);
        arguments.put("oldPassword", OLD_PASSWORD);
        arguments.put("newPassword1", NEW_PASSWORD1);
        arguments.put("newPassword2", NEW_PASSWORD2);
        arguments.put("passwordReset", PASSWORD_RESET);
        arguments.put("reminderQueryQuestion", REMINDER_QUERY_QUESTION);
        arguments.put("reminderQueryAnswer", REMINDER_QUERY_ANSWER);
        arguments.put("screenName", SCREEN_NAME);
        arguments.put("emailAddress", EMAIL_ADDRESS);
        arguments.put("facebookId", FACEBOOK_ID);
        arguments.put("openId", OPEN_ID);
        arguments.put("languageId", LANGUAGE_ID);
        arguments.put("timeZoneId", TIME_ZONE_ID);
        arguments.put("greeting", GREETING);
        arguments.put("comments", COMMENTS);
        arguments.put("firstName", FIRST_NAME);
        arguments.put("middleName", MIDDLE_NAME);
        arguments.put("lastName", LAST_NAME);
        arguments.put("prefixId", PREFIX_ID);
        arguments.put("suffixId", SUFFIX_ID);
        arguments.put("male", MALE);
        arguments.put("birthdayMonth", BIRTHDAY_MONTH);
        arguments.put("birthdayDay", BIRTHDAY_DAY);
        arguments.put("birthdayYear", BIRTHDAY_YEAR);
        arguments.put("smsSn", SMS_SN);
        arguments.put("facebookSn", FACEBOOK_SN);
        arguments.put("jabberSn", JABBER_SN);
        arguments.put("skypeSn", SKYPE_SN);
        arguments.put("twitterSn", TWITTER_SN);
        arguments.put("jobTitle", JOB_TITLE);
        arguments.put("groupIds", GROUP_IDS);
        arguments.put("organizationIds", ORGANIZATION_IDS);
        arguments.put("roleIds", ROLE_IDS);
        arguments.put("userGroupIds", USER_GROUP_IDS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, CREATOR_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("userId")))
                .thenReturn(789456L);
        when(localService.updateUser(eq(789456L), eq(OLD_PASSWORD), eq(NEW_PASSWORD1), eq(NEW_PASSWORD2), eq(PASSWORD_RESET), eq(REMINDER_QUERY_QUESTION), eq(REMINDER_QUERY_ANSWER), eq(SCREEN_NAME), eq(EMAIL_ADDRESS), eq(FACEBOOK_ID), eq(OPEN_ID), anyBoolean(), any(), eq(LANGUAGE_ID), eq(TIME_ZONE_ID), eq(GREETING), eq(COMMENTS), eq(FIRST_NAME), eq(MIDDLE_NAME), eq(LAST_NAME), eq(PREFIX_ID), eq(SUFFIX_ID), eq(MALE), eq(BIRTHDAY_MONTH), eq(BIRTHDAY_DAY), eq(BIRTHDAY_YEAR), eq(SMS_SN), eq(FACEBOOK_SN), eq(JABBER_SN), eq(SKYPE_SN), eq(TWITTER_SN), eq(JOB_TITLE), eq(GROUP_IDS), eq(ORGANIZATION_IDS), eq(ROLE_IDS), anyList(), eq(USER_GROUP_IDS), any(ServiceContext.class)))
                .thenThrow(NoSuchUserException.class);

        // Asserts
        User result = resolvers.updateUserDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateUserDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_CREATOR_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("userId")))
                .thenReturn(DEFAULT_CREATOR_ID);
        when(localService.updateUser(anyLong(), anyString(), anyString(), anyString(), anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyLong(), anyString(), anyBoolean(), any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong(), anyBoolean(), anyInt(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(), any(), any(), anyList(), any(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        User result = resolvers.updateUserDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteUserDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        User expectedResult = mock(User.class);
        expectedResult.setUserId(USER_ID);

        // When / Then
        useMockGraphQLUtil(environment, CREATOR_ID, true);
        when(localService.deleteUser(eq(USER_ID)))
                .thenReturn(expectedResult);

        // Asserts
        User result = resolvers.deleteUserDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchUserException.class)
    public void deleteUserDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        User expectedResult = mock(User.class);
        expectedResult.setUserId(USER_ID);

        // When / Then
        useMockGraphQLUtil(environment, CREATOR_ID, true);
        when(localService.deleteUser(eq(USER_ID)))
                .thenThrow(NoSuchUserException.class);

        // Asserts
        User result = resolvers.deleteUserDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchUserException.class)
    public void deleteUserDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        User expectedResult = mock(User.class);
        expectedResult.setUserId(USER_ID);

        // When / Then
        useMockGraphQLUtil(environment, CREATOR_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("userId")))
                .thenReturn(789456L);
        when(localService.deleteUser(eq(789456L)))
                .thenThrow(NoSuchUserException.class);

        // Asserts
        User result = resolvers.deleteUserDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
