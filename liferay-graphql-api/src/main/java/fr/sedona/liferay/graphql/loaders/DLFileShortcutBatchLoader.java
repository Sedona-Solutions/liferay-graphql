package fr.sedona.liferay.graphql.loaders;

import com.liferay.document.library.kernel.model.DLFileShortcut;
import com.liferay.document.library.kernel.service.DLFileShortcutLocalService;
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
        service = DLFileShortcutBatchLoader.class
)
public class DLFileShortcutBatchLoader implements BatchLoader<Long, DLFileShortcut> {
    public static final String KEY = "dlFileShortcut";
    private DLFileShortcutLocalService dlFileShortcutLocalService;

    @Reference(unbind = "-")
    public void setDLFileShortcutLocalService(DLFileShortcutLocalService dlFileShortcutLocalService) {
        this.dlFileShortcutLocalService = dlFileShortcutLocalService;
    }

    @Override
    public CompletionStage<List<DLFileShortcut>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DLFileShortcut.class);
            query.add(PropertyFactoryUtil.forName("fileShortcutId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return dlFileShortcutLocalService.dynamicQuery(query);
        });
    }
}
