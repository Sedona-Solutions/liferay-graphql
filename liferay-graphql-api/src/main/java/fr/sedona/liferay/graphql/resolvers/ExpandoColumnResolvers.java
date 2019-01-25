package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.expando.kernel.model.ExpandoColumn;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface ExpandoColumnResolvers {

    DataFetcher<List<ExpandoColumn>> getExpandoColumnsDataFetcher();

    DataFetcher<CompletableFuture<ExpandoColumn>> getExpandoColumnDataFetcher();

    DataFetcher<ExpandoColumn> createExpandoColumnDataFetcher();

    DataFetcher<ExpandoColumn> updateExpandoColumnDataFetcher();

    DataFetcher<ExpandoColumn> deleteExpandoColumnDataFetcher();
}
