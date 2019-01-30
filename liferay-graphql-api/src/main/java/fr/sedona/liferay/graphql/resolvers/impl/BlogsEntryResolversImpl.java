package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.BlogsEntryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.BlogsEntryResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = BlogsEntryResolvers.class
)
@SuppressWarnings("squid:S1192")
public class BlogsEntryResolversImpl implements BlogsEntryResolvers {
    private BlogsEntryLocalService blogsEntryLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setBlogsEntryLocalService(BlogsEntryLocalService blogsEntryLocalService) {
        this.blogsEntryLocalService = blogsEntryLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<BlogsEntry>> getBlogsEntriesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return blogsEntryLocalService.getBlogsEntries(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<BlogsEntry>> getBlogsEntryDataFetcher() {
        return environment -> {
            long entryId = util.getLongArg(environment, "entryId");
            if (entryId <= 0) {
                return null;
            }

            DataLoader<Long, BlogsEntry> dataLoader = environment.getDataLoader(BlogsEntryBatchLoader.KEY);
            return dataLoader.load(entryId);
        };
    }

    @Override
    public DataFetcher<BlogsEntry> createBlogsEntryDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId");
            String title = util.getStringArg(environment, "title");
            String subtitle = util.getStringArg(environment, "subtitle");
            String urlTitle = util.getStringArg(environment, "urlTitle");
            String description = util.getStringArg(environment, "description");
            String content = util.getStringArg(environment, "content");
            int displayDateMonth = util.getIntArg(environment, "displayDateMonth");
            int displayDateDay = util.getIntArg(environment, "displayDateDay");
            int displayDateYear = util.getIntArg(environment, "displayDateYear");
            int displayDateHour = util.getIntArg(environment, "displayDateHour");
            int displayDateMinute = util.getIntArg(environment, "displayDateMinute");
            boolean allowPingbacks = util.getBooleanArg(environment, "allowPingbacks");
            boolean allowTrackbacks = util.getBooleanArg(environment, "allowTrackbacks");
            String[] trackbacks = util.getStringArrayArg(environment, "trackbacks");
            String coverImageCaption = util.getStringArg(environment, "coverImageCaption");
            ServiceContext serviceContext = new ServiceContext();

            return blogsEntryLocalService.addEntry(
                    userId,
                    title,
                    subtitle,
                    urlTitle,
                    description,
                    content,
                    displayDateMonth,
                    displayDateDay,
                    displayDateYear,
                    displayDateHour,
                    displayDateMinute,
                    allowPingbacks,
                    allowTrackbacks,
                    trackbacks,
                    coverImageCaption,
                    null,
                    null,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<BlogsEntry> updateBlogsEntryDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId");
            long entryId = util.getLongArg(environment, "entryId");
            String title = util.getStringArg(environment, "title");
            String subtitle = util.getStringArg(environment, "subtitle");
            String urlTitle = util.getStringArg(environment, "urlTitle");
            String description = util.getStringArg(environment, "description");
            String content = util.getStringArg(environment, "content");
            int displayDateMonth = util.getIntArg(environment, "displayDateMonth");
            int displayDateDay = util.getIntArg(environment, "displayDateDay");
            int displayDateYear = util.getIntArg(environment, "displayDateYear");
            int displayDateHour = util.getIntArg(environment, "displayDateHour");
            int displayDateMinute = util.getIntArg(environment, "displayDateMinute");
            boolean allowPingbacks = util.getBooleanArg(environment, "allowPingbacks");
            boolean allowTrackbacks = util.getBooleanArg(environment, "allowTrackbacks");
            String[] trackbacks = util.getStringArrayArg(environment, "trackbacks");
            String coverImageCaption = util.getStringArg(environment, "coverImageCaption");
            ServiceContext serviceContext = new ServiceContext();

            return blogsEntryLocalService.updateEntry(
                    userId,
                    entryId,
                    title,
                    subtitle,
                    urlTitle,
                    description,
                    content,
                    displayDateMonth,
                    displayDateDay,
                    displayDateYear,
                    displayDateHour,
                    displayDateMinute,
                    allowPingbacks,
                    allowTrackbacks,
                    trackbacks,
                    coverImageCaption,
                    null,
                    null,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<BlogsEntry> deleteBlogsEntryDataFetcher() {
        return environment -> {
            long entryId = util.getLongArg(environment, "entryId");

            return blogsEntryLocalService.deleteBlogsEntry(entryId);
        };
    }
}
