package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.Website;
import com.liferay.portal.kernel.service.WebsiteLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = WebsiteBatchLoader.class
)
public class WebsiteBatchLoader implements BatchLoader<Long, Website> {
    public static final String KEY = "website";
    private WebsiteLocalService websiteLocalService;

    @Reference(unbind = "-")
    public void setWebsiteLocalService(WebsiteLocalService websiteLocalService) {
        this.websiteLocalService = websiteLocalService;
    }

    @Override
    public CompletionStage<List<Website>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Website.class);
            query.add(PropertyFactoryUtil.forName("websiteId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return websiteLocalService.dynamicQuery(query);
        });
    }
}
