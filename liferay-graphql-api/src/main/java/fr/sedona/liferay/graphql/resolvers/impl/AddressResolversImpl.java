package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.service.AddressLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.AddressBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AddressResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
    immediate = true,
    service = AddressResolvers.class
)
@SuppressWarnings("squid:S1192")
public class AddressResolversImpl implements AddressResolvers {
    private AddressLocalService addressLocalService;

    @Reference(unbind = "-")
    public void setAddressLocalService(AddressLocalService addressLocalService) {
        this.addressLocalService = addressLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<Address>> getAddressesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return addressLocalService.getAddresses(start, end);
        };
    }

    @Override
    public DataFetcher<List<Address>> getAddressesForEntityDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");

            return addressLocalService.getAddresses(companyId, className, classPK);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<Address>> getAddressDataFetcher() {
        return environment -> {
            long addressId = util.getLongArg(environment, "addressId");
            if (addressId <= 0) {
                return null;
            }

            DataLoader<Long, Address> dataLoader = environment.getDataLoader(AddressBatchLoader.KEY);
            return dataLoader.load(addressId);
        };
    }

    @Override
    public DataFetcher<Address> createAddressDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId");
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");
            String street1 = util.getStringArg(environment, "street1");
            String street2 = util.getStringArg(environment, "street2");
            String street3 = util.getStringArg(environment, "street3");
            String city = util.getStringArg(environment, "city");
            String zip = util.getStringArg(environment, "zip");
            long regionId = util.getLongArg(environment, "regionId");
            long countryId = util.getLongArg(environment, "countryId");
            long typeId = util.getLongArg(environment, "typeId");
            boolean mailing = util.getBooleanArg(environment, "mailing");
            boolean primary = util.getBooleanArg(environment, "primary");
            ServiceContext serviceContext = new ServiceContext();

            return addressLocalService.addAddress(
                    userId,
                    className,
                    classPK,
                    street1,
                    street2,
                    street3,
                    city,
                    zip,
                    regionId,
                    countryId,
                    typeId,
                    mailing,
                    primary,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<Address> updateAddressDataFetcher() {
        return environment -> {
            long addressId = util.getLongArg(environment, "addressId");
            String street1 = util.getStringArg(environment, "street1");
            String street2 = util.getStringArg(environment, "street2");
            String street3 = util.getStringArg(environment, "street3");
            String city = util.getStringArg(environment, "city");
            String zip = util.getStringArg(environment, "zip");
            long regionId = util.getLongArg(environment, "regionId");
            long countryId = util.getLongArg(environment, "countryId");
            long typeId = util.getLongArg(environment, "typeId");
            boolean mailing = util.getBooleanArg(environment, "mailing");
            boolean primary = util.getBooleanArg(environment, "primary");

            return addressLocalService.updateAddress(
                    addressId,
                    street1,
                    street2,
                    street3,
                    city,
                    zip,
                    regionId,
                    countryId,
                    typeId,
                    mailing,
                    primary);
        };
    }

    @Override
    public DataFetcher<Address> deleteAddressDataFetcher() {
        return environment -> {
            long addressId = util.getLongArg(environment, "addressId");

            return addressLocalService.deleteAddress(addressId);
        };
    }
}
