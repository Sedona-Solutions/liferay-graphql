package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.document.library.kernel.model.DLFileShortcut;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface DLFileShortcutResolvers {

    DataFetcher<List<DLFileShortcut>> getDLFileShortcutsDataFetcher();

    DataFetcher<CompletableFuture<DLFileShortcut>> getDLFileShortcutDataFetcher();

    DataFetcher<DLFileShortcut> createDLFileShortcutDataFetcher();

    DataFetcher<DLFileShortcut> updateDLFileShortcutDataFetcher();

    DataFetcher<DLFileShortcut> deleteDLFileShortcutDataFetcher();
}
