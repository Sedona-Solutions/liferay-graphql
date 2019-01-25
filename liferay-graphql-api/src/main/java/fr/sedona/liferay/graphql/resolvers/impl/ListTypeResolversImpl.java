package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Contact;
import com.liferay.portal.kernel.model.ListType;
import com.liferay.portal.kernel.service.ListTypeLocalService;
import fr.sedona.liferay.graphql.loaders.ListTypeBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ListTypeResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionPath;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = ListTypeResolvers.class
)
@SuppressWarnings("squid:S1192")
public class ListTypeResolversImpl implements ListTypeResolvers {
    private ListTypeLocalService listtypeLocalService;

    @Reference(unbind = "-")
    public void setListTypeLocalService(ListTypeLocalService listtypeLocalService) {
        this.listtypeLocalService = listtypeLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<ListType>> getListTypesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return listtypeLocalService.getListTypes(start, end);
        };
    }

    @Override
    public DataFetcher<List<ListType>> getListTypesByTypeDataFetcher() {
        return environment -> {
            String type = util.getStringArg(environment, "type");

            return listtypeLocalService.getListTypes(type);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<ListType>> getListTypeDataFetcher() {
        return environment -> {
            long listTypeId = getListTypeId(environment);
            if (listTypeId <= 0) {
                return null;
            }

            DataLoader<Long, ListType> dataLoader = environment.getDataLoader(ListTypeBatchLoader.KEY);
            return dataLoader.load(listTypeId);
        };
    }

    private long getListTypeId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "listTypeId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof Contact) {
            ExecutionPath segment = environment.getExecutionStepInfo().getPath();
            if (segment.getSegmentName().contains("prefix")) {
                return ((Contact) source).getPrefixId();
            } else if (segment.getSegmentName().contains("suffix")) {
                return ((Contact) source).getSuffixId();
            }
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getTypeId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<ListType> createListTypeDataFetcher() {
        return environment -> {
            String name = util.getStringArg(environment, "name");
            String type = util.getStringArg(environment, "type");

            return listtypeLocalService.addListType(name, type);
        };
    }

    @Override
    public DataFetcher<ListType> deleteListTypeDataFetcher() {
        return environment -> {
            long listTypeId = util.getLongArg(environment, "listTypeId");

            return listtypeLocalService.deleteListType(listTypeId);
        };
    }
}
