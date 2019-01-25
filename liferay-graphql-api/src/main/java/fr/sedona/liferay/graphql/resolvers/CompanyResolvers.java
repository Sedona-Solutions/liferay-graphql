package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Company;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface CompanyResolvers {

    DataFetcher<List<Company>> getCompaniesDataFetcher();

    DataFetcher<CompletableFuture<Company>> getCompanyDataFetcher();

    DataFetcher<Company> getCompanyByWebIdDataFetcher();

    DataFetcher<Company> createCompanyDataFetcher();

    DataFetcher<Company> updateCompanyDataFetcher();

    DataFetcher<Company> deleteCompanyDataFetcher();
}
