package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalArticleConstants;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.JournalArticleBatchLoader;
import fr.sedona.liferay.graphql.resolvers.JournalArticleResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = JournalArticleResolvers.class
)
@SuppressWarnings("squid:S1192")
public class JournalArticleResolversImpl implements JournalArticleResolvers {
    private JournalArticleLocalService journalArticleLocalService;

    @Reference(unbind = "-")
    public void setJournalArticleLocalService(JournalArticleLocalService journalArticleLocalService) {
        this.journalArticleLocalService = journalArticleLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<JournalArticle>> getJournalArticlesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return journalArticleLocalService.getJournalArticles(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<JournalArticle>> getJournalArticleDataFetcher() {
        return environment -> {
            long id = util.getLongArg(environment, "id");
            if (id <= 0) {
                return null;
            }

            DataLoader<Long, JournalArticle> dataLoader = environment.getDataLoader(JournalArticleBatchLoader.KEY);
            return dataLoader.load(id);
        };
    }

    @Override
    public DataFetcher<JournalArticle> createJournalArticleDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            long folderId = util.getLongArg(environment, "folderId");
            long classNameId = util.getLongArg(environment, "classNameId", JournalArticleConstants.CLASSNAME_ID_DEFAULT);
            long classPK = util.getLongArg(environment, "classPK");
            String articleId = util.getStringArg(environment, "articleId");
            boolean autoArticleId = util.getBooleanArg(environment, "autoArticleId");
            Map<Locale, String> titleMap = util.getTranslatedArg(environment, "titleMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            Map<Locale, String> friendlyURLMap = util.getTranslatedArg(environment, "friendlyURLMap");
            String content = util.getStringArg(environment, "content");
            String ddmStructureKey = util.getStringArg(environment, "ddmStructureKey");
            String ddmTemplateKey = util.getStringArg(environment, "ddmTemplateKey");
            String layoutUuid = util.getStringArg(environment, "layoutUuid");
            int displayDateMonth = util.getIntArg(environment, "displayDateMonth");
            int displayDateDay = util.getIntArg(environment, "displayDateDay");
            int displayDateYear = util.getIntArg(environment, "displayDateYear");
            int displayDateHour = util.getIntArg(environment, "displayDateHour");
            int displayDateMinute = util.getIntArg(environment, "displayDateMinute");
            int expirationDateMonth = util.getIntArg(environment, "expirationDateMonth");
            int expirationDateDay = util.getIntArg(environment, "expirationDateDay");
            int expirationDateYear = util.getIntArg(environment, "expirationDateYear");
            int expirationDateHour = util.getIntArg(environment, "expirationDateHour");
            int expirationDateMinute = util.getIntArg(environment, "expirationDateMinute");
            boolean neverExpire = util.getBooleanArg(environment, "neverExpire", true);
            int reviewDateMonth = util.getIntArg(environment, "reviewDateMonth");
            int reviewDateDay = util.getIntArg(environment, "reviewDateDay");
            int reviewDateYear = util.getIntArg(environment, "reviewDateYear");
            int reviewDateHour = util.getIntArg(environment, "reviewDateHour");
            int reviewDateMinute = util.getIntArg(environment, "reviewDateMinute");
            boolean neverReview = util.getBooleanArg(environment, "neverReview", true);
            boolean indexable = util.getBooleanArg(environment, "indexable", true);
            String articleURL = util.getStringArg(environment, "articleURL");
            ServiceContext serviceContext = new ServiceContext();

            return journalArticleLocalService.addArticle(
                    userId,
                    groupId,
                    folderId,
                    classNameId,
                    classPK,
                    articleId,
                    autoArticleId,
                    1,
                    titleMap,
                    descriptionMap,
                    friendlyURLMap,
                    content,
                    ddmStructureKey,
                    ddmTemplateKey,
                    layoutUuid,
                    displayDateMonth,
                    displayDateDay,
                    displayDateYear,
                    displayDateHour,
                    displayDateMinute,
                    expirationDateMonth,
                    expirationDateDay,
                    expirationDateYear,
                    expirationDateHour,
                    expirationDateMinute,
                    neverExpire,
                    reviewDateMonth,
                    reviewDateDay,
                    reviewDateYear,
                    reviewDateHour,
                    reviewDateMinute,
                    neverReview,
                    indexable,
                    false,
                    null,
                    null,
                    null,
                    articleURL,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<JournalArticle> updateJournalArticleDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            long folderId = util.getLongArg(environment, "folderId");
            String articleId = util.getStringArg(environment, "articleId");
            double version = util.getDoubleArg(environment, "version", 1);
            Map<Locale, String> titleMap = util.getTranslatedArg(environment, "titleMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            Map<Locale, String> friendlyURLMap = util.getTranslatedArg(environment, "friendlyURLMap");
            String content = util.getStringArg(environment, "content");
            String ddmStructureKey = util.getStringArg(environment, "ddmStructureKey");
            String ddmTemplateKey = util.getStringArg(environment, "ddmTemplateKey");
            String layoutUuid = util.getStringArg(environment, "layoutUuid");
            int displayDateMonth = util.getIntArg(environment, "displayDateMonth");
            int displayDateDay = util.getIntArg(environment, "displayDateDay");
            int displayDateYear = util.getIntArg(environment, "displayDateYear");
            int displayDateHour = util.getIntArg(environment, "displayDateHour");
            int displayDateMinute = util.getIntArg(environment, "displayDateMinute");
            int expirationDateMonth = util.getIntArg(environment, "expirationDateMonth");
            int expirationDateDay = util.getIntArg(environment, "expirationDateDay");
            int expirationDateYear = util.getIntArg(environment, "expirationDateYear");
            int expirationDateHour = util.getIntArg(environment, "expirationDateHour");
            int expirationDateMinute = util.getIntArg(environment, "expirationDateMinute");
            boolean neverExpire = util.getBooleanArg(environment, "neverExpire", true);
            int reviewDateMonth = util.getIntArg(environment, "reviewDateMonth");
            int reviewDateDay = util.getIntArg(environment, "reviewDateDay");
            int reviewDateYear = util.getIntArg(environment, "reviewDateYear");
            int reviewDateHour = util.getIntArg(environment, "reviewDateHour");
            int reviewDateMinute = util.getIntArg(environment, "reviewDateMinute");
            boolean neverReview = util.getBooleanArg(environment, "neverReview", true);
            boolean indexable = util.getBooleanArg(environment, "indexable", true);
            String articleURL = util.getStringArg(environment, "articleURL");
            ServiceContext serviceContext = new ServiceContext();

            return journalArticleLocalService.updateArticle(
                    userId,
                    groupId,
                    folderId,
                    articleId,
                    version,
                    titleMap,
                    descriptionMap,
                    friendlyURLMap,
                    content,
                    ddmStructureKey,
                    ddmTemplateKey,
                    layoutUuid,
                    displayDateMonth,
                    displayDateDay,
                    displayDateYear,
                    displayDateHour,
                    displayDateMinute,
                    expirationDateMonth,
                    expirationDateDay,
                    expirationDateYear,
                    expirationDateHour,
                    expirationDateMinute,
                    neverExpire,
                    reviewDateMonth,
                    reviewDateDay,
                    reviewDateYear,
                    reviewDateHour,
                    reviewDateMinute,
                    neverReview,
                    indexable,
                    false,
                    null,
                    null,
                    null,
                    articleURL,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<JournalArticle> deleteJournalArticleDataFetcher() {
        return environment -> {
            long id = util.getLongArg(environment, "id");

            return journalArticleLocalService.deleteJournalArticle(id);
        };
    }
}
