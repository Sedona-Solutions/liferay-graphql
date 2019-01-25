package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.ClassName;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface ClassNameResolvers {

    DataFetcher<List<ClassName>> getClassNamesDataFetcher();

    DataFetcher<CompletableFuture<ClassName>> getClassNameDataFetcher();

    DataFetcher<ClassName> getClassNameByNameDataFetcher();
}
