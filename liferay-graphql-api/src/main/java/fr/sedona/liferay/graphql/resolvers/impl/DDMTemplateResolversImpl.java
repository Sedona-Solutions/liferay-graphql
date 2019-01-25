package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.template.TemplateConstants;
import fr.sedona.liferay.graphql.loaders.DDMTemplateBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DDMTemplateResolvers;
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
        service = DDMTemplateResolvers.class
)
@SuppressWarnings("squid:S1192")
public class DDMTemplateResolversImpl implements DDMTemplateResolvers {
    private DDMTemplateLocalService ddmTemplateLocalService;

    @Reference(unbind = "-")
    public void setDDMTemplateLocalService(DDMTemplateLocalService ddmTemplateLocalService) {
        this.ddmTemplateLocalService = ddmTemplateLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<DDMTemplate>> getDDMTemplatesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return ddmTemplateLocalService.getDDMTemplates(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<DDMTemplate>> getDDMTemplateDataFetcher() {
        return environment -> {
            long templateId = util.getLongArg(environment, "templateId");
            if (templateId <= 0) {
                return null;
            }

            DataLoader<Long, DDMTemplate> dataLoader = environment.getDataLoader(DDMTemplateBatchLoader.KEY);
            return dataLoader.load(templateId);
        };
    }

    @Override
    public DataFetcher<DDMTemplate> getDDMTemplateByKeyDataFetcher() {
        return environment -> {
            long groupId = util.getLongArg(environment, "groupId");
            long classNameId = util.getLongArg(environment, "classNameId");
            String templateKey = util.getStringArg(environment, "templateKey");

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DDMTemplate.class);
            query.add(PropertyFactoryUtil.forName("groupId")
                    .eq(groupId));
            query.add(PropertyFactoryUtil.forName("classNameId")
                    .eq(classNameId));
            query.add(PropertyFactoryUtil.forName("templateKey")
                    .eq(templateKey));
            return ddmTemplateLocalService.dynamicQuery(query)
                    .stream()
                    .map(o -> (DDMTemplate) o)
                    .findFirst()
                    .orElse(null);
        };
    }

    @Override
    public DataFetcher<DDMTemplate> createDDMTemplateDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            long classNameId = util.getLongArg(environment, "classNameId");
            long classPK = util.getLongArg(environment, "classPK");
            long resourceClassNameId = util.getLongArg(environment, "resourceClassNameId");
            String templateKey = util.getStringArg(environment, "templateKey");
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            String type = util.getStringArg(environment, "type");
            String mode = util.getStringArg(environment, "mode");
            String language = util.getStringArg(environment, "language");
            String script = util.getStringArg(environment, "script");
            boolean cacheable = util.getBooleanArg(environment, "cacheable");
            ServiceContext serviceContext = new ServiceContext();

            return ddmTemplateLocalService.addTemplate(
                    userId,
                    groupId,
                    classNameId,
                    classPK,
                    resourceClassNameId,
                    templateKey,
                    nameMap,
                    descriptionMap,
                    type,
                    mode,
                    language,
                    script,
                    cacheable,
                    false,
                    null,
                    null,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DDMTemplate> updateDDMTemplateDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long templateId = util.getLongArg(environment, "templateId");
            long classPK = util.getLongArg(environment, "classPK");
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            String type = util.getStringArg(environment, "type");
            String mode = util.getStringArg(environment, "mode");
            String language = util.getStringArg(environment, "language", TemplateConstants.LANG_TYPE_FTL);
            String script = util.getStringArg(environment, "script");
            boolean cacheable = util.getBooleanArg(environment, "cacheable");
            ServiceContext serviceContext = new ServiceContext();

            return ddmTemplateLocalService.updateTemplate(
                    userId,
                    templateId,
                    classPK,
                    nameMap,
                    descriptionMap,
                    type,
                    mode,
                    language,
                    script,
                    cacheable,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DDMTemplate> deleteDDMTemplateDataFetcher() {
        return environment -> {
            long templateId = util.getLongArg(environment, "templateId");

            return ddmTemplateLocalService.deleteDDMTemplate(templateId);
        };
    }
}
