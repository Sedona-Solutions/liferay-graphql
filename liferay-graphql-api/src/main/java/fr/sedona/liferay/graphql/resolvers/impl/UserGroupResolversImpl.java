package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import fr.sedona.liferay.graphql.loaders.UserGroupBatchLoader;
import fr.sedona.liferay.graphql.resolvers.UserGroupResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionPath;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = UserGroupResolvers.class
)
@SuppressWarnings("squid:S1192")
public class UserGroupResolversImpl implements UserGroupResolvers {
    private UserGroupLocalService usergroupLocalService;

    @Reference(unbind = "-")
    public void setUserGroupLocalService(UserGroupLocalService usergroupLocalService) {
        this.usergroupLocalService = usergroupLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<UserGroup>> getUserGroupsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return usergroupLocalService.getUserGroups(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<UserGroup>> getUserGroupDataFetcher() {
        return environment -> {
            long userGroupId = getUserGroupId(environment);
            if (userGroupId <= 0) {
                return null;
            }

            DataLoader<Long, UserGroup> dataLoader = environment.getDataLoader(UserGroupBatchLoader.KEY);
            return dataLoader.load(userGroupId);
        };
    }

    private long getUserGroupId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "userGroupId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof UserGroup) {
            ExecutionPath segment = environment.getExecutionStepInfo().getPath();
            if (segment.getSegmentName().contains("parentUserGroup")) {
                return ((UserGroup) source).getParentUserGroupId();
            }
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getUserGroupId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<UserGroup> getUserGroupByNameDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String name = util.getStringArg(environment, "name");

            return usergroupLocalService.getUserGroup(companyId, name);
        };
    }

    @Override
    public DataFetcher<UserGroup> createUserGroupDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId");
            long companyId = util.getLongArg(environment, "companyId");
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            ServiceContext serviceContext = new ServiceContext();

            return usergroupLocalService.addUserGroup(
                    userId,
                    companyId,
                    name,
                    description,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<UserGroup> updateUserGroupDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            long userGroupId = util.getLongArg(environment, "userGroupId");
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            ServiceContext serviceContext = new ServiceContext();

            return usergroupLocalService.updateUserGroup(
                    companyId,
                    userGroupId,
                    name,
                    description,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<UserGroup> deleteUserGroupDataFetcher() {
        return environment -> {
            long userGroupId = util.getLongArg(environment, "userGroupId");

            return usergroupLocalService.deleteUserGroup(userGroupId);
        };
    }
}
