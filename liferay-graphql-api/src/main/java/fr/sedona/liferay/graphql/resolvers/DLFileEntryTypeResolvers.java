package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface DLFileEntryTypeResolvers {

    DataFetcher<List<DLFileEntryType>> getDLFileEntryTypesDataFetcher();

    DataFetcher<List<DLFileEntryType>> getDLFileEntryTypesForFolderDataFetcher();

    DataFetcher<CompletableFuture<DLFileEntryType>> getDLFileEntryTypeDataFetcher();

    DataFetcher<DLFileEntryType> createDLFileEntryTypeDataFetcher();

    DataFetcher<DLFileEntryType> updateDLFileEntryTypeDataFetcher();

    DataFetcher<DLFileEntryType> deleteDLFileEntryTypeDataFetcher();
}
