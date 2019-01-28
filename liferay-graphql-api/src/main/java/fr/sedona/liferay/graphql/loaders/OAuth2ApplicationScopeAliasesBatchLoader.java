package fr.sedona.liferay.graphql.loaders;

import com.liferay.oauth2.provider.model.OAuth2ApplicationScopeAliases;
import com.liferay.oauth2.provider.service.OAuth2ApplicationScopeAliasesLocalService;
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
        service = OAuth2ApplicationScopeAliasesBatchLoader.class
)
public class OAuth2ApplicationScopeAliasesBatchLoader implements BatchLoader<Long, OAuth2ApplicationScopeAliases> {
    public static final String KEY = "oAuth2ApplicationScopeAliases";
    private OAuth2ApplicationScopeAliasesLocalService oAuth2ApplicationScopeAliasesLocalService;

    @Reference(unbind = "-")
    public void setOAuth2ApplicationScopeAliasesLocalService(OAuth2ApplicationScopeAliasesLocalService oAuth2ApplicationScopeAliasesLocalService) {
        this.oAuth2ApplicationScopeAliasesLocalService = oAuth2ApplicationScopeAliasesLocalService;
    }

    @Override
    public CompletionStage<List<OAuth2ApplicationScopeAliases>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(OAuth2ApplicationScopeAliases.class);
            query.add(PropertyFactoryUtil.forName("oAuth2ApplicationScopeAliasesId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return oAuth2ApplicationScopeAliasesLocalService.dynamicQuery(query);
        });
    }
}
