package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Group;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface GroupResolvers {

    DataFetcher<List<Group>> getGroupsDataFetcher();

    DataFetcher<CompletableFuture<Group>> getGroupDataFetcher();

    DataFetcher<Group> getGroupByKeyDataFetcher();

    DataFetcher<Group> createGroupDataFetcher();

    DataFetcher<Group> updateGroupDataFetcher();

    DataFetcher<Group> deleteGroupDataFetcher();
}
