package fr.sedona.liferay.graphql.loaders;

import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
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
        service = DLFileEntryTypeBatchLoader.class
)
public class DLFileEntryTypeBatchLoader implements BatchLoader<Long, DLFileEntryType> {
    public static final String KEY = "dlFileEntryType";
    private DLFileEntryTypeLocalService dlFileEntryTypeLocalService;

    @Reference(unbind = "-")
    public void setDLFileEntryTypeLocalService(DLFileEntryTypeLocalService dlFileEntryTypeLocalService) {
        this.dlFileEntryTypeLocalService = dlFileEntryTypeLocalService;
    }

    @Override
    public CompletionStage<List<DLFileEntryType>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DLFileEntryType.class);
            query.add(PropertyFactoryUtil.forName("fileEntryTypeId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return dlFileEntryTypeLocalService.dynamicQuery(query);
        });
    }
}
