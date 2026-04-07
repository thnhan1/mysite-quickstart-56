package com.mysite.core.models;

import com.mysite.core.models.dto.ArticleItem;
import com.mysite.core.services.LatestNewsService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Model(adaptables = SlingHttpServletRequest.class,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class LatestNewsModel {

    private static final int DEFAULT_LIMIT = 10;

    @ValueMapValue
    private String rootPath;

    @ValueMapValue
    private Integer limit;

    @OSGiService
    private LatestNewsService latestNewsService;

    @SlingObject
    private ResourceResolver resourceResolver;

    private List<ArticleItem> articles;

    @PostConstruct
    protected void init() {
        int effectiveLimit = (limit != null && limit > 0) ? limit : DEFAULT_LIMIT;

        if (rootPath != null && !rootPath.isEmpty() && latestNewsService != null) {
            articles = latestNewsService.getLatestArticles(resourceResolver,
                                                           rootPath, effectiveLimit);
        } else {
            articles = Collections.emptyList();
        }
    }

    public List<ArticleItem> getArticles() {
        return articles;
    }

    public boolean isEmpty() {
        return articles == null || articles.isEmpty();
    }
}
