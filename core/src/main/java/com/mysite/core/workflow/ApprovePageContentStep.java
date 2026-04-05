package com.mysite.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.ParticipantStepChooser;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(
        service = ParticipantStepChooser.class,
        property = {
                "chooser.label=Approve Page Content"
        }
)
public class ApprovePageContentStep implements ParticipantStepChooser {
    private static final Logger LOG = LoggerFactory.getLogger(ApprovePageContentStep.class);

    private static final String CONTENT_PATH = "/content/mysite";
    private static final String GROUP_AUTHORS = "content-authors";
    private static final String GROUP_ADMINS = "administrators";

    @Override
    public String getParticipant(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {

        String payloadType = workItem.getWorkflowData().getPayloadType();
        LOG.debug("ApprovePageContentStep: payloadType={}", payloadType);

        if (StringUtils.equals(payloadType, "JCR_PATH")) {
            String path = workItem.getWorkflowData().getPayloadType();
            LOG.debug("ApprovePageContentStep: payloadPath={}", path);

            if (StringUtils.startsWith(path, CONTENT_PATH)) {
                LOG.debug("ApprovePageContentStep: assign to group={}", GROUP_AUTHORS);
                return GROUP_AUTHORS;
            }
        }

        LOG.debug("ApprovePageContentStep: fallback to group={}", GROUP_ADMINS);
        return GROUP_ADMINS;
    }
}
