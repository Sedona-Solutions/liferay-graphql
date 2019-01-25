package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Region;
import graphql.schema.DataFetcher;

import java.util.List;

@ProviderType
public interface RegionResolvers {

    DataFetcher<List<Region>> getRegionsDataFetcher();

    DataFetcher<Region> getRegionDataFetcher();

    DataFetcher<Region> getRegionByCodeDataFetcher();

    DataFetcher<Region> createRegionDataFetcher();
}
