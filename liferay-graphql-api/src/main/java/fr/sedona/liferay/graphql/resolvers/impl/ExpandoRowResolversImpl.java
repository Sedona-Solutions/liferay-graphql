package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.expando.kernel.model.ExpandoRow;
import com.liferay.expando.kernel.service.ExpandoRowLocalService;
import fr.sedona.liferay.graphql.loaders.ExpandoRowBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ExpandoRowResolvers;
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
        service = ExpandoRowResolvers.class
)
@SuppressWarnings("squid:S1192")
public class ExpandoRowResolversImpl implements ExpandoRowResolvers {
    private ExpandoRowLocalService expandoRowLocalService;

    @Reference(unbind = "-")
    public void setExpandoRowLocalService(ExpandoRowLocalService expandoRowLocalService) {
        this.expandoRowLocalService = expandoRowLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<ExpandoRow>> getExpandoRowsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return expandoRowLocalService.getExpandoRows(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<ExpandoRow>> getExpandoRowDataFetcher() {
        return environment -> {
            long rowId = getRowId(environment);
            if (rowId <= 0) {
                return null;
            }

            DataLoader<Long, ExpandoRow> dataLoader = environment.getDataLoader(ExpandoRowBatchLoader.KEY);
            return dataLoader.load(rowId);
        };
    }

    private long getRowId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "rowId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getRowId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<ExpandoRow> createExpandoRowDataFetcher() {
        return environment -> {
            long tableId = util.getLongArg(environment, "tableId");
            long classPK = util.getLongArg(environment, "classPK");

            return expandoRowLocalService.addRow(
                    tableId,
                    classPK);
        };
    }

    @Override
    public DataFetcher<ExpandoRow> deleteExpandoRowDataFetcher() {
        return environment -> {
            long rowId = util.getLongArg(environment, "rowId");

            return expandoRowLocalService.deleteExpandoRow(rowId);
        };
    }
}
