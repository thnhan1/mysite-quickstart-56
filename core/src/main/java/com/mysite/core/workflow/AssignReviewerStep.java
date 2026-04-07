package com.mysite.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.ParticipantStepChooser;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        service = ParticipantStepChooser.class,
        property = {
                "chooser.label=MySite - Assign Article Reviewer"
        }
)
public class AssignReviewerStep implements ParticipantStepChooser {

    private static final Logger LOG = LoggerFactory.getLogger(AssignReviewerStep.class);
    private static final String CONTENT_PATH = "/content/mysite";
    private static final String GROUP_REVIEWERS = "reviewers";
    private static final String GROUP_ADMINS = "administrators";

    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession,
                                  MetaDataMap metaDataMap) throws WorkflowException {

        String payloadType = workItem.getWorkflowData().getPayloadType();
        if (!"JCR_PATH".equals(payloadType)) {
            LOG.warn("Non-JCR payload, assigning to admins");
            return GROUP_ADMINS;
        }

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        LOG.debug("Assigning reviewer for article at: {}", payloadPath);

        if (payloadPath.startsWith(CONTENT_PATH)) {
            LOG.info("Assigning to reviewers group for: {}", payloadPath);
            return GROUP_REVIEWERS;
        }

        LOG.info("Content outside mysite, assigning to admins: {}", payloadPath);
        return GROUP_ADMINS;
    }
}
