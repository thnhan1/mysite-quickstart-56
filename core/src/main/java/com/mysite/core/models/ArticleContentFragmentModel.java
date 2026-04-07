package com.mysite.core.models;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.FragmentData;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import com.day.cq.wcm.api.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = {ArticleContentFragmentModel.class, ComponentExporter.class},
    resourceType = ArticleContentFragmentModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
          extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class ArticleContentFragmentModel implements ComponentExporter {

    private static final Logger LOG = LoggerFactory.getLogger(ArticleContentFragmentModel.class);
    static final String RESOURCE_TYPE = "mysite/components/content/article-detail";
    private static final String PN_FRAGMENT_PATH = "fragmentPath";

    @ValueMapValue
    private String fragmentPath;

    @SlingObject
    private ResourceResolver resourceResolver;

    @ScriptVariable
    private Page currentPage;

    private String headline;
    private String summary;
    private String body;
    private String featuredImage;
    private String author;
    private Calendar publishedDate;
    private String category;
    private String formattedDate;
    private boolean resolved;

    @PostConstruct
    protected void init() {
        String cfPath = resolveFragmentPath();
        if (cfPath == null || cfPath.isEmpty()) {
            LOG.debug("No Content Fragment path found for {}", currentPage != null ? currentPage.getPath() : "unknown");
            return;
        }

        Resource cfResource = resourceResolver.getResource(cfPath);
        if (cfResource == null) {
            LOG.warn("Content Fragment resource not found at: {}", cfPath);
            return;
        }

        ContentFragment cf = cfResource.adaptTo(ContentFragment.class);
        if (cf == null) {
            LOG.warn("Resource at {} is not a Content Fragment", cfPath);
            return;
        }

        headline = getElementValue(cf, "headline", String.class);
        summary = getElementValue(cf, "summary", String.class);
        body = getElementValue(cf, "body", String.class);
        featuredImage = getElementValue(cf, "featuredImage", String.class);
        author = getElementValue(cf, "author", String.class);
        publishedDate = getElementValue(cf, "publishedDate", Calendar.class);
        category = getElementValue(cf, "category", String.class);

        if (publishedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            formattedDate = sdf.format(publishedDate.getTime());
        }

        resolved = true;
        LOG.debug("Resolved Content Fragment '{}' for page {}", cf.getTitle(),
                  currentPage != null ? currentPage.getPath() : "unknown");
    }

    private String resolveFragmentPath() {
        if (fragmentPath != null && !fragmentPath.isEmpty()) {
            return fragmentPath;
        }
        if (currentPage != null) {
            ValueMap pageProps = currentPage.getProperties();
            return pageProps.get(PN_FRAGMENT_PATH, String.class);
        }
        return null;
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

    @JsonProperty("headline")
    public String getHeadline() { return headline; }

    @JsonProperty("summary")
    public String getSummary() { return summary; }

    @JsonProperty("body")
    public String getBody() { return body; }

    @JsonProperty("featuredImage")
    public String getFeaturedImage() { return featuredImage; }

    @JsonProperty("author")
    public String getAuthor() { return author; }

    @JsonIgnore
    public Calendar getPublishedDate() { return publishedDate; }

    @JsonProperty("publishedDate")
    public String getFormattedDate() { return formattedDate; }

    @JsonProperty("category")
    public String getCategory() { return category; }

    @JsonIgnore
    public boolean isResolved() { return resolved; }

    @JsonProperty("url")
    public String getUrl() {
        return currentPage != null ? currentPage.getPath() + ".html" : "";
    }

    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}
