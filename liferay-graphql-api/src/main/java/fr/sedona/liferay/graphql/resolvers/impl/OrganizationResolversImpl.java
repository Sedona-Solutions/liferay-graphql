package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.OrganizationConstants;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.OrganizationBatchLoader;
import fr.sedona.liferay.graphql.resolvers.OrganizationResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = OrganizationResolvers.class
)
@SuppressWarnings("squid:S1192")
public class OrganizationResolversImpl implements OrganizationResolvers {
    private OrganizationLocalService organizationLocalService;

    @Reference(unbind = "-")
    public void setOrganizationLocalService(OrganizationLocalService organizationLocalService) {
        this.organizationLocalService = organizationLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<Organization>> getOrganizationsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return organizationLocalService.getOrganizations(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<Organization>> getOrganizationDataFetcher() {
        return environment -> {
            long organizationId = util.getLongArg(environment, "organizationId");
            if (organizationId <= 0) {
                return null;
            }

            DataLoader<Long, Organization> dataLoader = environment.getDataLoader(OrganizationBatchLoader.KEY);
            return dataLoader.load(organizationId);
        };
    }

    @Override
    public DataFetcher<Organization> getOrganizationByNameDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String name = util.getStringArg(environment, "name");

            return organizationLocalService.getOrganization(companyId, name);
        };
    }

    @Override
    public DataFetcher<Organization> createOrganizationDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUser().getUserId());
            long parentOrganizationId = util.getLongArg(environment, "parentOrganizationId", OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID);
            String name = util.getStringArg(environment, "name");
            String type = util.getStringArg(environment, "type");
            long regionId = util.getLongArg(environment, "regionId");
            long countryId = util.getLongArg(environment, "countryId");
            long statusId = util.getLongArg(environment, "statusId");
            String comments = util.getStringArg(environment, "comments");
            boolean site = util.getBooleanArg(environment, "site");
            ServiceContext serviceContext = new ServiceContext();

            return organizationLocalService.addOrganization(
                    userId,
                    parentOrganizationId,
                    name,
                    type,
                    regionId,
                    countryId,
                    statusId,
                    comments,
                    site,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<Organization> updateOrganizationDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            long organizationId = util.getLongArg(environment, "organizationId");
            long parentOrganizationId = util.getLongArg(environment, "parentOrganizationId", OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID);
            String name = util.getStringArg(environment, "name");
            String type = util.getStringArg(environment, "type");
            long regionId = util.getLongArg(environment, "regionId");
            long countryId = util.getLongArg(environment, "countryId");
            long statusId = util.getLongArg(environment, "statusId");
            String comments = util.getStringArg(environment, "comments");
            boolean site = util.getBooleanArg(environment, "site");
            ServiceContext serviceContext = new ServiceContext();

            return organizationLocalService.updateOrganization(
                    companyId,
                    organizationId,
                    parentOrganizationId,
                    name,
                    type,
                    regionId,
                    countryId,
                    statusId,
                    comments,
                    false,
                    null,
                    site,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<Organization> deleteOrganizationDataFetcher() {
        return environment -> {
            long organizationId = util.getLongArg(environment, "organizationId");

            return organizationLocalService.deleteOrganization(organizationId);
        };
    }
}
