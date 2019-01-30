package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Contact;
import com.liferay.portal.kernel.service.ContactLocalService;
import fr.sedona.liferay.graphql.loaders.ContactBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ContactResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = ContactResolvers.class
)
@SuppressWarnings("squid:S1192")
public class ContactResolversImpl implements ContactResolvers {
    private ContactLocalService contactLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setContactLocalService(ContactLocalService contactLocalService) {
        this.contactLocalService = contactLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<Contact>> getContactsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return contactLocalService.getContacts(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<Contact>> getContactDataFetcher() {
        return environment -> {
            long contactId = getContactId(environment);
            if (contactId <= 0) {
                return null;
            }

            DataLoader<Long, Contact> dataLoader = environment.getDataLoader(ContactBatchLoader.KEY);
            return dataLoader.load(contactId);
        };
    }

    private long getContactId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "contactId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getContactId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<Contact> createContactDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId");
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");
            String emailAddress = util.getStringArg(environment, "emailAddress");
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

            return contactLocalService.addContact(
                    userId,
                    className,
                    classPK,
                    emailAddress,
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
                    jobTitle);
        };
    }

    @Override
    public DataFetcher<Contact> updateContactDataFetcher() {
        return environment -> {
            long contactId = util.getLongArg(environment, "contactId");
            String emailAddress = util.getStringArg(environment, "emailAddress");
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

            return contactLocalService.updateContact(
                    contactId,
                    emailAddress,
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
                    jobTitle);
        };
    }

    @Override
    public DataFetcher<Contact> deleteContactDataFetcher() {
        return environment -> {
            long contactId = util.getLongArg(environment, "contactId");

            return contactLocalService.deleteContact(contactId);
        };
    }
}
