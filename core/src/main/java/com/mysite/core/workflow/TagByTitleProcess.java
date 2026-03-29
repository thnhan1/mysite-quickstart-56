package com.mysite.core.workflow;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.mysite.core.constants.AppConstants;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * workflow add tag to page. if title has news tag is new else tag is demo
 */
@Component(service = WorkflowProcess.class,
property = {
        "process.label=Tag page by jcr:title (news, demo)"
})
public class TagByTitleProcess implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(TagByTitleProcess.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private static final String SUBSERVICE= AppConstants.SUBSERVICE;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {

        String payloadPath = Objects.toString(workItem.getWorkflowData().getPayload(), null);
        if (payloadPath == null || !payloadPath.startsWith("/content/")) {
            log.warn("Skip: payloadPath={}", payloadPath);
            return;
        }

        Map<String, Object>  authInfo = Map.of(
                ResourceResolverFactory.SUBSERVICE, SUBSERVICE
        );

        try (ResourceResolver rr = resourceResolverFactory.getServiceResourceResolver(authInfo)) {
            Resource contentResource = rr.getResource(payloadPath + "/jcr:content");
            if (contentResource == null) {
                log.warn("Not found jcr:content for payload={}", payloadPath);
                return;
            }

            ModifiableValueMap mvm = contentResource.adaptTo(ModifiableValueMap.class);
            if (mvm == null) {
                log.warn("Can not adapt to Modifiedable Value Map: {}", contentResource.getPath());
                return;
            }

            String title = mvm.get("jcr:title", String.class);
            String tagToSet = "demo";
            if (title.contains("news")) {
               tagToSet = "news";
            }

            // override cq:tags (multi-value)
            mvm.put("cq:tags", new String[]{tagToSet});

            rr.commit();
            log.info("Update cq:tags={} for {}", tagToSet, contentResource.getParent());
        } catch (LoginException e) {
            throw new WorkflowException("Can not get service resource resolver", e);
        } catch (PersistenceException e) {
            throw new WorkflowException("Can not get service resource resolver", e);
        }
    }
}
