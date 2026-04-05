package com.mysite.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.ParticipantStepChooser;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow Step for multi page review after publish
 */
@Component(
        service = ParticipantStepChooser.class,
        property = {
                "chooser.label=Review Children Pages Step"
        }
)
public class ReviewChildrenPagesStep implements ParticipantStepChooser {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewChildrenPagesStep.class);
    private static final String CHILD_PAGE_COUNT = "CHILD_PAGE_COUNT";
    private static final String ADMINISTRATORS_GROUP = "administrators";
    private static final String CONTENT_AUTHORS_GROUP =  "content-authors";

    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
      // read value from MetaDataMap, provide default value is 0 if not found
      int childPagesCount = workItem.getWorkflow().getMetaDataMap()
              .get(CHILD_PAGE_COUNT,0);
      LOG.debug("Child pages count retrieved from MetaDataMap: {}", childPagesCount);

      return childPagesCount > 0 ? ADMINISTRATORS_GROUP : CONTENT_AUTHORS_GROUP;

    }
}
