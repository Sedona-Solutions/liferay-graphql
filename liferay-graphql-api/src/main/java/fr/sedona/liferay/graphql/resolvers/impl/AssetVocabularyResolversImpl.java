package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.AssetVocabularyBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AssetVocabularyResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = AssetVocabularyResolvers.class
)
@SuppressWarnings("squid:S1192")
public class AssetVocabularyResolversImpl implements AssetVocabularyResolvers {
    private AssetVocabularyLocalService assetVocabularyLocalService;

    @Reference(unbind = "-")
    public void setAssetVocabularyLocalService(AssetVocabularyLocalService assetVocabularyLocalService) {
        this.assetVocabularyLocalService = assetVocabularyLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<AssetVocabulary>> getAssetVocabulariesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return assetVocabularyLocalService.getAssetVocabularies(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<AssetVocabulary>> getAssetVocabularyDataFetcher() {
        return environment -> {
            long vocabularyId = getVocabularyId(environment);
            if (vocabularyId <= 0) {
                return null;
            }

            DataLoader<Long, AssetVocabulary> dataLoader = environment.getDataLoader(AssetVocabularyBatchLoader.KEY);
            return dataLoader.load(vocabularyId);
        };
    }

    private long getVocabularyId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "vocabularyId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getVocabularyId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<AssetVocabulary> createAssetVocabularyDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            String title = util.getStringArg(environment, "title");
            Map<Locale, String> titleMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            String settings = util.getStringArg(environment, "settings");
            ServiceContext serviceContext = new ServiceContext();

            return assetVocabularyLocalService.addVocabulary(
                    userId,
                    groupId,
                    title,
                    titleMap,
                    descriptionMap,
                    settings,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<AssetVocabulary> updateAssetVocabularyDataFetcher() {
        return environment -> {
            long vocabularyId = util.getLongArg(environment, "vocabularyId");
            String title = util.getStringArg(environment, "title");
            Map<Locale, String> titleMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            String settings = util.getStringArg(environment, "settings");
            ServiceContext serviceContext = new ServiceContext();

            return assetVocabularyLocalService.updateVocabulary(
                    vocabularyId,
                    title,
                    titleMap,
                    descriptionMap,
                    settings,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<AssetVocabulary> deleteAssetVocabularyDataFetcher() {
        return environment -> {
            long vocabularyId = util.getLongArg(environment, "vocabularyId");

            return assetVocabularyLocalService.deleteAssetVocabulary(vocabularyId);
        };
    }
}
