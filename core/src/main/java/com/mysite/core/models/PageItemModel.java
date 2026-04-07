package com.mysite.core.models;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.FragmentData;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Model(adaptables = Resource.class,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PageItemModel {

    private static final Logger LOG = LoggerFactory.getLogger(PageItemModel.class);
    private static final String PN_FRAGMENT_PATH = "fragmentPath";

    @ValueMapValue
    private String pagePath;

    @SlingObject
    private Resource currentResource;

    @SlingObject
    private ResourceResolver resourceResolver;

    private String title;
    private String summary;
    private String imagePath;
    private String url;
    private String author;
    private Calendar publishedDate;
    private String formattedDate;
    private boolean fromContentFragment;

    @PostConstruct
    protected void init() {
        String pathToResolve = pagePath;
        if (pathToResolve == null || pathToResolve.isEmpty()) {
            PageManager pm = resourceResolver.adaptTo(PageManager.class);
            if (pm != null) {
                Page containingPage = pm.getContainingPage(currentResource);
                if (containingPage != null) {
                    pathToResolve = containingPage.getPath();
                }
            }
        }

        if (pathToResolve == null || pathToResolve.isEmpty()) {
            return;
        }

        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        if (pageManager == null) {
            return;
        }
        Page page = pageManager.getPage(pathToResolve);
        if (page == null) {
            return;
        }

        url = page.getPath() + ".html";

        ValueMap props = page.getProperties();
        String cfPath = props.get(PN_FRAGMENT_PATH, String.class);
        if (cfPath != null && !cfPath.isEmpty()) {
            populateFromContentFragment(cfPath, props);
        }

        if (!fromContentFragment) {
            populateFromPageProperties(page, props);
        }

        if (publishedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            formattedDate = sdf.format(publishedDate.getTime());
        } else {
            formattedDate = "";
        }
    }

    private void populateFromContentFragment(String cfPath, ValueMap pageProps) {
        Resource cfResource = resourceResolver.getResource(cfPath);
        if (cfResource == null) {
            LOG.debug("CF resource not found at: {}", cfPath);
            return;
        }

        ContentFragment cf = cfResource.adaptTo(ContentFragment.class);
        if (cf == null) {
            LOG.debug("Resource at {} is not a Content Fragment", cfPath);
            return;
        }

        title = getElementValue(cf, "headline", String.class);
        summary = getElementValue(cf, "summary", String.class);
        imagePath = getElementValue(cf, "featuredImage", String.class);
        author = getElementValue(cf, "author", String.class);
        publishedDate = getElementValue(cf, "publishedDate", Calendar.class);

        if (title == null || title.isEmpty()) {
            title = pageProps.get("jcr:title", "");
        }

        fromContentFragment = true;
        LOG.debug("PageItemModel populated from CF: {}", cfPath);
    }

    private void populateFromPageProperties(Page page, ValueMap props) {
        title = props.get("jcr:title", page.getName());
        summary = props.get("jcr:description", "");
        imagePath = props.get("image/fileReference", "");
        author = props.get("author", "");
        publishedDate = props.get("publishedDate",
                        props.get("cq:lastModified", Calendar.class));
    }

    @SuppressWarnings("unchecked")
    private <T> T getElementValue(ContentFragment cf, String elementName, Class<T> type) {
        ContentElement element = cf.getElement(elementName);
        if (element == null) {
            return null;
        }
        FragmentData data = element.getValue();
        if (data == null) {
            return null;
        }
        Object value = data.getValue(type);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getImagePath() { return imagePath; }
    public String getUrl() { return url; }
    public String getAuthor() { return author; }
    public Calendar getPublishedDate() { return publishedDate; }
    public String getFormattedDate() { return formattedDate; }
    public boolean isFromContentFragment() { return fromContentFragment; }
}
