package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.document.library.kernel.model.DLFolder;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface DLFolderResolvers {

    DataFetcher<List<DLFolder>> getDLFoldersDataFetcher();

    DataFetcher<List<DLFolder>> getDLFoldersForTypeDataFetcher();

    DataFetcher<CompletableFuture<DLFolder>> getDLFolderDataFetcher();

    DataFetcher<DLFolder> createDLFolderDataFetcher();

    DataFetcher<DLFolder> updateDLFolderDataFetcher();

    DataFetcher<DLFolder> deleteDLFolderDataFetcher();
}
