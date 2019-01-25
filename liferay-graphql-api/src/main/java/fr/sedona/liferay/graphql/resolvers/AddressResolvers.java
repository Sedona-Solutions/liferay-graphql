package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Address;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface AddressResolvers {

    DataFetcher<List<Address>> getAddressesDataFetcher();

    DataFetcher<List<Address>> getAddressesForEntityDataFetcher();

    DataFetcher<CompletableFuture<Address>> getAddressDataFetcher();

    DataFetcher<Address> createAddressDataFetcher();

    DataFetcher<Address> updateAddressDataFetcher();

    DataFetcher<Address> deleteAddressDataFetcher();
}
