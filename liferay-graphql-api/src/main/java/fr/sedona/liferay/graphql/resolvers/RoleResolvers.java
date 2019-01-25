package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Role;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface RoleResolvers {

    DataFetcher<List<Role>> getRolesDataFetcher();

    DataFetcher<CompletableFuture<Role>> getRoleDataFetcher();

    DataFetcher<Role> getRoleByNameDataFetcher();

    DataFetcher<Role> createRoleDataFetcher();

    DataFetcher<Role> updateRoleDataFetcher();

    DataFetcher<Role> deleteRoleDataFetcher();
}
