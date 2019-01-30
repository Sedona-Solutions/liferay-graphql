package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.LayoutBatchLoader;
import fr.sedona.liferay.graphql.resolvers.LayoutResolvers;
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
        service = LayoutResolvers.class
)
@SuppressWarnings("squid:S1192")
public class LayoutResolversImpl implements LayoutResolvers {
    private LayoutLocalService layoutLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setLayoutLocalService(LayoutLocalService layoutLocalService) {
        this.layoutLocalService = layoutLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<Layout>> getLayoutsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return layoutLocalService.getLayouts(start, end);
        };
    }

    @Override
    public DataFetcher<List<Layout>> getLayoutsForGroupDataFetcher() {
        return environment -> {
            long groupId = util.getLongArg(environment, "groupId");
            boolean privateLayout = util.getBooleanArg(environment, "privateLayout");

            return layoutLocalService.getLayouts(groupId, privateLayout);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<Layout>> getLayoutDataFetcher() {
        return environment -> {
            long layoutId = util.getLongArg(environment, "layoutId");
            if (layoutId <= 0) {
                return null;
            }

            DataLoader<Long, Layout> dataLoader = environment.getDataLoader(LayoutBatchLoader.KEY);
            return dataLoader.load(layoutId);
        };
    }

    @Override
    public DataFetcher<Layout> createLayoutDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId");
            long groupId = util.getLongArg(environment, "groupId");
            boolean privateLayout = util.getBooleanArg(environment, "privateLayout");
            long parentLayoutId = util.getLongArg(environment, "parentLayoutId");
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> titleMap = util.getTranslatedArg(environment, "titleMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            Map<Locale, String> keywordsMap = util.getTranslatedArg(environment, "keywordsMap");
            Map<Locale, String> robotsMap = util.getTranslatedArg(environment, "robotsMap");
            String type = util.getStringArg(environment, "type");
            String typeSettings = util.getStringArg(environment, "typeSettings");
            boolean hidden = util.getBooleanArg(environment, "hidden");
            Map<Locale, String> friendlyURLMap = util.getTranslatedArg(environment, "friendlyURLMap");
            ServiceContext serviceContext = new ServiceContext();

            return layoutLocalService.addLayout(
                    userId,
                    groupId,
                    privateLayout,
                    parentLayoutId,
                    nameMap,
                    titleMap,
                    descriptionMap,
                    keywordsMap,
                    robotsMap,
                    type,
                    typeSettings,
                    hidden,
                    friendlyURLMap,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<Layout> updateLayoutDataFetcher() {
        return environment -> {
            long groupId = util.getLongArg(environment, "groupId");
            boolean privateLayout = util.getBooleanArg(environment, "privateLayout");
            long layoutId = util.getLongArg(environment, "layoutId");
            long parentLayoutId = util.getLongArg(environment, "parentLayoutId");
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> titleMap = util.getTranslatedArg(environment, "titleMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            Map<Locale, String> keywordsMap = util.getTranslatedArg(environment, "keywordsMap");
            Map<Locale, String> robotsMap = util.getTranslatedArg(environment, "robotsMap");
            String type = util.getStringArg(environment, "type");
            String typeSettings = util.getStringArg(environment, "typeSettings");
            boolean hidden = util.getBooleanArg(environment, "hidden");
            Map<Locale, String> friendlyURLMap = util.getTranslatedArg(environment, "friendlyURLMap");
            ServiceContext serviceContext = new ServiceContext();

            // Remark: there is no update method to update everything at once
            layoutLocalService.updateLayout(
                    groupId,
                    privateLayout,
                    layoutId,
                    parentLayoutId,
                    nameMap,
                    titleMap,
                    descriptionMap,
                    keywordsMap,
                    robotsMap,
                    type,
                    hidden,
                    friendlyURLMap,
                    false,
                    null,
                    serviceContext);
            return layoutLocalService.updateLayout(
                    groupId,
                    privateLayout,
                    layoutId,
                    typeSettings);
        };
    }

    @Override
    public DataFetcher<Layout> deleteLayoutDataFetcher() {
        return environment -> {
            long layoutId = util.getLongArg(environment, "layoutId");

            return layoutLocalService.deleteLayout(layoutId);
        };
    }
}
