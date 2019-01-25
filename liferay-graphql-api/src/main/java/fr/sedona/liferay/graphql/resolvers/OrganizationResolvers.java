package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Organization;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface OrganizationResolvers {

    DataFetcher<List<Organization>> getOrganizationsDataFetcher();

    DataFetcher<CompletableFuture<Organization>> getOrganizationDataFetcher();

    DataFetcher<Organization> getOrganizationByNameDataFetcher();

    DataFetcher<Organization> createOrganizationDataFetcher();

    DataFetcher<Organization> updateOrganizationDataFetcher();

    DataFetcher<Organization> deleteOrganizationDataFetcher();
}
