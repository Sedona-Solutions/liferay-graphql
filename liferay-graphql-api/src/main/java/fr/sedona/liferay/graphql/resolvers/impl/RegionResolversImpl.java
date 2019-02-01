package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.service.RegionService;
import fr.sedona.liferay.graphql.resolvers.RegionResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(
        immediate = true,
        service = RegionResolvers.class
)
@SuppressWarnings("squid:S1192")
public class RegionResolversImpl implements RegionResolvers {
    private RegionService regionService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setRegionService(RegionService regionService) {
        this.regionService = regionService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<Region>> getRegionsDataFetcher() {
        return environment -> {
            boolean active = util.getBooleanArg(environment, "active", true);

            return regionService.getRegions(active);
        };
    }

    @Override
    public DataFetcher<Region> getRegionDataFetcher() {
        return environment -> {
            long regionId = getRegionId(environment);
            if (regionId <= 0) {
                return null;
            }

            return regionService.getRegion(regionId);
        };
    }

    private long getRegionId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "regionId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getRegionId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<Region> getRegionByCodeDataFetcher() {
        return environment -> {
            long countryId = util.getLongArg(environment, "countryId");
            String regionCode = util.getStringArg(environment, "regionCode");

            return regionService.getRegion(countryId, regionCode);
        };
    }

    @Override
    public DataFetcher<Region> createRegionDataFetcher() {
        return environment -> {
            long countryId = util.getLongArg(environment, "countryId");
            String regionCode = util.getStringArg(environment, "regionCode");
            String name = util.getStringArg(environment, "name");
            boolean active = util.getBooleanArg(environment, "active");

            return regionService.addRegion(
                    countryId,
                    regionCode,
                    name,
                    active);
        };
    }
}
