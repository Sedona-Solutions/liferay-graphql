package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.expando.kernel.model.ExpandoValue;
import com.liferay.expando.kernel.service.ExpandoValueLocalService;
import fr.sedona.liferay.graphql.loaders.ExpandoValueBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ExpandoValueResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = ExpandoValueResolvers.class
)
@SuppressWarnings("squid:S1192")
public class ExpandoValueResolversImpl implements ExpandoValueResolvers {
    private ExpandoValueLocalService expandoValueLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setExpandoValueLocalService(ExpandoValueLocalService expandoValueLocalService) {
        this.expandoValueLocalService = expandoValueLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<ExpandoValue>> getExpandoValuesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return expandoValueLocalService.getExpandoValues(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<ExpandoValue>> getExpandoValueDataFetcher() {
        return environment -> {
            long valueId = util.getLongArg(environment, "valueId");
            if (valueId <= 0) {
                return null;
            }

            DataLoader<Long, ExpandoValue> dataLoader = environment.getDataLoader(ExpandoValueBatchLoader.KEY);
            return dataLoader.load(valueId);
        };
    }

    @Override
    public DataFetcher<ExpandoValue> createExpandoValueForStringDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String className = util.getStringArg(environment, "className");
            String tableName = util.getStringArg(environment, "tableName");
            String columnName = util.getStringArg(environment, "columnName");
            long classPK = util.getLongArg(environment, "classPK");
            String data = util.getStringArg(environment, "data");

            return expandoValueLocalService.addValue(
                    companyId,
                    className,
                    tableName,
                    columnName,
                    classPK,
                    data);
        };
    }

    @Override
    public DataFetcher<ExpandoValue> createExpandoValueForIntDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String className = util.getStringArg(environment, "className");
            String tableName = util.getStringArg(environment, "tableName");
            String columnName = util.getStringArg(environment, "columnName");
            long classPK = util.getLongArg(environment, "classPK");
            int data = util.getIntArg(environment, "data");

            return expandoValueLocalService.addValue(
                    companyId,
                    className,
                    tableName,
                    columnName,
                    classPK,
                    data);
        };
    }

    @Override
    public DataFetcher<ExpandoValue> createExpandoValueForLongDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String className = util.getStringArg(environment, "className");
            String tableName = util.getStringArg(environment, "tableName");
            String columnName = util.getStringArg(environment, "columnName");
            long classPK = util.getLongArg(environment, "classPK");
            long data = util.getLongArg(environment, "data");

            return expandoValueLocalService.addValue(
                    companyId,
                    className,
                    tableName,
                    columnName,
                    classPK,
                    data);
        };
    }

    @Override
    public DataFetcher<ExpandoValue> createExpandoValueForDoubleDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String className = util.getStringArg(environment, "className");
            String tableName = util.getStringArg(environment, "tableName");
            String columnName = util.getStringArg(environment, "columnName");
            long classPK = util.getLongArg(environment, "classPK");
            double data = util.getDoubleArg(environment, "data");

            return expandoValueLocalService.addValue(
                    companyId,
                    className,
                    tableName,
                    columnName,
                    classPK,
                    data);
        };
    }

    @Override
    public DataFetcher<ExpandoValue> createExpandoValueForBooleanDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String className = util.getStringArg(environment, "className");
            String tableName = util.getStringArg(environment, "tableName");
            String columnName = util.getStringArg(environment, "columnName");
            long classPK = util.getLongArg(environment, "classPK");
            boolean data = util.getBooleanArg(environment, "data");

            return expandoValueLocalService.addValue(
                    companyId,
                    className,
                    tableName,
                    columnName,
                    classPK,
                    data);
        };
    }

    @Override
    public DataFetcher<ExpandoValue> deleteExpandoValueDataFetcher() {
        return environment -> {
            long valueId = util.getLongArg(environment, "valueId");

            return expandoValueLocalService.deleteExpandoValue(valueId);
        };
    }
}
