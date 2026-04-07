package com.mysite.core.services;

import com.mysite.core.models.dto.ArticleItem;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;

public interface LatestNewsService {

    List<ArticleItem> getLatestArticles(ResourceResolver resourceResolver,
                                        String rootPath, int limit);

    void invalidateCache(String rootPath);
}
