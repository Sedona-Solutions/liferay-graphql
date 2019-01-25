package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import fr.sedona.liferay.graphql.loaders.CompanyBatchLoader;
import fr.sedona.liferay.graphql.resolvers.CompanyResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = CompanyResolvers.class
)
@SuppressWarnings("squid:S1192")
public class CompanyResolversImpl implements CompanyResolvers {
    private CompanyLocalService companyLocalService;

    @Reference(unbind = "-")
    public void setCompanyLocalService(CompanyLocalService companyLocalService) {
        this.companyLocalService = companyLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<Company>> getCompaniesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return companyLocalService.getCompanies(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<Company>> getCompanyDataFetcher() {
        return environment -> {
            long companyId = getCompanyId(environment);
            if (companyId <= 0) {
                return null;
            }

            DataLoader<Long, Company> dataLoader = environment.getDataLoader(CompanyBatchLoader.KEY);
            return dataLoader.load(companyId);
        };
    }

    private long getCompanyId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "companyId");
        if(environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getCompanyId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<Company> getCompanyByWebIdDataFetcher() {
        return environment -> {
            String webId = util.getStringArg(environment, "webId");

            return companyLocalService.getCompanyByWebId(webId);
        };
    }

    @Override
    public DataFetcher<Company> createCompanyDataFetcher() {
        return environment -> {
            String webId = util.getStringArg(environment, "webId");
            String virtualHostname = util.getStringArg(environment, "virtualHostname");
            String mx = util.getStringArg(environment, "mx");
            boolean system = util.getBooleanArg(environment, "system");
            int maxUsers = util.getIntArg(environment, "maxUsers");
            boolean active = util.getBooleanArg(environment, "active", true);

            return companyLocalService.addCompany(
                    webId,
                    virtualHostname,
                    mx,
                    system,
                    maxUsers,
                    active);
        };
    }

    @Override
    public DataFetcher<Company> updateCompanyDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String virtualHostname = util.getStringArg(environment, "virtualHostname");
            String mx = util.getStringArg(environment, "mx");
            int maxUsers = util.getIntArg(environment, "maxUsers");
            boolean active = util.getBooleanArg(environment, "active", true);

            return companyLocalService.updateCompany(
                    companyId,
                    virtualHostname,
                    mx,
                    maxUsers,
                    active);
        };
    }

    @Override
    public DataFetcher<Company> deleteCompanyDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");

            return companyLocalService.deleteCompany(companyId);
        };
    }
}
