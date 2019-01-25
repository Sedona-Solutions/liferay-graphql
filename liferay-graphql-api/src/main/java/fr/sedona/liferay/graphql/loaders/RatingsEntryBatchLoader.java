package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.ratings.kernel.model.RatingsEntry;
import com.liferay.ratings.kernel.service.RatingsEntryLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = RatingsEntryBatchLoader.class
)
public class RatingsEntryBatchLoader implements BatchLoader<Long, RatingsEntry> {
    public static final String KEY = "ratingsEntry";
    private RatingsEntryLocalService ratingsEntryLocalService;

    @Reference(unbind = "-")
    public void setRatingsEntryLocalService(RatingsEntryLocalService ratingsEntryLocalService) {
        this.ratingsEntryLocalService = ratingsEntryLocalService;
    }

    @Override
    public CompletionStage<List<RatingsEntry>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(RatingsEntry.class);
            query.add(PropertyFactoryUtil.forName("entryId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return ratingsEntryLocalService.dynamicQuery(query);
        });
    }
}
