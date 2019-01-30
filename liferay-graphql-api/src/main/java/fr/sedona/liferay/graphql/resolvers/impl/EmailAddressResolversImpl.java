package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.EmailAddress;
import com.liferay.portal.kernel.service.EmailAddressLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.EmailAddressBatchLoader;
import fr.sedona.liferay.graphql.resolvers.EmailAddressResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = EmailAddressResolvers.class
)
@SuppressWarnings("squid:S1192")
public class EmailAddressResolversImpl implements EmailAddressResolvers {
    private EmailAddressLocalService emailaddressLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setEmailAddressLocalService(EmailAddressLocalService emailaddressLocalService) {
        this.emailaddressLocalService = emailaddressLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<EmailAddress>> getEmailAddressesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return emailaddressLocalService.getEmailAddresses(start, end);
        };
    }

    @Override
    public DataFetcher<List<EmailAddress>> getEmailAddressesForEntityDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");

            return emailaddressLocalService.getEmailAddresses(companyId, className, classPK);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<EmailAddress>> getEmailAddressDataFetcher() {
        return environment -> {
            long emailAddressId = util.getLongArg(environment, "emailAddressId");
            if (emailAddressId <= 0) {
                return null;
            }

            DataLoader<Long, EmailAddress> dataLoader = environment.getDataLoader(EmailAddressBatchLoader.KEY);
            return dataLoader.load(emailAddressId);
        };
    }

    @Override
    public DataFetcher<EmailAddress> createEmailAddressDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId");
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");
            String address = util.getStringArg(environment, "address");
            long typeId = util.getLongArg(environment, "typeId");
            boolean primary = util.getBooleanArg(environment, "primary");
            ServiceContext serviceContext = new ServiceContext();

            return emailaddressLocalService.addEmailAddress(
                    userId,
                    className,
                    classPK,
                    address,
                    typeId,
                    primary,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<EmailAddress> updateEmailAddressDataFetcher() {
        return environment -> {
            long emailAddressId = util.getLongArg(environment, "emailAddressId");
            String address = util.getStringArg(environment, "address");
            long typeId = util.getLongArg(environment, "typeId");
            boolean primary = util.getBooleanArg(environment, "primary");

            return emailaddressLocalService.updateEmailAddress(
                    emailAddressId,
                    address,
                    typeId,
                    primary);
        };
    }

    @Override
    public DataFetcher<EmailAddress> deleteEmailAddressDataFetcher() {
        return environment -> {
            long emailAddressId = util.getLongArg(environment, "emailAddressId");

            return emailaddressLocalService.deleteEmailAddress(emailAddressId);
        };
    }
}
