package fr.sedona.liferay.graphql.loaders;

import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalService;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = BlogsEntryBatchLoader.class
)
public class BlogsEntryBatchLoader implements BatchLoader<Long, BlogsEntry> {
    public static final String KEY = "blogsEntry";
    private BlogsEntryLocalService blogsEntryLocalService;

    @Reference(unbind = "-")
    public void setBlogsEntryLocalService(BlogsEntryLocalService blogsEntryLocalService) {
        this.blogsEntryLocalService = blogsEntryLocalService;
    }

    @Override
    public CompletionStage<List<BlogsEntry>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(BlogsEntry.class);
            query.add(PropertyFactoryUtil.forName("entryId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return blogsEntryLocalService.dynamicQuery(query);
        });
    }
}
