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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collections;
import java.util.Map;

@Component(
        service = WorkflowProcess.class,
        property = {
                "process.label=MySite - Validate Article"
        }
)
public class ValidateArticleStep implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateArticleStep.class);
    private static final String ARTICLE_STATUS = "articleStatus";
    private static final String STATUS_PENDING = "pending-review";

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
        LOG.info("Validating article at: {}", payloadPath);

        Session session = workflowSession.adaptTo(Session.class);
        if (session == null) {
            throw new WorkflowException("Cannot obtain JCR session");
        }

        Map<String, Object> param = Collections.singletonMap("user.jcr.session", session);
        try (ResourceResolver resolver = resolverFactory.getResourceResolver(param)) {
            Resource contentRes = resolver.getResource(payloadPath + "/jcr:content");
            if (contentRes == null) {
                throw new WorkflowException("No jcr:content found at: " + payloadPath);
            }

            ValueMap props = contentRes.getValueMap();
            StringBuilder errors = new StringBuilder();

            if (!props.containsKey("jcr:title") || props.get("jcr:title", "").isEmpty()) {
                errors.append("Missing jcr:title. ");
            }
            if (!props.containsKey("jcr:description") || props.get("jcr:description", "").isEmpty()) {
                errors.append("Missing jcr:description. ");
            }

            if (errors.length() > 0) {
                String errorMsg = errors.toString().trim();
                LOG.warn("Article validation failed at {}: {}", payloadPath, errorMsg);
                workItem.getWorkflow().getMetaDataMap().put("validationErrors", errorMsg);
                throw new WorkflowException("Article validation failed: " + errorMsg);
            }

            ModifiableValueMap mvm = contentRes.adaptTo(ModifiableValueMap.class);
            if (mvm != null) {
                mvm.put(ARTICLE_STATUS, STATUS_PENDING);
                resolver.commit();
                LOG.info("Article status set to '{}' for {}", STATUS_PENDING, payloadPath);
            }

        } catch (LoginException e) {
            throw new WorkflowException("Cannot obtain ResourceResolver", e);
        } catch (PersistenceException e) {
            throw new WorkflowException("Cannot save article status", e);
        }
    }
}
