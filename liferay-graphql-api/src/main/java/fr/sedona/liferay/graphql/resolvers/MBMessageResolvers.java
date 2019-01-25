package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.message.boards.kernel.model.MBMessage;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface MBMessageResolvers {

    DataFetcher<List<MBMessage>> getMBMessagesDataFetcher();

    DataFetcher<CompletableFuture<MBMessage>> getMBMessageDataFetcher();

    DataFetcher<MBMessage> createMBMessageDataFetcher();

    DataFetcher<MBMessage> updateMBMessageDataFetcher();

    DataFetcher<MBMessage> deleteMBMessageDataFetcher();
}
