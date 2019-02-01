package fr.sedona.liferay.graphql.loaders;

import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
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
        service = OAuth2ApplicationBatchLoader.class
)
public class OAuth2ApplicationBatchLoader implements BatchLoader<Long, OAuth2Application> {
    public static final String KEY = "oAuth2Application";
    private OAuth2ApplicationLocalService oAuth2ApplicationLocalService;

    @Reference(unbind = "-")
    public void setOAuth2ApplicationLocalService(OAuth2ApplicationLocalService oAuth2ApplicationLocalService) {
        this.oAuth2ApplicationLocalService = oAuth2ApplicationLocalService;
    }

    @Override
    public CompletionStage<List<OAuth2Application>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(OAuth2Application.class);
            query.add(PropertyFactoryUtil.forName("oAuth2ApplicationId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return oAuth2ApplicationLocalService.dynamicQuery(query);
        });
    }
}
