package fr.sedona.liferay.graphql.loaders;

import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.service.OAuth2AuthorizationLocalService;
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
        service = OAuth2AuthorizationBatchLoader.class
)
public class OAuth2AuthorizationBatchLoader implements BatchLoader<Long, OAuth2Authorization> {
    public static final String KEY = "oAuth2Authorization";
    private OAuth2AuthorizationLocalService oAuth2AuthorizationLocalService;

    @Reference(unbind = "-")
    public void setOAuth2AuthorizationLocalService(OAuth2AuthorizationLocalService oAuth2AuthorizationLocalService) {
        this.oAuth2AuthorizationLocalService = oAuth2AuthorizationLocalService;
    }

    @Override
    public CompletionStage<List<OAuth2Authorization>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(OAuth2Authorization.class);
            query.add(PropertyFactoryUtil.forName("oAuth2AuthorizationId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return oAuth2AuthorizationLocalService.dynamicQuery(query);
        });
    }
}
