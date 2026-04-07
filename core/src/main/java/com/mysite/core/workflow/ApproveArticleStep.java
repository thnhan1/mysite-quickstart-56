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
                "process.label=MySite - Approve Article"
        }
)
public class ApproveArticleStep implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(ApproveArticleStep.class);

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
        LOG.info("Approving article at: {}", payloadPath);

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

            mvm.put("articleStatus", "approved");
            mvm.put("approvedDate", Calendar.getInstance());

            String initiator = workItem.getWorkflow().getInitiator();
            String currentUser = session.getUserID();
            mvm.put("approvedBy", currentUser);

            resolver.commit();
            LOG.info("Article approved at {} by {}, initiated by {}",
                     payloadPath, currentUser, initiator);

        } catch (LoginException e) {
            throw new WorkflowException("Cannot obtain ResourceResolver", e);
        } catch (PersistenceException e) {
            throw new WorkflowException("Cannot save approval data", e);
        }
    }
}
