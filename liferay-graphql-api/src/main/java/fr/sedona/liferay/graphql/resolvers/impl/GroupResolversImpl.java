package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.GroupBatchLoader;
import fr.sedona.liferay.graphql.resolvers.GroupResolvers;
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
        service = GroupResolvers.class
)
@SuppressWarnings("squid:S1192")
public class GroupResolversImpl implements GroupResolvers {
    private GroupLocalService groupLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setGroupLocalService(GroupLocalService groupLocalService) {
        this.groupLocalService = groupLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<Group>> getGroupsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return groupLocalService.getGroups(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<Group>> getGroupDataFetcher() {
        return environment -> {
            long groupId = getGroupId(environment);
            if (groupId <= 0) {
                return null;
            }

            DataLoader<Long, Group> dataLoader = environment.getDataLoader(GroupBatchLoader.KEY);
            return dataLoader.load(groupId);
        };
    }

    private long getGroupId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "groupId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof Group) {
            ExecutionPath segment = environment.getExecutionStepInfo().getPath();
            if (segment.getSegmentName().contains("liveGroup")) {
                return ((Group) source).getLiveGroupId();
            } else if (segment.getSegmentName().contains("parentGroup")) {
                return ((Group) source).getParentGroupId();
            }
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getGroupId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<Group> getGroupByKeyDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String groupKey = util.getStringArg(environment, "groupKey");

            return groupLocalService.getGroup(companyId, groupKey);
        };
    }

    @Override
    public DataFetcher<Group> createGroupDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long parentGroupId = util.getLongArg(environment, "parentGroupId", GroupConstants.DEFAULT_PARENT_GROUP_ID);
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");
            long liveGroupId = util.getLongArg(environment, "liveGroupId", GroupConstants.DEFAULT_LIVE_GROUP_ID);
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            int type = util.getIntArg(environment, "type", GroupConstants.TYPE_SITE_OPEN);
            boolean manualMembership = util.getBooleanArg(environment, "manualMembership");
            int membershipRestriction = util.getIntArg(environment, "membershipRestriction");
            String friendlyURL = util.getStringArg(environment, "friendlyURL");
            boolean site = util.getBooleanArg(environment, "site");
            boolean inheritContent = util.getBooleanArg(environment, "inheritContent", true);
            boolean active = util.getBooleanArg(environment, "active", true);
            ServiceContext serviceContext = new ServiceContext();

            return groupLocalService.addGroup(
                    userId,
                    parentGroupId,
                    className,
                    classPK,
                    liveGroupId,
                    nameMap,
                    descriptionMap,
                    type,
                    manualMembership,
                    membershipRestriction,
                    friendlyURL,
                    site,
                    inheritContent,
                    active,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<Group> updateGroupDataFetcher() {
        return environment -> {
            long groupId = util.getLongArg(environment, "groupId");
            long parentGroupId = util.getLongArg(environment, "parentGroupId", GroupConstants.DEFAULT_PARENT_GROUP_ID);
            Map<Locale, String> nameMap = util.getTranslatedArg(environment, "nameMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            int type = util.getIntArg(environment, "type", GroupConstants.TYPE_SITE_OPEN);
            boolean manualMembership = util.getBooleanArg(environment, "manualMembership");
            int membershipRestriction = util.getIntArg(environment, "membershipRestriction");
            String friendlyURL = util.getStringArg(environment, "friendlyURL");
            boolean inheritContent = util.getBooleanArg(environment, "inheritContent", true);
            boolean active = util.getBooleanArg(environment, "active", true);
            ServiceContext serviceContext = new ServiceContext();

            return groupLocalService.updateGroup(
                    groupId,
                    parentGroupId,
                    nameMap,
                    descriptionMap,
                    type,
                    manualMembership,
                    membershipRestriction,
                    friendlyURL,
                    inheritContent,
                    active,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<Group> deleteGroupDataFetcher() {
        return environment -> {
            long groupId = util.getLongArg(environment, "groupId");

            return groupLocalService.deleteGroup(groupId);
        };
    }
}
