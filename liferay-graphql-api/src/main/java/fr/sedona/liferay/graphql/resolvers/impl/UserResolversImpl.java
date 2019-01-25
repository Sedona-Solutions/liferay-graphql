package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import fr.sedona.liferay.graphql.loaders.UserBatchLoader;
import fr.sedona.liferay.graphql.resolvers.UserResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component(
        immediate = true,
        service = UserResolvers.class
)
@SuppressWarnings("squid:S1192")
public class UserResolversImpl implements UserResolvers {
    private UserLocalService userLocalService;

    @Reference(unbind = "-")
    public void setUserLocalService(UserLocalService userLocalService) {
        this.userLocalService = userLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<User>> getUsersDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return userLocalService.getUsers(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<List<User>>> getBulkUsersDataFetcher() {
        return environment -> {
            long[] userIds = util.getLongArrayArg(environment, "userIds");

            DataLoader<Long, User> dataLoader = environment.getDataLoader(UserBatchLoader.KEY);
            return dataLoader.loadMany(Arrays.stream(userIds)
                    .boxed()
                    .collect(Collectors.toList()));
        };
    }

    @Override
    public DataFetcher<CompletableFuture<User>> getUserDataFetcher() {
        return environment -> {
            long userId = getUserId(environment);
            if (userId <= 0) {
                return null;
            }

            DataLoader<Long, User> dataLoader = environment.getDataLoader(UserBatchLoader.KEY);
            return dataLoader.load(userId);
        };
    }

    private long getUserId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "userId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof Group) {
            return ((Group) source).getCreatorUserId();
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getUserId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<User> getUserByEmailDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String email = util.getStringArg(environment, "email");

            return userLocalService.getUserByEmailAddress(companyId, email);
        };
    }

    @Override
    public DataFetcher<User> getUserByScreenNameDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String screenName = util.getStringArg(environment, "screenName");

            return userLocalService.getUserByScreenName(companyId, screenName);
        };
    }

    @Override
    public DataFetcher<User> createUserDataFetcher() {
        return environment -> {
            long creatorUserId = util.getLongArg(environment, "creatorUserId", util.getDefaultUserId());
            long companyId = util.getLongArg(environment, "companyId");
            boolean autoPassword = util.getBooleanArg(environment, "autoPassword");
            String password = util.getStringArg(environment, "password");
            boolean autoScreenName = util.getBooleanArg(environment, "autoScreenName");
            String screenName = util.getStringArg(environment, "screenName");
            String emailAddress = util.getStringArg(environment, "emailAddress");
            long facebookId = util.getLongArg(environment, "facebookId");
            String openId = util.getStringArg(environment, "openId");
            Locale locale = util.getLocaleArg(environment, "locale");
            String firstName = util.getStringArg(environment, "firstName");
            String middleName = util.getStringArg(environment, "middleName");
            String lastName = util.getStringArg(environment, "lastName");
            long prefixId = util.getLongArg(environment, "prefixId");
            long suffixId = util.getLongArg(environment, "suffixId");
            boolean male = util.getBooleanArg(environment, "male", true);
            int birthdayMonth = util.getIntArg(environment, "birthdayMonth");
            int birthdayDay = util.getIntArg(environment, "birthdayDay");
            int birthdayYear = util.getIntArg(environment, "birthdayYear");
            String jobTitle = util.getStringArg(environment, "jobTitle");
            long[] groupIds = util.getLongArrayArg(environment, "groupIds");
            long[] organizationIds = util.getLongArrayArg(environment, "organizationIds");
            long[] roleIds = util.getLongArrayArg(environment, "roleIds");
            long[] userGroupIds = util.getLongArrayArg(environment, "userGroupIds");
            boolean sendEmail = util.getBooleanArg(environment, "sendEmail");
            ServiceContext serviceContext = new ServiceContext();

            return userLocalService.addUser(
                    creatorUserId,
                    companyId,
                    autoPassword,
                    password,
                    password,
                    autoScreenName,
                    screenName,
                    emailAddress,
                    facebookId,
                    openId,
                    locale,
                    firstName,
                    middleName,
                    lastName,
                    prefixId,
                    suffixId,
                    male,
                    birthdayMonth,
                    birthdayDay,
                    birthdayYear,
                    jobTitle,
                    groupIds,
                    organizationIds,
                    roleIds,
                    userGroupIds,
                    sendEmail,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<User> updateUserDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            String oldPassword = util.getStringArg(environment, "oldPassword");
            String newPassword1 = util.getStringArg(environment, "newPassword1");
            String newPassword2 = util.getStringArg(environment, "newPassword2");
            boolean passwordReset = util.getBooleanArg(environment, "passwordReset");
            String reminderQueryQuestion = util.getStringArg(environment, "reminderQueryQuestion");
            String reminderQueryAnswer = util.getStringArg(environment, "reminderQueryAnswer");
            String screenName = util.getStringArg(environment, "screenName");
            String emailAddress = util.getStringArg(environment, "emailAddress");
            long facebookId = util.getLongArg(environment, "facebookId");
            String openId = util.getStringArg(environment, "openId");
            String languageId = util.getStringArg(environment, "languageId");
            String timeZoneId = util.getStringArg(environment, "timeZoneId");
            String greeting = util.getStringArg(environment, "greeting");
            String comments = util.getStringArg(environment, "comments");
            String firstName = util.getStringArg(environment, "firstName");
            String middleName = util.getStringArg(environment, "middleName");
            String lastName = util.getStringArg(environment, "lastName");
            long prefixId = util.getLongArg(environment, "prefixId");
            long suffixId = util.getLongArg(environment, "suffixId");
            boolean male = util.getBooleanArg(environment, "male", true);
            int birthdayMonth = util.getIntArg(environment, "birthdayMonth");
            int birthdayDay = util.getIntArg(environment, "birthdayDay");
            int birthdayYear = util.getIntArg(environment, "birthdayYear");
            String smsSn = util.getStringArg(environment, "smsSn");
            String facebookSn = util.getStringArg(environment, "facebookSn");
            String jabberSn = util.getStringArg(environment, "jabberSn");
            String skypeSn = util.getStringArg(environment, "skypeSn");
            String twitterSn = util.getStringArg(environment, "twitterSn");
            String jobTitle = util.getStringArg(environment, "jobTitle");
            long[] groupIds = util.getLongArrayArg(environment, "groupIds");
            long[] organizationIds = util.getLongArrayArg(environment, "organizationIds");
            long[] roleIds = util.getLongArrayArg(environment, "roleIds");
            long[] userGroupIds = util.getLongArrayArg(environment, "userGroupIds");
            ServiceContext serviceContext = new ServiceContext();

            return userLocalService.updateUser(
                    userId,
                    oldPassword,
                    newPassword1,
                    newPassword2,
                    passwordReset,
                    reminderQueryQuestion,
                    reminderQueryAnswer,
                    screenName,
                    emailAddress,
                    facebookId,
                    openId,
                    false,
                    null,
                    languageId,
                    timeZoneId,
                    greeting,
                    comments,
                    firstName,
                    middleName,
                    lastName,
                    prefixId,
                    suffixId,
                    male,
                    birthdayMonth,
                    birthdayDay,
                    birthdayYear,
                    smsSn,
                    facebookSn,
                    jabberSn,
                    skypeSn,
                    twitterSn,
                    jobTitle,
                    groupIds,
                    organizationIds,
                    roleIds,
                    null,
                    userGroupIds,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<User> deleteUserDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId");

            return userLocalService.deleteUser(userId);
        };
    }
}
