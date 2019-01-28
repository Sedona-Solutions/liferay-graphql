package fr.sedona.liferay.graphql.loaders;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = DLFileEntryBatchLoader.class
)
public class DLFileEntryBatchLoader implements BatchLoader<Long, DLFileEntry> {
    public static final String KEY = "dlFileEntry";
    private DLFileEntryLocalService dlFileEntryLocalService;

    @Reference(unbind = "-")
    public void setDLFileEntryLocalService(DLFileEntryLocalService dlFileEntryLocalService) {
        this.dlFileEntryLocalService = dlFileEntryLocalService;
    }

    @Override
    public CompletionStage<List<DLFileEntry>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DLFileEntry.class);
            query.add(PropertyFactoryUtil.forName("fileEntryId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return dlFileEntryLocalService.dynamicQuery(query);
        });
    }
}
