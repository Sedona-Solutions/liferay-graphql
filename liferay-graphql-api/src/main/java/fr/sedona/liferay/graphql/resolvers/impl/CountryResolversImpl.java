package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.service.CountryService;
import fr.sedona.liferay.graphql.resolvers.CountryResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(
        immediate = true,
        service = CountryResolvers.class
)
@SuppressWarnings("squid:S1192")
public class CountryResolversImpl implements CountryResolvers {
    private CountryService countryService;

    @Reference(unbind = "-")
    public void setCountryService(CountryService countryService) {
        this.countryService = countryService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<Country>> getCountriesDataFetcher() {
        return environment -> {
            boolean active = util.getBooleanArg(environment, "active", true);

            return countryService.getCountries(active);
        };
    }

    @Override
    public DataFetcher<Country> getCountryDataFetcher() {
        return environment -> {
            long countryId = getCountryId(environment);
            if (countryId <= 0) {
                return null;
            }

            return countryService.getCountry(countryId);
        };
    }

    private long getCountryId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "countryId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getCountryId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<Country> getCountryByA2DataFetcher() {
        return environment -> {
            String a2 = util.getStringArg(environment, "a2");

            return countryService.getCountryByA2(a2);
        };
    }

    @Override
    public DataFetcher<Country> getCountryByA3DataFetcher() {
        return environment -> {
            String a3 = util.getStringArg(environment, "a3");

            return countryService.getCountryByA3(a3);
        };
    }

    @Override
    public DataFetcher<Country> getCountryByNameDataFetcher() {
        return environment -> {
            String name = util.getStringArg(environment, "name");

            return countryService.getCountryByName(name);
        };
    }

    @Override
    public DataFetcher<Country> createCountryDataFetcher() {
        return environment -> {
            String name = util.getStringArg(environment, "name");
            String a2 = util.getStringArg(environment, "a2");
            String a3 = util.getStringArg(environment, "a3");
            String number = util.getStringArg(environment, "number");
            String idd = util.getStringArg(environment, "idd");
            boolean active = util.getBooleanArg(environment, "active");

            return countryService.addCountry(
                    name,
                    a2,
                    a3,
                    number,
                    idd,
                    active);
        };
    }
}
