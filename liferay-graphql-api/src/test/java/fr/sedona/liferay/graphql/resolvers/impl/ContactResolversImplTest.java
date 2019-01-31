package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchContactException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Contact;
import com.liferay.portal.kernel.service.ContactLocalService;
import fr.sedona.liferay.graphql.loaders.ContactBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ContactResolvers;
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
 * Test suite for {@link ContactResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class ContactResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long CONTACT_ID = 987L;
    private static final long USER_ID = 123L;
    private static final String CLASS_NAME = "fr.sedona.Test";
    private static final long CLASS_PK = 456L;
    private static final String EMAIL_ADDRESS = "info@sedona.fr";
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
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, Contact> dataLoader;

    @InjectMocks
    ContactResolvers resolvers = new ContactResolversImpl();

    @Mock
    private ContactLocalService localService;

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
                .getDataLoader(ContactBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((ContactResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("contactId")))
                    .thenReturn(CONTACT_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("className")))
                    .thenReturn(CLASS_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getStringArg(eq(environment), eq("emailAddress")))
                    .thenReturn(EMAIL_ADDRESS);
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
            when(graphQLUtil.getBooleanArg(eq(environment), eq("male")))
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
    public void getContactsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Contact> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Contact entity = mock(Contact.class);
                    entity.setContactId(value);
                    availableObjects.add(entity);
                });
        List<Contact> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getContacts(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<Contact> results = resolvers.getContactsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getContactsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Contact> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Contact entity = mock(Contact.class);
                    entity.setContactId(value);
                    availableObjects.add(entity);
                });
        List<Contact> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getContacts(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<Contact> results = resolvers.getContactsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getContactsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Contact> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Contact entity = mock(Contact.class);
                    entity.setContactId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getContacts(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Contact> results = resolvers.getContactsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getContactsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Contact> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getContacts(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Contact> results = resolvers.getContactsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getContactDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Contact expectedResult = mock(Contact.class);
        expectedResult.setContactId(CONTACT_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("contactId"))
                .thenReturn(CONTACT_ID);
        when(dataLoader.load(CONTACT_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<Contact> asyncResult = resolvers.getContactDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Contact result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getContactDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("contactId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<Contact> asyncResult = resolvers.getContactDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getContactDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("contactId"))
                .thenReturn(CONTACT_ID);
        when(dataLoader.load(CONTACT_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<Contact> asyncResult = resolvers.getContactDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Contact result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createContactDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("emailAddress", EMAIL_ADDRESS);
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

        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Contact expectedResult = mock(Contact.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setContactId(CONTACT_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setEmailAddress(EMAIL_ADDRESS);
        expectedResult.setFirstName(FIRST_NAME);
        expectedResult.setMiddleName(MIDDLE_NAME);
        expectedResult.setLastName(LAST_NAME);
        expectedResult.setPrefixId(PREFIX_ID);
        expectedResult.setSuffixId(SUFFIX_ID);
        expectedResult.setMale(MALE);
        expectedResult.setSmsSn(SMS_SN);
        expectedResult.setFacebookSn(FACEBOOK_SN);
        expectedResult.setJabberSn(JABBER_SN);
        expectedResult.setSkypeSn(SKYPE_SN);
        expectedResult.setTwitterSn(TWITTER_SN);
        expectedResult.setJobTitle(JOB_TITLE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addContact(eq(CONTACT_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(EMAIL_ADDRESS), eq(FIRST_NAME), eq(MIDDLE_NAME), eq(LAST_NAME), eq(PREFIX_ID), eq(SUFFIX_ID), eq(MALE), eq(BIRTHDAY_MONTH), eq(BIRTHDAY_DAY), eq(BIRTHDAY_YEAR), eq(SMS_SN), eq(FACEBOOK_SN), eq(JABBER_SN), eq(SKYPE_SN), eq(TWITTER_SN), eq(JOB_TITLE)))
                .thenReturn(expectedResult);

        // Asserts
        Contact result = resolvers.createContactDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createContactDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("emailAddress", EMAIL_ADDRESS);
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
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Contact expectedResult = mock(Contact.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setContactId(CONTACT_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setEmailAddress(EMAIL_ADDRESS);
        expectedResult.setFirstName(FIRST_NAME);
        expectedResult.setMiddleName(MIDDLE_NAME);
        expectedResult.setLastName(LAST_NAME);
        expectedResult.setPrefixId(PREFIX_ID);
        expectedResult.setSuffixId(SUFFIX_ID);
        expectedResult.setMale(MALE);
        expectedResult.setSmsSn(SMS_SN);
        expectedResult.setFacebookSn(FACEBOOK_SN);
        expectedResult.setJabberSn(JABBER_SN);
        expectedResult.setSkypeSn(SKYPE_SN);
        expectedResult.setTwitterSn(TWITTER_SN);
        expectedResult.setJobTitle(JOB_TITLE);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addContact(eq(CONTACT_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(EMAIL_ADDRESS), eq(FIRST_NAME), eq(MIDDLE_NAME), eq(LAST_NAME), eq(PREFIX_ID), eq(SUFFIX_ID), eq(MALE), eq(BIRTHDAY_MONTH), eq(BIRTHDAY_DAY), eq(BIRTHDAY_YEAR), eq(SMS_SN), eq(FACEBOOK_SN), eq(JABBER_SN), eq(SKYPE_SN), eq(TWITTER_SN), eq(JOB_TITLE)))
                .thenReturn(expectedResult);

        // Asserts
        Contact result = resolvers.createContactDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createContactDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addContact(anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong(), anyBoolean(), anyInt(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(PortalException.class);

        // Asserts
        Contact result = resolvers.createContactDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateContactDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("contactId", CONTACT_ID);
        arguments.put("emailAddress", EMAIL_ADDRESS);
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
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Contact expectedResult = mock(Contact.class);
        expectedResult.setContactId(CONTACT_ID);
        expectedResult.setUserId(USER_ID);
        expectedResult.setContactId(CONTACT_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setEmailAddress(EMAIL_ADDRESS);
        expectedResult.setFirstName(FIRST_NAME);
        expectedResult.setMiddleName(MIDDLE_NAME);
        expectedResult.setLastName(LAST_NAME);
        expectedResult.setPrefixId(PREFIX_ID);
        expectedResult.setSuffixId(SUFFIX_ID);
        expectedResult.setMale(MALE);
        expectedResult.setSmsSn(SMS_SN);
        expectedResult.setFacebookSn(FACEBOOK_SN);
        expectedResult.setJabberSn(JABBER_SN);
        expectedResult.setSkypeSn(SKYPE_SN);
        expectedResult.setTwitterSn(TWITTER_SN);
        expectedResult.setJobTitle(JOB_TITLE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateContact(eq(CONTACT_ID), eq(EMAIL_ADDRESS), eq(FIRST_NAME), eq(MIDDLE_NAME), eq(LAST_NAME), eq(PREFIX_ID), eq(SUFFIX_ID), eq(MALE), eq(BIRTHDAY_MONTH), eq(BIRTHDAY_DAY), eq(BIRTHDAY_YEAR), eq(SMS_SN), eq(FACEBOOK_SN), eq(JABBER_SN), eq(SKYPE_SN), eq(TWITTER_SN), eq(JOB_TITLE)))
                .thenReturn(expectedResult);

        // Asserts
        Contact result = resolvers.updateContactDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchContactException.class)
    public void updateContactDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("emailAddress", EMAIL_ADDRESS);
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
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("contactId")))
                .thenReturn(0L);
        when(localService.updateContact(eq(0L), eq(EMAIL_ADDRESS), eq(FIRST_NAME), eq(MIDDLE_NAME), eq(LAST_NAME), eq(PREFIX_ID), eq(SUFFIX_ID), eq(MALE), eq(BIRTHDAY_MONTH), eq(BIRTHDAY_DAY), eq(BIRTHDAY_YEAR), eq(SMS_SN), eq(FACEBOOK_SN), eq(JABBER_SN), eq(SKYPE_SN), eq(TWITTER_SN), eq(JOB_TITLE)))
                .thenThrow(NoSuchContactException.class);

        // Asserts
        Contact result = resolvers.updateContactDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchContactException.class)
    public void updateContactDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("contactId", 789456L);
        arguments.put("emailAddress", EMAIL_ADDRESS);
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
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("contactId")))
                .thenReturn(789456L);
        when(localService.updateContact(eq(789456L), eq(EMAIL_ADDRESS), eq(FIRST_NAME), eq(MIDDLE_NAME), eq(LAST_NAME), eq(PREFIX_ID), eq(SUFFIX_ID), eq(MALE), eq(BIRTHDAY_MONTH), eq(BIRTHDAY_DAY), eq(BIRTHDAY_YEAR), eq(SMS_SN), eq(FACEBOOK_SN), eq(JABBER_SN), eq(SKYPE_SN), eq(TWITTER_SN), eq(JOB_TITLE)))
                .thenThrow(NoSuchContactException.class);

        // Asserts
        Contact result = resolvers.updateContactDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateContactDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("contactId")))
                .thenReturn(CONTACT_ID);
        when(localService.updateContact(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong(), anyBoolean(), anyInt(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(PortalException.class);

        // Asserts
        Contact result = resolvers.updateContactDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteContactDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("contactId", CONTACT_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Contact expectedResult = mock(Contact.class);
        expectedResult.setContactId(CONTACT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteContact(eq(CONTACT_ID)))
                .thenReturn(expectedResult);

        // Asserts
        Contact result = resolvers.deleteContactDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchContactException.class)
    public void deleteContactDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        Contact expectedResult = mock(Contact.class);
        expectedResult.setContactId(CONTACT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteContact(eq(CONTACT_ID)))
                .thenThrow(NoSuchContactException.class);

        // Asserts
        Contact result = resolvers.deleteContactDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchContactException.class)
    public void deleteContactDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("contactId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Contact expectedResult = mock(Contact.class);
        expectedResult.setContactId(CONTACT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("contactId")))
                .thenReturn(789456L);
        when(localService.deleteContact(eq(789456L)))
                .thenThrow(NoSuchContactException.class);

        // Asserts
        Contact result = resolvers.deleteContactDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
