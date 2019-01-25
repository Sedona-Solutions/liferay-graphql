package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.User;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface UserResolvers {

    DataFetcher<List<User>> getUsersDataFetcher();

    DataFetcher<CompletableFuture<List<User>>> getBulkUsersDataFetcher();

    DataFetcher<CompletableFuture<User>> getUserDataFetcher();

    DataFetcher<User> getUserByEmailDataFetcher();

    DataFetcher<User> getUserByScreenNameDataFetcher();

    DataFetcher<User> createUserDataFetcher();

    DataFetcher<User> updateUserDataFetcher();

    DataFetcher<User> deleteUserDataFetcher();
}
