package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.UserGroup;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface UserGroupResolvers {

    DataFetcher<List<UserGroup>> getUserGroupsDataFetcher();

    DataFetcher<CompletableFuture<UserGroup>> getUserGroupDataFetcher();

    DataFetcher<UserGroup> getUserGroupByNameDataFetcher();

    DataFetcher<UserGroup> createUserGroupDataFetcher();

    DataFetcher<UserGroup> updateUserGroupDataFetcher();

    DataFetcher<UserGroup> deleteUserGroupDataFetcher();
}
