package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.document.library.kernel.model.DLFileEntry;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface DLFileEntryResolvers {

    DataFetcher<List<DLFileEntry>> getDLFileEntriesDataFetcher();

    DataFetcher<CompletableFuture<DLFileEntry>> getDLFileEntryDataFetcher();

    DataFetcher<DLFileEntry> createDLFileEntryDataFetcher();

    DataFetcher<DLFileEntry> updateDLFileEntryDataFetcher();

    DataFetcher<DLFileEntry> deleteDLFileEntryDataFetcher();
}
