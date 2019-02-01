package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.message.boards.kernel.model.MBMessage;
import com.liferay.message.boards.kernel.model.MBThread;
import com.liferay.message.boards.kernel.service.MBThreadLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.MBThreadBatchLoader;
import fr.sedona.liferay.graphql.resolvers.MBThreadResolvers;
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
        service = MBThreadResolvers.class
)
@SuppressWarnings("squid:S1192")
public class MBThreadResolversImpl implements MBThreadResolvers {
    private MBThreadLocalService mbThreadLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setMBThreadLocalService(MBThreadLocalService mbThreadLocalService) {
        this.mbThreadLocalService = mbThreadLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<MBThread>> getMBThreadsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return mbThreadLocalService.getMBThreads(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<MBThread>> getMBThreadDataFetcher() {
        return environment -> {
            long threadId = getThreadId(environment);
            if (threadId <= 0) {
                return null;
            }

            DataLoader<Long, MBThread> dataLoader = environment.getDataLoader(MBThreadBatchLoader.KEY);
            return dataLoader.load(threadId);
        };
    }

    private long getThreadId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "threadId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getThreadId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<MBThread> createMBThreadDataFetcher() {
        return environment -> {
            long categoryId = util.getLongArg(environment, "categoryId");
            MBMessage message = util.getMBMessageArg(environment, "message");
            ServiceContext serviceContext = new ServiceContext();

            return mbThreadLocalService.addThread(
                    categoryId,
                    message,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<MBThread> deleteMBThreadDataFetcher() {
        return environment -> {
            long threadId = util.getLongArg(environment, "threadId");

            return mbThreadLocalService.deleteMBThread(threadId);
        };
    }
}
