package fr.sedona.liferay.graphql.loaders;

import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.service.JournalFolderLocalService;
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
        service = JournalFolderBatchLoader.class
)
public class JournalFolderBatchLoader implements BatchLoader<Long, JournalFolder> {
    public static final String KEY = "journalFolder";
    private JournalFolderLocalService journalFolderLocalService;

    @Reference(unbind = "-")
    public void setJournalFolderLocalService(JournalFolderLocalService journalFolderLocalService) {
        this.journalFolderLocalService = journalFolderLocalService;
    }

    @Override
    public CompletionStage<List<JournalFolder>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(JournalFolder.class);
            query.add(PropertyFactoryUtil.forName("folderId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return journalFolderLocalService.dynamicQuery(query);
        });
    }
}
