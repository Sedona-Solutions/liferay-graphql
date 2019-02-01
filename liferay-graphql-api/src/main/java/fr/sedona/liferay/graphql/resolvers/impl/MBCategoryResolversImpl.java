package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.message.boards.kernel.model.MBCategory;
import com.liferay.message.boards.kernel.model.MBCategoryConstants;
import com.liferay.message.boards.kernel.service.MBCategoryLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.MBCategoryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.MBCategoryResolvers;
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
        service = MBCategoryResolvers.class
)
@SuppressWarnings("squid:S1192")
public class MBCategoryResolversImpl implements MBCategoryResolvers {
    private MBCategoryLocalService mbCategoryLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setMBCategoryLocalService(MBCategoryLocalService mbCategoryLocalService) {
        this.mbCategoryLocalService = mbCategoryLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<MBCategory>> getMBCategoriesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return mbCategoryLocalService.getMBCategories(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<MBCategory>> getMBCategoryDataFetcher() {
        return environment -> {
            long categoryId = getCategoryId(environment);
            if (categoryId <= 0) {
                return null;
            }

            DataLoader<Long, MBCategory> dataLoader = environment.getDataLoader(MBCategoryBatchLoader.KEY);
            return dataLoader.load(categoryId);
        };
    }

    private long getCategoryId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "categoryId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof MBCategory) {
            ExecutionPath segment = environment.getExecutionStepInfo().getPath();
            if (segment.getSegmentName().contains("parentCategory")) {
                return ((MBCategory) source).getParentCategoryId();
            }
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getCategory");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<MBCategory> createMBCategoryDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long parentCategoryId = util.getLongArg(environment, "parentCategoryId");
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            ServiceContext serviceContext = new ServiceContext();

            return mbCategoryLocalService.addCategory(
                    userId,
                    parentCategoryId,
                    name,
                    description,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<MBCategory> updateMBCategoryDataFetcher() {
        return environment -> {
            long categoryId = util.getLongArg(environment, "categoryId");
            long parentCategoryId = util.getLongArg(environment, "parentCategoryId");
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            ServiceContext serviceContext = new ServiceContext();

            return mbCategoryLocalService.updateCategory(
                    categoryId,
                    parentCategoryId,
                    name,
                    description,
                    MBCategoryConstants.DEFAULT_DISPLAY_STYLE,
                    null,
                    null,
                    null,
                    0,
                    false,
                    null,
                    null,
                    0,
                    null,
                    false,
                    null,
                    0,
                    false,
                    null,
                    null,
                    false,
                    false,
                    false,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<MBCategory> deleteMBCategoryDataFetcher() {
        return environment -> {
            long categoryId = util.getLongArg(environment, "categoryId");

            return mbCategoryLocalService.deleteMBCategory(categoryId);
        };
    }
}
