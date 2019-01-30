package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import fr.sedona.liferay.graphql.loaders.ClassNameBatchLoader;
import fr.sedona.liferay.graphql.resolvers.ClassNameResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = ClassNameResolvers.class
)
@SuppressWarnings("squid:S1192")
public class ClassNameResolversImpl implements ClassNameResolvers {
    private ClassNameLocalService classnameLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setClassNameLocalService(ClassNameLocalService classnameLocalService) {
        this.classnameLocalService = classnameLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<ClassName>> getClassNamesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return classnameLocalService.getClassNames(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<ClassName>> getClassNameDataFetcher() {
        return environment -> {
            long classNameId = util.getLongArg(environment, "classNameId");
            if (classNameId <= 0) {
                return null;
            }

            DataLoader<Long, ClassName> dataLoader = environment.getDataLoader(ClassNameBatchLoader.KEY);
            return dataLoader.load(classNameId);
        };
    }

    @Override
    public DataFetcher<ClassName> getClassNameByNameDataFetcher() {
        return environment -> {
            String name = util.getStringArg(environment, "name");

            return classnameLocalService.getClassName(name);
        };
    }
}
