package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.message.boards.kernel.model.MBThread;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface MBThreadResolvers {

    DataFetcher<List<MBThread>> getMBThreadsDataFetcher();

    DataFetcher<CompletableFuture<MBThread>> getMBThreadDataFetcher();

    DataFetcher<MBThread> createMBThreadDataFetcher();

    DataFetcher<MBThread> deleteMBThreadDataFetcher();
}
