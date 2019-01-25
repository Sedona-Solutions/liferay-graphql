package fr.sedona.liferay.graphql.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.sedona.liferay.graphql.loaders.*;
import org.dataloader.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.concurrent.TimeUnit;

@Component(
        immediate = true,
        service = GraphQLRegistry.class
)
public class GraphQLRegistry extends DataLoaderRegistry {
    private DataLoaderOptions loaderOptions;

    @Reference
    private AssetCategoryBatchLoader assetCategoryBatchLoader;

    @Reference
    private AssetEntryBatchLoader assetEntryBatchLoader;

    @Reference
    private AssetLinkBatchLoader assetLinkBatchLoader;

    @Reference
    private AssetTagBatchLoader assetTagBatchLoader;

    @Reference
    private AssetVocabularyBatchLoader assetVocabularyBatchLoader;

    @Reference
    private BlogsEntryBatchLoader blogsEntryBatchLoader;

    @Reference
    private DLFileEntryBatchLoader dlFileEntryBatchLoader;

    @Reference
    private DLFileEntryMetadataBatchLoader dlFileEntryMetadataBatchLoader;

    @Reference
    private DLFileEntryTypeBatchLoader dlFileEntryTypeBatchLoader;

    @Reference
    private DLFileShortcutBatchLoader dlFileShortcutBatchLoader;

    @Reference
    private DLFileVersionBatchLoader dlFileVersionBatchLoader;

    @Reference
    private DLFolderBatchLoader dlFolderBatchLoader;

    @Reference
    private DDMContentBatchLoader ddmContentBatchLoader;

    @Reference
    private DDMStructureBatchLoader ddmStructureBatchLoader;

    @Reference
    private DDMTemplateBatchLoader ddmTemplateBatchLoader;

    @Reference
    private ExpandoColumnBatchLoader expandoColumnBatchLoader;

    @Reference
    private ExpandoRowBatchLoader expandoRowBatchLoader;

    @Reference
    private ExpandoTableBatchLoader expandoTableBatchLoader;

    @Reference
    private ExpandoValueBatchLoader expandoValueBatchLoader;

    @Reference
    private JournalArticleBatchLoader journalArticleBatchLoader;

    @Reference
    private JournalFeedBatchLoader journalFeedBatchLoader;

    @Reference
    private JournalFolderBatchLoader journalFolderBatchLoader;

    @Reference
    private MBCategoryBatchLoader mbCategoryBatchLoader;

    @Reference
    private MBMessageBatchLoader mbMessageBatchLoader;

    @Reference
    private MBThreadBatchLoader mbThreadBatchLoader;

    @Reference
    private OAuth2ApplicationBatchLoader oAuth2ApplicationBatchLoader;

    @Reference
    private OAuth2ApplicationScopeAliasesBatchLoader oAuth2ApplicationScopeAliasesBatchLoader;

    @Reference
    private OAuth2AuthorizationBatchLoader oAuth2AuthorizationBatchLoader;

    @Reference
    private OAuth2ScopeGrantBatchLoader oAuth2ScopeGrantBatchLoader;

    @Reference
    private AddressBatchLoader addressBatchLoader;

    @Reference
    private ClassNameBatchLoader classnameBatchLoader;

    @Reference
    private CompanyBatchLoader companyBatchLoader;

    @Reference
    private ContactBatchLoader contactBatchLoader;

    @Reference
    private EmailAddressBatchLoader emailaddressBatchLoader;

    @Reference
    private GroupBatchLoader groupBatchLoader;

    @Reference
    private LayoutBatchLoader layoutBatchLoader;

    @Reference
    private ListTypeBatchLoader listTypeBatchLoader;

    @Reference
    private OrganizationBatchLoader organizationBatchLoader;

    @Reference
    private PhoneBatchLoader phoneBatchLoader;

    @Reference
    private RoleBatchLoader roleBatchLoader;

    @Reference
    private UserBatchLoader userBatchLoader;

    @Reference
    private UserGroupBatchLoader userGroupBatchLoader;

    @Reference
    private WebsiteBatchLoader websiteBatchLoader;

    @Reference
    private RatingsEntryBatchLoader ratingsEntryBatchLoader;


    @Activate
    public void activeComponent() {
        prepareLoaderOptions();
        registerLoaders();
    }

    private void prepareLoaderOptions() {
        CacheMap<Long, Object> crossRequestCacheMap = new CacheMap<Long, Object>() {
            Cache<Long, Object> inMemoryCache = Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build();

            @Override
            public boolean containsKey(Long key) {
                return inMemoryCache.getIfPresent(key) != null;
            }

            @Override
            public Object get(Long key) {
                return inMemoryCache.getIfPresent(key);
            }

            @Override
            public CacheMap<Long, Object> set(Long key, Object value) {
                inMemoryCache.put(key, value);
                return this;
            }

            @Override
            public CacheMap<Long, Object> delete(Long key) {
                inMemoryCache.invalidate(key);
                return this;
            }

            @Override
            public CacheMap<Long, Object> clear() {
                inMemoryCache.invalidateAll();
                return this;
            }
        };
        loaderOptions = DataLoaderOptions.newOptions()
                .setCacheMap(crossRequestCacheMap)
                .setMaxBatchSize(10);
    }

    private void registerLoaders() {
        register(AssetCategoryBatchLoader.KEY, assetCategoryBatchLoader);

        register(AssetEntryBatchLoader.KEY, assetEntryBatchLoader);

        register(AssetLinkBatchLoader.KEY, assetLinkBatchLoader);

        register(AssetTagBatchLoader.KEY, assetTagBatchLoader);

        register(AssetVocabularyBatchLoader.KEY, assetVocabularyBatchLoader);

        register(BlogsEntryBatchLoader.KEY, blogsEntryBatchLoader);

        register(DLFileEntryBatchLoader.KEY, dlFileEntryBatchLoader);

        register(DLFileEntryMetadataBatchLoader.KEY, dlFileEntryMetadataBatchLoader);

        register(DLFileEntryTypeBatchLoader.KEY, dlFileEntryTypeBatchLoader);

        register(DLFileShortcutBatchLoader.KEY, dlFileShortcutBatchLoader);

        register(DLFileVersionBatchLoader.KEY, dlFileVersionBatchLoader);

        register(DLFolderBatchLoader.KEY, dlFolderBatchLoader);

        register(DDMContentBatchLoader.KEY, ddmContentBatchLoader);

        register(DDMStructureBatchLoader.KEY, ddmStructureBatchLoader);

        register(DDMTemplateBatchLoader.KEY, ddmTemplateBatchLoader);

        register(ExpandoColumnBatchLoader.KEY, expandoColumnBatchLoader);

        register(ExpandoRowBatchLoader.KEY, expandoRowBatchLoader);

        register(ExpandoTableBatchLoader.KEY, expandoTableBatchLoader);

        register(ExpandoValueBatchLoader.KEY, expandoValueBatchLoader);

        register(JournalArticleBatchLoader.KEY, journalArticleBatchLoader);

        register(JournalFeedBatchLoader.KEY, journalFeedBatchLoader);

        register(JournalFolderBatchLoader.KEY, journalFolderBatchLoader);

        register(MBCategoryBatchLoader.KEY, mbCategoryBatchLoader);

        register(MBMessageBatchLoader.KEY, mbMessageBatchLoader);

        register(MBThreadBatchLoader.KEY, mbThreadBatchLoader);

        register(OAuth2ApplicationBatchLoader.KEY, oAuth2ApplicationBatchLoader);

        register(OAuth2ApplicationScopeAliasesBatchLoader.KEY, oAuth2ApplicationScopeAliasesBatchLoader);

        register(OAuth2AuthorizationBatchLoader.KEY, oAuth2AuthorizationBatchLoader);

        register(OAuth2ScopeGrantBatchLoader.KEY, oAuth2ScopeGrantBatchLoader);

        register(AddressBatchLoader.KEY, addressBatchLoader);

        register(ClassNameBatchLoader.KEY, classnameBatchLoader);

        register(CompanyBatchLoader.KEY, companyBatchLoader);

        register(ContactBatchLoader.KEY, contactBatchLoader);

        register(EmailAddressBatchLoader.KEY, emailaddressBatchLoader);

        register(GroupBatchLoader.KEY, groupBatchLoader);

        register(LayoutBatchLoader.KEY, layoutBatchLoader);

        register(ListTypeBatchLoader.KEY, listTypeBatchLoader);

        register(OrganizationBatchLoader.KEY, organizationBatchLoader);

        register(PhoneBatchLoader.KEY, phoneBatchLoader);

        register(RoleBatchLoader.KEY, roleBatchLoader);

        register(UserBatchLoader.KEY, userBatchLoader);

        register(UserGroupBatchLoader.KEY, userGroupBatchLoader);

        register(WebsiteBatchLoader.KEY, websiteBatchLoader);

        register(RatingsEntryBatchLoader.KEY, ratingsEntryBatchLoader);

    }

    private void register(String key, BatchLoader batchLoader) {
        register(key, DataLoader.newDataLoader(batchLoader, loaderOptions));
    }
}
