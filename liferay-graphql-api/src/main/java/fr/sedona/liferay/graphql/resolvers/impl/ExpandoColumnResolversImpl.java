package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.service.ExpandoColumnLocalService;
import fr.sedona.liferay.graphql.loaders.ExpandoColumnBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ExpandoColumnResolvers;
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
        service = ExpandoColumnResolvers.class
)
@SuppressWarnings("squid:S1192")
public class ExpandoColumnResolversImpl implements ExpandoColumnResolvers {
    private ExpandoColumnLocalService expandoColumnLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setExpandoColumnLocalService(ExpandoColumnLocalService expandoColumnLocalService) {
        this.expandoColumnLocalService = expandoColumnLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<ExpandoColumn>> getExpandoColumnsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return expandoColumnLocalService.getExpandoColumns(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<ExpandoColumn>> getExpandoColumnDataFetcher() {
        return environment -> {
            long columnId = getColumnId(environment);
            if (columnId <= 0) {
                return null;
            }

            DataLoader<Long, ExpandoColumn> dataLoader = environment.getDataLoader(ExpandoColumnBatchLoader.KEY);
            return dataLoader.load(columnId);
        };
    }

    private long getColumnId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "columnId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getColumnId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<ExpandoColumn> createExpandoColumnDataFetcher() {
        return environment -> {
            long tableId = util.getLongArg(environment, "tableId");
            String name = util.getStringArg(environment, "name");
            int type = util.getIntArg(environment, "type", ExpandoColumnConstants.STRING);

            return expandoColumnLocalService.addColumn(
                    tableId,
                    name,
                    type,
                    null);
        };
    }

    @Override
    public DataFetcher<ExpandoColumn> updateExpandoColumnDataFetcher() {
        return environment -> {
            long columnId = util.getLongArg(environment, "columnId");
            String name = util.getStringArg(environment, "name");
            int type = util.getIntArg(environment, "type", ExpandoColumnConstants.STRING);

            return expandoColumnLocalService.updateColumn(
                    columnId,
                    name,
                    type,
                    null);
        };
    }

    @Override
    public DataFetcher<ExpandoColumn> deleteExpandoColumnDataFetcher() {
        return environment -> {
            long columnId = util.getLongArg(environment, "columnId");

            return expandoColumnLocalService.deleteExpandoColumn(columnId);
        };
    }
}
