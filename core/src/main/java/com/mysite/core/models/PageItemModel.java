package com.mysite.core.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Model(adaptables = Resource.class,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PageItemModel {

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

        ValueMap props = page.getProperties();
        title = props.get("jcr:title", page.getName());
        summary = props.get("jcr:description", "");
        imagePath = props.get("image/fileReference", "");
        url = page.getPath() + ".html";
        author = props.get("author", "");
        publishedDate = props.get("publishedDate",
                        props.get("cq:lastModified", Calendar.class));

        if (publishedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            formattedDate = sdf.format(publishedDate.getTime());
        } else {
            formattedDate = "";
        }
    }

    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getImagePath() { return imagePath; }
    public String getUrl() { return url; }
    public String getAuthor() { return author; }
    public Calendar getPublishedDate() { return publishedDate; }
    public String getFormattedDate() { return formattedDate; }
}
