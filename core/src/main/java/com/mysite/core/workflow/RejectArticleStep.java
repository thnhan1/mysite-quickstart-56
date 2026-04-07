package com.mysite.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

@Component(
        service = WorkflowProcess.class,
        property = {
                "process.label=MySite - Reject Article"
        }
)
public class RejectArticleStep implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(RejectArticleStep.class);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession,
                        MetaDataMap metaDataMap) throws WorkflowException {

        String payloadType = workItem.getWorkflowData().getPayloadType();
        if (!"JCR_PATH".equals(payloadType)) {
            LOG.warn("Unsupported payload type: {}", payloadType);
            return;
        }

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.info("Rejecting article at: {}", payloadPath);

        String rejectReason = metaDataMap.get("PROCESS_ARGS", "No reason provided");
        String workflowComment = workItem.getWorkflow().getMetaDataMap()
                .get("comment", rejectReason);

        Session session = workflowSession.adaptTo(Session.class);
        if (session == null) {
            throw new WorkflowException("Cannot obtain JCR session");
        }

        Map<String, Object> param = Collections.singletonMap("user.jcr.session", session);
        try (ResourceResolver resolver = resolverFactory.getResourceResolver(param)) {
            Resource contentRes = resolver.getResource(payloadPath + "/jcr:content");
            if (contentRes == null) {
                LOG.warn("No jcr:content found at: {}", payloadPath);
                return;
            }

            ModifiableValueMap mvm = contentRes.adaptTo(ModifiableValueMap.class);
            if (mvm == null) {
                LOG.warn("Cannot adapt to ModifiableValueMap: {}", contentRes.getPath());
                return;
            }

            mvm.put("articleStatus", "rejected");
            mvm.put("rejectedDate", Calendar.getInstance());
            mvm.put("rejectedBy", session.getUserID());
            mvm.put("rejectedReason", workflowComment);

            resolver.commit();
            LOG.info("Article rejected at {} by {}, reason: {}",
                     payloadPath, session.getUserID(), workflowComment);

        } catch (LoginException e) {
            throw new WorkflowException("Cannot obtain ResourceResolver", e);
        } catch (PersistenceException e) {
            throw new WorkflowException("Cannot save rejection data", e);
        }
    }
}
