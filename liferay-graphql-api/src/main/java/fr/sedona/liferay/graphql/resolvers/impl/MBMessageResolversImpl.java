package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.message.boards.kernel.model.MBMessage;
import com.liferay.message.boards.kernel.model.MBThread;
import com.liferay.message.boards.kernel.service.MBMessageLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.MBMessageBatchLoader;
import fr.sedona.liferay.graphql.resolvers.MBMessageResolvers;
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
        service = MBMessageResolvers.class
)
@SuppressWarnings("squid:S1192")
public class MBMessageResolversImpl implements MBMessageResolvers {
    private MBMessageLocalService mbMessageLocalService;

    @Reference(unbind = "-")
    public void setMBMessageLocalService(MBMessageLocalService mbMessageLocalService) {
        this.mbMessageLocalService = mbMessageLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<MBMessage>> getMBMessagesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return mbMessageLocalService.getMBMessages(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<MBMessage>> getMBMessageDataFetcher() {
        return environment -> {
            long messageId = getMessageId(environment);
            if (messageId <= 0) {
                return null;
            }

            DataLoader<Long, MBMessage> dataLoader = environment.getDataLoader(MBMessageBatchLoader.KEY);
            return dataLoader.load(messageId);
        };
    }

    private long getMessageId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "messageId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof MBMessage) {
            ExecutionPath segment = environment.getExecutionStepInfo().getPath();
            if (segment.getSegmentName().contains("parentMessage")) {
                return ((MBMessage) source).getParentMessageId();
            } else if (segment.getSegmentName().contains("rootMessage")) {
                return ((MBMessage) source).getRootMessageId();
            }
        } else if (source instanceof MBThread) {
            return ((MBThread) source).getRootMessageId();
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getMessageId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<MBMessage> createMBMessageDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            String userName = util.getStringArg(environment, "userName");
            long groupId = util.getLongArg(environment, "groupId");
            long categoryId = util.getLongArg(environment, "categoryId");
            long threadId = util.getLongArg(environment, "threadId");
            long parentMessageId = util.getLongArg(environment, "parentMessageId");
            String subject = util.getStringArg(environment, "subject");
            String body = util.getStringArg(environment, "body");
            String format = util.getStringArg(environment, "format");
            boolean anonymous = util.getBooleanArg(environment, "anonymous");
            double priority = util.getDoubleArg(environment, "priority");
            boolean allowPingbacks = util.getBooleanArg(environment, "allowPingbacks");
            ServiceContext serviceContext = new ServiceContext();

            return mbMessageLocalService.addMessage(
                    userId,
                    userName,
                    groupId,
                    categoryId,
                    threadId,
                    parentMessageId,
                    subject,
                    body,
                    format,
                    null,
                    anonymous,
                    priority,
                    allowPingbacks,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<MBMessage> updateMBMessageDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long messageId = util.getLongArg(environment, "messageId");
            String subject = util.getStringArg(environment, "subject");
            String body = util.getStringArg(environment, "body");
            double priority = util.getDoubleArg(environment, "priority");
            boolean allowPingbacks = util.getBooleanArg(environment, "allowPingbacks");
            ServiceContext serviceContext = new ServiceContext();

            return mbMessageLocalService.updateMessage(
                    userId,
                    messageId,
                    subject,
                    body,
                    null,
                    null,
                    priority,
                    allowPingbacks,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<MBMessage> deleteMBMessageDataFetcher() {
        return environment -> {
            long messageId = util.getLongArg(environment, "messageId");

            return mbMessageLocalService.deleteMBMessage(messageId);
        };
    }
}
