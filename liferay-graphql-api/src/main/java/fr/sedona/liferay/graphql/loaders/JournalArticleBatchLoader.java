package fr.sedona.liferay.graphql.loaders;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
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
        service = JournalArticleBatchLoader.class
)
public class JournalArticleBatchLoader implements BatchLoader<Long, JournalArticle> {
    public static final String KEY = "journalArticle";
    private JournalArticleLocalService journalArticleLocalService;

    @Reference(unbind = "-")
    public void setJournalArticleLocalService(JournalArticleLocalService journalArticleLocalService) {
        this.journalArticleLocalService = journalArticleLocalService;
    }

    @Override
    public CompletionStage<List<JournalArticle>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(JournalArticle.class);
            query.add(PropertyFactoryUtil.forName("id")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return journalArticleLocalService.dynamicQuery(query);
        });
    }
}
