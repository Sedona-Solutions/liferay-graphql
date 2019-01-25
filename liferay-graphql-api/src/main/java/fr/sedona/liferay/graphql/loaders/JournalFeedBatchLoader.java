package fr.sedona.liferay.graphql.loaders;

import com.liferay.journal.model.JournalFeed;
import com.liferay.journal.service.JournalFeedLocalService;
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
        service = JournalFeedBatchLoader.class
)
public class JournalFeedBatchLoader implements BatchLoader<Long, JournalFeed> {
    public static final String KEY = "journalFeed";
    private JournalFeedLocalService journalFeedLocalService;

    @Reference(unbind = "-")
    public void setJournalFeedLocalService(JournalFeedLocalService journalFeedLocalService) {
        this.journalFeedLocalService = journalFeedLocalService;
    }

    @Override
    public CompletionStage<List<JournalFeed>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(JournalFeed.class);
            query.add(PropertyFactoryUtil.forName("id")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return journalFeedLocalService.dynamicQuery(query);
        });
    }
}
