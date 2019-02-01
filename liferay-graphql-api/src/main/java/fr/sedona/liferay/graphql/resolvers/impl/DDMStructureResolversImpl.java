package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMStructureConstants;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.DDMStructureBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DDMStructureResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionPath;
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
        service = DDMStructureResolvers.class
)
@SuppressWarnings("squid:S1192")
public class DDMStructureResolversImpl implements DDMStructureResolvers {
    private DDMStructureLocalService ddmStructureLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setDDMStructureLocalService(DDMStructureLocalService ddmstructureLocalService) {
        this.ddmStructureLocalService = ddmstructureLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<DDMStructure>> getDDMStructuresDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return ddmStructureLocalService.getDDMStructures(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<DDMStructure>> getDDMStructureDataFetcher() {
        return environment -> {
            long structureId = getStructureId(environment);
            if (structureId <= 0) {
                return null;
            }

            DataLoader<Long, DDMStructure> dataLoader = environment.getDataLoader(DDMStructureBatchLoader.KEY);
            return dataLoader.load(structureId);
        };
    }

    private long getStructureId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "structureId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof DDMStructure) {
            ExecutionPath segment = environment.getExecutionStepInfo().getPath();
            if (segment.getSegmentName().contains("parentStructure")) {
                return ((DDMStructure) source).getParentStructureId();
            }
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getDDMStructureId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<DDMStructure> getDDMStructureByKeyDataFetcher() {
        return environment -> {
            long groupId = util.getLongArg(environment, "groupId");
            long classNameId = util.getLongArg(environment, "classNameId");
            String structureKey = util.getStringArg(environment, "structureKey");

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DDMStructure.class);
            query.add(PropertyFactoryUtil.forName("groupId")
                    .eq(groupId));
            query.add(PropertyFactoryUtil.forName("classNameId")
                    .eq(classNameId));
            query.add(PropertyFactoryUtil.forName("structureKey")
                    .eq(structureKey));
            return ddmStructureLocalService.dynamicQuery(query)
                    .stream()
                    .map(o -> (DDMStructure) o)
                    .findFirst()
                    .orElse(null);
        };
    }

    @Override
    public DataFetcher<DDMStructure> createDDMStructureDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            String parentStructureKey = util.getStringArg(environment, "parentStructureKey");
            long classNameId = util.getLongArg(environment, "classNameId");
            String structureKey = util.getStringArg(environment, "structureKey");
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            DDMForm ddmForm = util.getDDDMFormArg(environment, "ddmForm");
            DDMFormLayout ddmFormLayout = util.getDDDMFormLayoutArg(environment, "ddmFormLayout");
            String storageType = util.getStringArg(environment, "storageType", "json");
            int type = util.getIntArg(environment, "type", DDMStructureConstants.TYPE_DEFAULT);
            ServiceContext serviceContext = new ServiceContext();

            return ddmStructureLocalService.addStructure(
                    userId,
                    groupId,
                    parentStructureKey,
                    classNameId,
                    structureKey,
                    nameMap,
                    descriptionMap,
                    ddmForm,
                    ddmFormLayout,
                    storageType,
                    type,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DDMStructure> createDDMStructureForJournalArticleDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            String parentStructureKey = util.getStringArg(environment, "parentStructureKey");
            long classNameId = util.getClassNameId(JournalArticle.class.getName());
            String structureKey = util.getStringArg(environment, "structureKey");
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            DDMForm ddmForm = util.getDDDMFormForJournalArticleArg(environment, "ddmForm");
            DDMFormLayout ddmFormLayout = util.getDefaultDDDMFormLayout(ddmForm);
            String storageType = util.getStringArg(environment, "storageType", "json");
            ServiceContext serviceContext = new ServiceContext();

            return ddmStructureLocalService.addStructure(
                    userId,
                    groupId,
                    parentStructureKey,
                    classNameId,
                    structureKey,
                    nameMap,
                    descriptionMap,
                    ddmForm,
                    ddmFormLayout,
                    storageType,
                    DDMStructureConstants.TYPE_DEFAULT,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DDMStructure> updateDDMStructureDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long structureId = util.getLongArg(environment, "structureId");
            long parentStructureId = util.getLongArg(environment, "parentStructureId");
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            DDMForm ddmForm = util.getDDDMFormArg(environment, "ddmForm");
            DDMFormLayout ddmFormLayout = util.getDDDMFormLayoutArg(environment, "ddmFormLayout");
            ServiceContext serviceContext = new ServiceContext();

            return ddmStructureLocalService.updateStructure(
                    userId,
                    structureId,
                    parentStructureId,
                    nameMap,
                    descriptionMap,
                    ddmForm,
                    ddmFormLayout,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DDMStructure> updateDDMStructureForJournalArticleDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long structureId = util.getLongArg(environment, "structureId");
            long parentStructureId = util.getLongArg(environment, "parentStructureId");
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            DDMForm ddmForm = util.getDDDMFormForJournalArticleArg(environment, "ddmForm");
            DDMFormLayout ddmFormLayout = util.getDefaultDDDMFormLayout(ddmForm);
            ServiceContext serviceContext = new ServiceContext();

            return ddmStructureLocalService.updateStructure(
                    userId,
                    structureId,
                    parentStructureId,
                    nameMap,
                    descriptionMap,
                    ddmForm,
                    ddmFormLayout,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DDMStructure> deleteDDMStructureDataFetcher() {
        return environment -> {
            long structureId = util.getLongArg(environment, "structureId");

            return ddmStructureLocalService.deleteDDMStructure(structureId);
        };
    }
}
