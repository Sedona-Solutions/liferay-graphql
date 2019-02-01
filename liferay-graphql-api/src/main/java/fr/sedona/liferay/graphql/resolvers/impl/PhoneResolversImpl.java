package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Phone;
import com.liferay.portal.kernel.service.PhoneLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.PhoneBatchLoader;
import fr.sedona.liferay.graphql.resolvers.PhoneResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = PhoneResolvers.class
)
@SuppressWarnings("squid:S1192")
public class PhoneResolversImpl implements PhoneResolvers {
    private PhoneLocalService phoneLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setPhoneLocalService(PhoneLocalService phoneLocalService) {
        this.phoneLocalService = phoneLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<Phone>> getPhonesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return phoneLocalService.getPhones(start, end);
        };
    }

    @Override
    public DataFetcher<List<Phone>> getPhonesForEntityDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");

            return phoneLocalService.getPhones(companyId, className, classPK);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<Phone>> getPhoneDataFetcher() {
        return environment -> {
            long phoneId = util.getLongArg(environment, "phoneId");
            if (phoneId <= 0) {
                return null;
            }

            DataLoader<Long, Phone> dataLoader = environment.getDataLoader(PhoneBatchLoader.KEY);
            return dataLoader.load(phoneId);
        };
    }

    @Override
    public DataFetcher<Phone> createPhoneDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");
            String number = util.getStringArg(environment, "number");
            String extension = util.getStringArg(environment, "extension");
            long typeId = util.getLongArg(environment, "typeId");
            boolean primary = util.getBooleanArg(environment, "primary");
            ServiceContext serviceContext = new ServiceContext();

            return phoneLocalService.addPhone(
                    userId,
                    className,
                    classPK,
                    number,
                    extension,
                    typeId,
                    primary,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<Phone> updatePhoneDataFetcher() {
        return environment -> {
            long phoneId = util.getLongArg(environment, "phoneId");
            String number = util.getStringArg(environment, "number");
            String extension = util.getStringArg(environment, "extension");
            long typeId = util.getLongArg(environment, "typeId");
            boolean primary = util.getBooleanArg(environment, "primary");

            return phoneLocalService.updatePhone(
                    phoneId,
                    number,
                    extension,
                    typeId,
                    primary);
        };
    }

    @Override
    public DataFetcher<Phone> deletePhoneDataFetcher() {
        return environment -> {
            long phoneId = util.getLongArg(environment, "phoneId");

            return phoneLocalService.deletePhone(phoneId);
        };
    }
}
