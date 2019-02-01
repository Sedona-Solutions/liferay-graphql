package fr.sedona.liferay.graphql.loaders;

import com.liferay.oauth2.provider.model.OAuth2ScopeGrant;
import com.liferay.oauth2.provider.service.OAuth2ScopeGrantLocalService;
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
        service = OAuth2ScopeGrantBatchLoader.class
)
public class OAuth2ScopeGrantBatchLoader implements BatchLoader<Long, OAuth2ScopeGrant> {
    public static final String KEY = "oAuth2ScopeGrant";
    private OAuth2ScopeGrantLocalService oAuth2ScopeGrantLocalService;

    @Reference(unbind = "-")
    public void setOAuth2ScopeGrantLocalService(OAuth2ScopeGrantLocalService oAuth2ScopeGrantLocalService) {
        this.oAuth2ScopeGrantLocalService = oAuth2ScopeGrantLocalService;
    }

    @Override
    public CompletionStage<List<OAuth2ScopeGrant>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(OAuth2ScopeGrant.class);
            query.add(PropertyFactoryUtil.forName("oAuth2ScopeGrantId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return oAuth2ScopeGrantLocalService.dynamicQuery(query);
        });
    }
}
