package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.expando.kernel.model.ExpandoValue;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface ExpandoValueResolvers {

    DataFetcher<List<ExpandoValue>> getExpandoValuesDataFetcher();

    DataFetcher<CompletableFuture<ExpandoValue>> getExpandoValueDataFetcher();

    DataFetcher<ExpandoValue> createExpandoValueForStringDataFetcher();

    DataFetcher<ExpandoValue> createExpandoValueForIntDataFetcher();

    DataFetcher<ExpandoValue> createExpandoValueForLongDataFetcher();

    DataFetcher<ExpandoValue> createExpandoValueForDoubleDataFetcher();

    DataFetcher<ExpandoValue> createExpandoValueForBooleanDataFetcher();

    DataFetcher<ExpandoValue> deleteExpandoValueDataFetcher();
}
