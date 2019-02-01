package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.journal.model.JournalFeed;
import com.liferay.journal.service.JournalFeedLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.JournalFeedBatchLoader;
import fr.sedona.liferay.graphql.resolvers.JournalFeedResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = JournalFeedResolvers.class
)
@SuppressWarnings("squid:S1192")
public class JournalFeedResolversImpl implements JournalFeedResolvers {
    private JournalFeedLocalService journalFeedLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setJournalFeedLocalService(JournalFeedLocalService journalFeedLocalService) {
        this.journalFeedLocalService = journalFeedLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<JournalFeed>> getJournalFeedsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return journalFeedLocalService.getJournalFeeds(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<JournalFeed>> getJournalFeedDataFetcher() {
        return environment -> {
            long id = util.getLongArg(environment, "id");
            if (id <= 0) {
                return null;
            }

            DataLoader<Long, JournalFeed> dataLoader = environment.getDataLoader(JournalFeedBatchLoader.KEY);
            return dataLoader.load(id);
        };
    }

    @Override
    public DataFetcher<JournalFeed> createJournalFeedDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            String feedId = util.getStringArg(environment, "feedId");
            boolean autoFeedId = util.getBooleanArg(environment, "autoFeedId");
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            String ddmStructureKey = util.getStringArg(environment, "ddmStructureKey");
            String ddmTemplateKey = util.getStringArg(environment, "ddmTemplateKey");
            String ddmRendererTemplateKey = util.getStringArg(environment, "ddmRendererTemplateKey");
            int delta = util.getIntArg(environment, "delta");
            String orderByCol = util.getStringArg(environment, "orderByCol");
            String orderByType = util.getStringArg(environment, "orderByType");
            String targetLayoutFriendlyUrl = util.getStringArg(environment, "targetLayoutFriendlyUrl");
            String targetPortletId = util.getStringArg(environment, "targetPortletId");
            String contentField = util.getStringArg(environment, "contentField");
            String feedFormat = util.getStringArg(environment, "feedFormat");
            double feedVersion = util.getDoubleArg(environment, "feedVersion");
            ServiceContext serviceContext = new ServiceContext();

            return journalFeedLocalService.addFeed(
                    userId,
                    groupId,
                    feedId,
                    autoFeedId,
                    name,
                    description,
                    ddmStructureKey,
                    ddmTemplateKey,
                    ddmRendererTemplateKey,
                    delta,
                    orderByCol,
                    orderByType,
                    targetLayoutFriendlyUrl,
                    targetPortletId,
                    contentField,
                    feedFormat,
                    feedVersion,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<JournalFeed> updateJournalFeedDataFetcher() {
        return environment -> {
            long groupId = util.getLongArg(environment, "groupId");
            String feedId = util.getStringArg(environment, "feedId");
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            String ddmStructureKey = util.getStringArg(environment, "ddmStructureKey");
            String ddmTemplateKey = util.getStringArg(environment, "ddmTemplateKey");
            String ddmRendererTemplateKey = util.getStringArg(environment, "ddmRendererTemplateKey");
            int delta = util.getIntArg(environment, "delta");
            String orderByCol = util.getStringArg(environment, "orderByCol");
            String orderByType = util.getStringArg(environment, "orderByType");
            String targetLayoutFriendlyUrl = util.getStringArg(environment, "targetLayoutFriendlyUrl");
            String targetPortletId = util.getStringArg(environment, "targetPortletId");
            String contentField = util.getStringArg(environment, "contentField");
            String feedFormat = util.getStringArg(environment, "feedFormat");
            double feedVersion = util.getDoubleArg(environment, "feedVersion");
            ServiceContext serviceContext = new ServiceContext();

            return journalFeedLocalService.updateFeed(
                    groupId,
                    feedId,
                    name,
                    description,
                    ddmStructureKey,
                    ddmTemplateKey,
                    ddmRendererTemplateKey,
                    delta,
                    orderByCol,
                    orderByType,
                    targetLayoutFriendlyUrl,
                    targetPortletId,
                    contentField,
                    feedFormat,
                    feedVersion,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<JournalFeed> deleteJournalFeedDataFetcher() {
        return environment -> {
            long id = util.getLongArg(environment, "id");

            return journalFeedLocalService.deleteJournalFeed(id);
        };
    }
}
