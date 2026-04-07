# Content Fragment Integration - Implementation Summary

**Ngay:** 07/04/2026  
**Muc dich:** Ket noi Page voi Content Fragment de doc du lieu bai bao tu CF thay vi page properties, ho tro JSON API (Sling Model Exporter) cho headless.

---

## Kien truc

```
DAM (/content/dam/mysite/articles/)
  └── bai-viet-abc  (Content Fragment, model = Article)
        ├── headline
        ├── summary
        ├── body (rich text)
        ├── featuredImage (content-reference)
        ├── author
        ├── publishedDate
        └── category (enumeration)

Page (/content/mysite/vi/thoi-su/bai-viet-abc)
  └── jcr:content
        ├── fragmentPath = "/content/dam/mysite/articles/bai-viet-abc"
        └── sling:resourceType = "mysite/components/page"
```

**Logic:** Khi page co property `fragmentPath`, tat ca Sling Model se uu tien doc data tu Content Fragment. Neu khong co `fragmentPath`, fallback ve page properties (backward compatible).

---

## Danh sach file thay doi

### Java - Sling Model & Service

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `core/.../models/ArticleContentFragmentModel.java` | Tao moi | Sling Model chinh cho article-detail component. Adaptable tu `SlingHttpServletRequest`. Doc CF qua `fragmentPath` (tu dialog hoac page property). Expose toan bo field cua CF Model (headline, summary, body, featuredImage, author, publishedDate, category). Implement `ComponentExporter` voi `@Exporter` annotation cho JSON API. |
| `core/.../models/PageItemModel.java` | Sua | Them logic fallback: neu page co `fragmentPath` → goi `populateFromContentFragment()` doc data tu CF. Neu CF khong ton tai hoac khong co `fragmentPath` → goi `populateFromPageProperties()` nhu cu. Them method helper `getElementValue()` de doc CF element an toan. |
| `core/.../services/impl/LatestNewsServiceImpl.java` | Sua | Tach `buildArticleItem()` thanh 2 method: `buildFromContentFragment()` va `buildFromPageProperties()`. Khi page co `fragmentPath`, uu tien doc tu CF. Fallback ve page properties neu CF khong resolve duoc. |

### HTL & Component Definition

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `ui.apps/.../components/article-detail/.content.xml` | Tao moi | Component definition: `jcr:title="Article Detail"`, `componentGroup="MySite - Content"`, `sling:resourceSuperType="core/wcm/components/contentfragment/v1/contentfragment"` |
| `ui.apps/.../components/article-detail/_cq_dialog/.content.xml` | Tao moi | Dialog voi 1 field: `fragmentPath` (pathfield, rootPath="/content/dam/mysite", required) |
| `ui.apps/.../components/article-detail/article-detail.html` | Tao moi | HTL render full article: category badge, headline (h1), summary (italic, border-left), meta (author + date), featured image, body (rich text voi `context='html'`). Khi chua chon CF, hien placeholder trong edit mode. |

### CSS & ClientLib

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `ui.apps/.../clientlibs/clientlib-site/css/article-detail.css` | Tao moi | CSS cho article-detail: max-width 800px centered, category badge (#b80000), headline 2rem bold, summary italic voi border-left, meta row, featured image full-width, body typography (p, h2, h3, blockquote, a), empty state placeholder, responsive mobile (<768px). |
| `ui.apps/.../clientlibs/clientlib-site/css.txt` | Sua | Them `article-detail.css` truoc `site.css` |

### Dialog Update

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `ui.apps/.../components/page-item/_cq_dialog/.content.xml` | Sua | Them note giai thich cho author: "If the selected page has a 'fragmentPath' property pointing to a Content Fragment, article data will be read from the CF instead of page properties." |

### i18n

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `ui.apps/.../i18n/vi.json` | Sua | Them key `article.selectFragment`: "Vui long chon mot Content Fragment bai bao." |
| `ui.apps/.../i18n/en.json` | Sua | Them key `article.selectFragment`: "Please select an Article Content Fragment." |

---

## Thong ke

| Loai | Tao moi | Sua | Tong |
|------|---------|-----|------|
| Java (Sling Model) | 1 | 2 | 3 |
| HTL | 1 | 0 | 1 |
| Component XML | 2 | 1 | 3 |
| CSS | 1 | 0 | 1 |
| ClientLib manifest | 0 | 1 | 1 |
| i18n | 0 | 2 | 2 |
| **Tong** | **5** | **6** | **11** |

---

## Chi tiet ky thuat

### ArticleContentFragmentModel

- **Adaptable:** `SlingHttpServletRequest`
- **Resource type:** `mysite/components/article-detail`
- **Implements:** `ComponentExporter` (cho Sling Model Exporter)
- **@Exporter:** `jackson`, extension `.model.json`
- **Logic resolve CF path:**
  1. Doc `fragmentPath` tu component dialog (ValueMapValue)
  2. Neu khong co, doc tu `currentPage.getProperties().get("fragmentPath")`
  3. Neu van khong co → model.resolved = false
- **CF API:** `ContentFragment.getElement(name)` → `FragmentData.getValue(type)`
- **JSON output mau:**

```json
{
  "headline": "Tieu de bai viet",
  "summary": "Tom tat ngan",
  "body": "<p>Noi dung rich text...</p>",
  "featuredImage": "/content/dam/mysite/articles/image.jpg",
  "author": "Nguyen Van A",
  "publishedDate": "07/04/2026 14:30",
  "category": "thoi-su",
  "url": "/content/mysite/vi/thoi-su/bai-viet.html",
  ":type": "mysite/components/article-detail"
}
```

### PageItemModel - CF Fallback Logic

```
init()
  ├── resolve page path
  ├── page.getProperties().get("fragmentPath")
  │     ├── co fragmentPath → populateFromContentFragment(cfPath)
  │     │     ├── resolve CF resource
  │     │     ├── adaptTo(ContentFragment.class)
  │     │     ├── doc headline, summary, featuredImage, author, publishedDate
  │     │     └── fallback title tu page neu CF khong co headline
  │     └── khong co → populateFromPageProperties(page)
  │           └── doc jcr:title, jcr:description, image/fileReference, author, cq:lastModified
  └── format date
```

### LatestNewsServiceImpl - CF-aware buildArticleItem

```
buildArticleItem(page, resolver)
  ├── page.getProperties().get("fragmentPath")
  │     ├── co → buildFromContentFragment(page, cfPath, resolver)
  │     │     ├── resolve CF, adaptTo ContentFragment
  │     │     ├── doc headline, summary, featuredImage, author, publishedDate
  │     │     └── return ArticleItem (hoac null neu fail)
  │     └── null hoac fail → buildFromPageProperties(page, props)
  └── return ArticleItem
```

---

## Cach su dung

### 1. Author tao Content Fragment
- Vao DAM → `/content/dam/mysite/articles/`
- Tao CF moi tu model "Article"
- Dien cac field: headline, summary, body, featuredImage, author, publishedDate, category

### 2. Author tao/sua page bai bao
- Set property `fragmentPath` = duong dan den CF vua tao
- Hoac dung article-detail component trong page, chon CF trong dialog

### 3. JSON API (headless)
- Goi `GET /content/mysite/vi/thoi-su/bai-viet.model.json`
- Tra ve JSON tu Sling Model Exporter

### 4. Backward compatible
- Nhung page khong co `fragmentPath` van hoat dong binh thuong
- PageItemModel va LatestNewsService tu dong fallback ve page properties
