package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.service.ExpandoTableLocalService;
import fr.sedona.liferay.graphql.loaders.ExpandoTableBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ExpandoTableResolvers;
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
        service = ExpandoTableResolvers.class
)
@SuppressWarnings("squid:S1192")
public class ExpandoTableResolversImpl implements ExpandoTableResolvers {
    private ExpandoTableLocalService expandoTableLocalService;

    @Reference(unbind = "-")
    public void setExpandoTableLocalService(ExpandoTableLocalService expandoTableLocalService) {
        this.expandoTableLocalService = expandoTableLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<ExpandoTable>> getExpandoTablesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return expandoTableLocalService.getExpandoTables(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<ExpandoTable>> getExpandoTableDataFetcher() {
        return environment -> {
            long tableId = getTableId(environment);
            if (tableId <= 0) {
                return null;
            }

            DataLoader<Long, ExpandoTable> dataLoader = environment.getDataLoader(ExpandoTableBatchLoader.KEY);
            return dataLoader.load(tableId);
        };
    }

    private long getTableId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "tableId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getTableId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<ExpandoTable> createExpandoTableDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String className = util.getStringArg(environment, "className");
            String name = util.getStringArg(environment, "name");

            return expandoTableLocalService.addTable(
                    companyId,
                    className,
                    name);
        };
    }

    @Override
    public DataFetcher<ExpandoTable> updateExpandoTableDataFetcher() {
        return environment -> {
            long tableId = util.getLongArg(environment, "tableId");
            String name = util.getStringArg(environment, "name");

            return expandoTableLocalService.updateTable(
                    tableId,
                    name);
        };
    }

    @Override
    public DataFetcher<ExpandoTable> deleteExpandoTableDataFetcher() {
        return environment -> {
            long tableId = util.getLongArg(environment, "tableId");

            return expandoTableLocalService.deleteExpandoTable(tableId);
        };
    }
}
