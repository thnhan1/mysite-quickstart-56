package com.mysite.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Component(
        service = WorkflowProcess.class,
        property = {
                "process.label=My Site - Auto Tag Pages"
        }
)
public class AutoTagProcessStep implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(AutoTagProcessStep.class);
    private static final String JCR_TITLE = com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
    private static final String JCR_CONTENT = "/jcr:content";

    @Override
    public void execute(WorkItem workItem,
                        WorkflowSession workflowSession,
                        MetaDataMap metaDataMap) throws WorkflowException {

        String payloadType = workItem.getWorkflowData().getPayloadType();
        if (!"JCR_PATH".equals(payloadType)) {
            log.warn("Unsupported payload type: {}", payloadType);
            return;
        }

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        log.info("Xu ly payload: {}", payloadPath);

        Session session = workflowSession.adaptTo(Session.class);
        if (session == null) {
            throw new WorkflowException("Cannot obtain JCR Session");
        }

        try {
            String jcrContentPath = payloadPath + JCR_CONTENT;
            if (!session.nodeExists(jcrContentPath)) {
                log.warn("No jcr:content found at: {}", payloadPath);
                return;
            }

            Node contentNode = session.getNode(jcrContentPath);

            // check node versioning
            log.info("Log is versioning {}, is node checked out: {}", contentNode.isNodeType(com.day.cq.commons.jcr.JcrConstants.MIX_VERSIONABLE),  !contentNode.isCheckedOut());

            String title = contentNode.hasProperty(JCR_TITLE)
                    ? contentNode.getProperty(JCR_TITLE).getString()
                    : "";
            log.info("Page title: '{}' at {}", title, payloadPath);

            if (!title.toLowerCase().contains("news")) {
                log.info("Title does not contain 'news', skipping: {}", payloadPath);
                return;
            }

            // Thêm property demo = "ok"
            contentNode.setProperty("demo", "ok");
            session.save();
            log.info("Added property 'demo=ok' and saved session for {}", payloadPath);

        } catch (RepositoryException e) {
            throw new WorkflowException("Failed to process payload: " + payloadPath, e);
        }
    }

    /**
     * Using modified value map
     */


}

/*
public class AutoTagProcessStep implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(AutoTagProcessStep.class);

    @Override
    public void execute(WorkItem workItem,
                        WorkflowSession workflowSession,
                        MetaDataMap metaDataMap) throws WorkflowException {

        // 1. Validate payload type
        String payloadType = workItem.getWorkflowData().getPayloadType();
        if (!"JCR_PATH".equals(payloadType)) {
            log.warn("Unsupported payload type: {}", payloadType);
            return;
        }

        // 2. Get payload path
        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        log.info("Xu ly payload: {}", payloadPath);

        // 3. Read process step args
        String tagNamespace = metaDataMap.get("PROCESS_ARGS", "mysite:");

        // 4. Access JCR via Workflow Session
        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        if (resolver == null) {
            throw new WorkflowException("Cannot obtain ResourceResolver");
        }

        Resource contentResource = resolver.getResource(payloadPath + "/jcr:content");
        if (contentResource == null) {
            log.warn("No jcr:content found at: {}", payloadPath);
            return;
        }

        // 5. Apply business logic
        ModifiableValueMap properties = contentResource.adaptTo(ModifiableValueMap.class);
        if (properties == null) {
            log.warn("Cannot adapt to ModifiableValueMap at: {}", payloadPath);
            return;
        }

        boolean changed = false;

        String title = properties.get("jcr:title", "");
        log.info("Page title: '{}' at {}", title, payloadPath); // thêm dòng này

        if (title.toLowerCase().contains("news")) {
            String[] existingTags = properties.get("cq:tags", new String[0]);
            log.info("Existing tags: {}", Arrays.toString(existingTags)); // thêm dòng này
            String newTag = tagNamespace + "content-type/news";

            if (!Arrays.asList(existingTags).contains(newTag)) {
                String[] updatedTags = Arrays.copyOf(existingTags, existingTags.length + 1);
                updatedTags[existingTags.length] = newTag;
                properties.put("cq:tags", updatedTags);
                log.info("Tagged {} with {}", payloadPath, newTag);
                changed = true;
            } else {
                log.info("Tag {} already exists, skipping", newTag); // thêm dòng này
            }
        }

        if (changed) {
            try {
                resolver.commit();
                log.info("Committed changes for {}", payloadPath); // thêm dòng này
            } catch (PersistenceException e) {
                throw new WorkflowException("Failed to commit changes for: " + payloadPath, e);
            }
        } else {
            log.info("No changes to commit for {}", payloadPath); // thêm dòng này
        }
    }
}*/
