package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = ClassNameBatchLoader.class
)
public class ClassNameBatchLoader implements BatchLoader<Long, ClassName> {
    public static final String KEY = "className";
    private ClassNameLocalService classNameLocalService;

    @Reference(unbind = "-")
    public void setClassNameLocalService(ClassNameLocalService classNameLocalService) {
        this.classNameLocalService = classNameLocalService;
    }

    @Override
    public CompletionStage<List<ClassName>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(ClassName.class);
            query.add(PropertyFactoryUtil.forName("classNameId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return classNameLocalService.dynamicQuery(query);
        });
    }
}
