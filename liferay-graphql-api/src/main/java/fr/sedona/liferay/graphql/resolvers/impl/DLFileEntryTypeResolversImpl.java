package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.DLFileEntryTypeBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFileEntryTypeResolvers;
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
        service = DLFileEntryTypeResolvers.class
)
@SuppressWarnings("squid:S1192")
public class DLFileEntryTypeResolversImpl implements DLFileEntryTypeResolvers {
    private DLFileEntryTypeLocalService dlFileEntryTypeLocalService;

    @Reference(unbind = "-")
    public void setDLFileEntryTypeLocalService(DLFileEntryTypeLocalService dlFileEntryTypeLocalService) {
        this.dlFileEntryTypeLocalService = dlFileEntryTypeLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<DLFileEntryType>> getDLFileEntryTypesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return dlFileEntryTypeLocalService.getDLFileEntryTypes(start, end);
        };
    }

    @Override
    public DataFetcher<List<DLFileEntryType>> getDLFileEntryTypesForFolderDataFetcher() {
        return environment -> {
            long folderId = util.getLongArg(environment, "folderId");
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return dlFileEntryTypeLocalService.getDLFolderDLFileEntryTypes(folderId, start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<DLFileEntryType>> getDLFileEntryTypeDataFetcher() {
        return environment -> {
            long fileEntryTypeId = getFileEntryTypeId(environment);
            if (fileEntryTypeId <= 0) {
                return null;
            }

            DataLoader<Long, DLFileEntryType> dataLoader = environment.getDataLoader(DLFileEntryTypeBatchLoader.KEY);
            return dataLoader.load(fileEntryTypeId);
        };
    }

    private long getFileEntryTypeId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "fileEntryTypeId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getFileEntryTypeId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<DLFileEntryType> createDLFileEntryTypeDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            String fileEntryTypeKey = util.getStringArg(environment, "fileEntryTypeKey");
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            long[] ddmStructureIds = util.getLongArrayArg(environment, "ddmStructureIds");
            ServiceContext serviceContext = new ServiceContext();

            return dlFileEntryTypeLocalService.addFileEntryType(
                    userId,
                    groupId,
                    fileEntryTypeKey,
                    nameMap,
                    descriptionMap,
                    ddmStructureIds,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DLFileEntryType> updateDLFileEntryTypeDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long fileEntryTypeId = util.getLongArg(environment, "fileEntryTypeId");
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            long[] ddmStructureIds = util.getLongArrayArg(environment, "ddmStructureIds");
            ServiceContext serviceContext = new ServiceContext();

            // REMARK: Thank you for inconsistent API: update does not return the updated entity...
            dlFileEntryTypeLocalService.updateFileEntryType(
                    userId,
                    fileEntryTypeId,
                    nameMap,
                    descriptionMap,
                    ddmStructureIds,
                    serviceContext);
            return dlFileEntryTypeLocalService.getDLFileEntryType(fileEntryTypeId);
        };
    }

    @Override
    public DataFetcher<DLFileEntryType> deleteDLFileEntryTypeDataFetcher() {
        return environment -> {
            long fileEntryTypeId = util.getLongArg(environment, "fileEntryTypeId");

            return dlFileEntryTypeLocalService.deleteDLFileEntryType(fileEntryTypeId);
        };
    }
}
