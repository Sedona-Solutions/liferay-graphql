package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.RoleBatchLoader;
import fr.sedona.liferay.graphql.resolvers.RoleResolvers;
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
        service = RoleResolvers.class
)
@SuppressWarnings("squid:S1192")
public class RoleResolversImpl implements RoleResolvers {
    private RoleLocalService roleLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setRoleLocalService(RoleLocalService roleLocalService) {
        this.roleLocalService = roleLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<Role>> getRolesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return roleLocalService.getRoles(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<Role>> getRoleDataFetcher() {
        return environment -> {
            long roleId = util.getLongArg(environment, "roleId");
            if (roleId <= 0) {
                return null;
            }

            DataLoader<Long, Role> dataLoader = environment.getDataLoader(RoleBatchLoader.KEY);
            return dataLoader.load(roleId);
        };
    }

    @Override
    public DataFetcher<Role> getRoleByNameDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String name = util.getStringArg(environment, "name");

            return roleLocalService.getRole(companyId, name);
        };
    }

    @Override
    public DataFetcher<Role> createRoleDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");
            String name = util.getStringArg(environment, "name");
            Map<Locale, String> titleMap = util.getTranslatedArg(environment, "titleMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            int type = util.getIntArg(environment, "type");
            String subtype = util.getStringArg(environment, "subtype");
            ServiceContext serviceContext = new ServiceContext();

            return roleLocalService.addRole(
                    userId,
                    className,
                    classPK,
                    name,
                    titleMap,
                    descriptionMap,
                    type,
                    subtype,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<Role> updateRoleDataFetcher() {
        return environment -> {
            long roleId = util.getLongArg(environment, "roleId");
            String name = util.getStringArg(environment, "name");
            Map<Locale, String> titleMap = util.getTranslatedArg(environment, "titleMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            String subtype = util.getStringArg(environment, "subtype");
            ServiceContext serviceContext = new ServiceContext();

            return roleLocalService.updateRole(
                    roleId,
                    name,
                    titleMap,
                    descriptionMap,
                    subtype,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<Role> deleteRoleDataFetcher() {
        return environment -> {
            long roleId = util.getLongArg(environment, "roleId");

            return roleLocalService.deleteRole(roleId);
        };
    }
}
