package com.mysite.core.services.impl;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.mysite.core.models.dto.ArticleItem;
import com.mysite.core.services.LatestNewsService;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component(service = LatestNewsService.class, immediate = true)
@Designate(ocd = LatestNewsServiceImpl.Config.class)
public class LatestNewsServiceImpl implements LatestNewsService {

    private static final Logger LOG = LoggerFactory.getLogger(LatestNewsServiceImpl.class);

    @ObjectClassDefinition(name = "MySite - Latest News Service Configuration")
    @interface Config {
        @AttributeDefinition(name = "Cache TTL (seconds)",
                description = "Time-to-live for cached results in seconds")
        long cacheTtlSeconds() default 300;
    }

    @Reference
    private QueryBuilder queryBuilder;

    private long cacheTtlMs;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Activate
    @Modified
    protected void activate(Config config) {
        cacheTtlMs = config.cacheTtlSeconds() * 1000L;
        cache.clear();
        LOG.info("LatestNewsService activated with cacheTTL={}s", config.cacheTtlSeconds());
    }

    @Override
    public List<ArticleItem> getLatestArticles(ResourceResolver resourceResolver,
                                                String rootPath, int limit) {
        if (rootPath == null || rootPath.isEmpty()) {
            return Collections.emptyList();
        }

        String cacheKey = rootPath + ":" + limit;
        CacheEntry entry = cache.get(cacheKey);
        if (entry != null && !entry.isExpired(cacheTtlMs)) {
            LOG.debug("Cache hit for key={}", cacheKey);
            return entry.getData();
        }

        LOG.debug("Cache miss for key={}, querying JCR", cacheKey);
        List<ArticleItem> articles = queryLatestPages(resourceResolver, rootPath, limit);
        cache.put(cacheKey, new CacheEntry(articles));
        return articles;
    }

    @Override
    public void invalidateCache(String rootPath) {
        if (rootPath == null) {
            cache.clear();
            return;
        }
        cache.entrySet().removeIf(e -> e.getKey().startsWith(rootPath + ":"));
        LOG.info("Cache invalidated for rootPath={}", rootPath);
    }

    private List<ArticleItem> queryLatestPages(ResourceResolver resolver,
                                                String rootPath, int limit) {
        List<ArticleItem> results = new ArrayList<>();
        Session session = resolver.adaptTo(Session.class);
        if (session == null) {
            return results;
        }

        Map<String, String> predicates = new HashMap<>();
        predicates.put("path", rootPath);
        predicates.put("type", "cq:Page");
        predicates.put("property", "jcr:content/jcr:title");
        predicates.put("property.operation", "exists");
        predicates.put("orderby", "@jcr:content/cq:lastModified");
        predicates.put("orderby.sort", "desc");
        predicates.put("p.limit", String.valueOf(limit));

        try {
            Query query = queryBuilder.createQuery(PredicateGroup.create(predicates), session);
            SearchResult result = query.getResult();
            PageManager pageManager = resolver.adaptTo(PageManager.class);
            if (pageManager == null) {
                return results;
            }

            for (Hit hit : result.getHits()) {
                Page page = pageManager.getPage(hit.getPath());
                if (page == null) {
                    continue;
                }
                ArticleItem item = buildArticleItem(page, pageManager);
                if (item != null) {
                    results.add(item);
                }
            }
        } catch (RepositoryException e) {
            LOG.error("Error querying latest news under {}", rootPath, e);
        }
        return results;
    }

    private ArticleItem buildArticleItem(Page page, PageManager pageManager) {
        ValueMap props = page.getProperties();
        String title = props.get("jcr:title", page.getName());
        String summary = props.get("jcr:description", "");
        String imagePath = props.get("image/fileReference", "");
        String url = page.getPath() + ".html";
        String author = props.get("author", "");
        Calendar publishedDate = props.get("publishedDate",
                                  props.get("cq:lastModified", Calendar.class));

        String categoryTitle = "";
        Page parent = page.getParent();
        if (parent != null) {
            categoryTitle = parent.getTitle();
        }

        return new ArticleItem(title, summary, imagePath, url, author,
                              publishedDate, categoryTitle);
    }

    private static class CacheEntry {
        private final List<ArticleItem> data;
        private final long createdAt;

        CacheEntry(List<ArticleItem> data) {
            this.data = Collections.unmodifiableList(data);
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired(long ttlMs) {
            return (System.currentTimeMillis() - createdAt) > ttlMs;
        }

        List<ArticleItem> getData() {
            return data;
        }
    }
}
