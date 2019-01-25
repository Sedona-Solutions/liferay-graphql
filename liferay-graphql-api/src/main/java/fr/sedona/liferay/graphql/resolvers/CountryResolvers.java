package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Country;
import graphql.schema.DataFetcher;

import java.util.List;

@ProviderType
public interface CountryResolvers {

    DataFetcher<List<Country>> getCountriesDataFetcher();

    DataFetcher<Country> getCountryDataFetcher();

    DataFetcher<Country> getCountryByA2DataFetcher();

    DataFetcher<Country> getCountryByA3DataFetcher();

    DataFetcher<Country> getCountryByNameDataFetcher();

    DataFetcher<Country> createCountryDataFetcher();
}
