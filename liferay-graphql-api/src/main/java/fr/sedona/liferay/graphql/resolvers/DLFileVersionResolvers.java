package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.document.library.kernel.model.DLFileVersion;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface DLFileVersionResolvers {

    DataFetcher<List<DLFileVersion>> getDLFileVersionsDataFetcher();

    DataFetcher<CompletableFuture<DLFileVersion>> getDLFileVersionDataFetcher();
}
