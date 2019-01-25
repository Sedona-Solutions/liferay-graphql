package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.expando.kernel.model.ExpandoRow;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface ExpandoRowResolvers {

    DataFetcher<List<ExpandoRow>> getExpandoRowsDataFetcher();

    DataFetcher<CompletableFuture<ExpandoRow>> getExpandoRowDataFetcher();

    DataFetcher<ExpandoRow> createExpandoRowDataFetcher();

    DataFetcher<ExpandoRow> deleteExpandoRowDataFetcher();
}
