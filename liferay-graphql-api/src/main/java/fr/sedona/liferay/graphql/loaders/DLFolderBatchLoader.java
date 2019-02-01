package fr.sedona.liferay.graphql.loaders;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
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
        service = DLFolderBatchLoader.class
)
public class DLFolderBatchLoader implements BatchLoader<Long, DLFolder> {
    public static final String KEY = "dlFolder";
    private DLFolderLocalService dlFolderLocalService;

    @Reference(unbind = "-")
    public void setDLFolderLocalService(DLFolderLocalService dlFolderLocalService) {
        this.dlFolderLocalService = dlFolderLocalService;
    }

    @Override
    public CompletionStage<List<DLFolder>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DLFolder.class);
            query.add(PropertyFactoryUtil.forName("folderId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return dlFolderLocalService.dynamicQuery(query);
        });
    }
}
