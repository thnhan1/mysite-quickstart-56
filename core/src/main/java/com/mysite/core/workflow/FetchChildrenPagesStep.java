package com.mysite.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Workflow Step for multi page review after publish
 */
@Component(
        service = WorkflowProcess.class,
        property = {
                "process.label=Fetch Children Pages"
        }
)
public class FetchChildrenPagesStep implements WorkflowProcess{
    private static final Logger LOG = LoggerFactory.getLogger(FetchChildrenPagesStep.class);
    private static final String CHILD_PAGE_COUNT = "CHILD_PAGE_COUNT";


    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        String payloadPath = workItem.getWorkflowData().getPayload().toString();

        // using ResourceResolver from WorkflowSession instead using system service
        // WF Session is read-only
        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        if (resolver == null) {
            LOG.error("cannot adapt WorkflowSession to ResourceResolver");
            return;
        }

        PageManager pageManager = resolver.adaptTo(PageManager.class);
        if (pageManager == null) return;

        Page currentPage = pageManager.getPage(payloadPath);
        if (currentPage != null) {
            int childPageCount = getChildrenPagesCount(currentPage, 0);
            LOG.debug("Payload: {} has {} chilren pages.", payloadPath, childPageCount);

            // save data into MetaDataMap of Workflow
            workItem.getWorkflow().getMetaDataMap().put(CHILD_PAGE_COUNT, childPageCount);
        }
    }

    private int getChildrenPagesCount(Page page, int count) {
        Iterator<Page> pageIterator = page.listChildren();
        while (pageIterator.hasNext()) {
            Page childPage = pageIterator.next();
            if (childPage != null) {
                count += getChildrenPagesCount(childPage, count);
            }
            count++;
        }
        return count;
    }
}
