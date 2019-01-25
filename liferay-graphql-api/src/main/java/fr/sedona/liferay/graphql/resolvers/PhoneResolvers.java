package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Phone;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface PhoneResolvers {

    DataFetcher<List<Phone>> getPhonesDataFetcher();

    DataFetcher<List<Phone>> getPhonesForEntityDataFetcher();

    DataFetcher<CompletableFuture<Phone>> getPhoneDataFetcher();

    DataFetcher<Phone> createPhoneDataFetcher();

    DataFetcher<Phone> updatePhoneDataFetcher();

    DataFetcher<Phone> deletePhoneDataFetcher();
}
