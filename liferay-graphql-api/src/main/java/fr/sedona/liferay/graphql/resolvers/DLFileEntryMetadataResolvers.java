package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.document.library.kernel.model.DLFileEntryMetadata;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface DLFileEntryMetadataResolvers {

    DataFetcher<List<DLFileEntryMetadata>> getDLFileEntryMetadatasDataFetcher();

    DataFetcher<CompletableFuture<DLFileEntryMetadata>> getDLFileEntryMetadataDataFetcher();
}
