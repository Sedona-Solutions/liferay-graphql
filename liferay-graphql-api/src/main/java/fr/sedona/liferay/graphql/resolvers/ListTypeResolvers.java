package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.ListType;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface ListTypeResolvers {

    DataFetcher<List<ListType>> getListTypesDataFetcher();

    DataFetcher<List<ListType>> getListTypesByTypeDataFetcher();

    DataFetcher<CompletableFuture<ListType>> getListTypeDataFetcher();

    DataFetcher<ListType> createListTypeDataFetcher();

    DataFetcher<ListType> deleteListTypeDataFetcher();
}
