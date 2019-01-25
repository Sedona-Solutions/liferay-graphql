package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.expando.kernel.model.ExpandoTable;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface ExpandoTableResolvers {

    DataFetcher<List<ExpandoTable>> getExpandoTablesDataFetcher();

    DataFetcher<CompletableFuture<ExpandoTable>> getExpandoTableDataFetcher();

    DataFetcher<ExpandoTable> createExpandoTableDataFetcher();

    DataFetcher<ExpandoTable> updateExpandoTableDataFetcher();

    DataFetcher<ExpandoTable> deleteExpandoTableDataFetcher();
}
