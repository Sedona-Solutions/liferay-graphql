package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.journal.model.JournalFolder;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface JournalFolderResolvers {

    DataFetcher<List<JournalFolder>> getJournalFoldersDataFetcher();

    DataFetcher<CompletableFuture<JournalFolder>> getJournalFolderDataFetcher();

    DataFetcher<JournalFolder> createJournalFolderDataFetcher();

    DataFetcher<JournalFolder> updateJournalFolderDataFetcher();

    DataFetcher<JournalFolder> deleteJournalFolderDataFetcher();
}
