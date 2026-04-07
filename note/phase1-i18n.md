# Phase 1 Implementation Summary - News Portal (VnExpress-style)

**Ngay:** 07/04/2026
**Nguoi thuc hien:** AI Assistant
**Trang thai:** Hoan thanh tat ca 10 task

---

## Tong quan

Implement day du plan "News Portal" theo phong cach VnExpress bao gom: i18n (vi/en), cau truc 8 chuyen muc x 4 sub-topic, Content Fragment Model, 4 component (Navigation, Breadcrumb, Latest News, Page Item), workflow duyet bai viet (4 step), clientlibs, va fix bug.

---

## Danh sach file da thay doi

### Phase 0: i18n Setup

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `ui.apps/.../i18n/vi.json` | Tao moi | Tu dien tieng Viet (24 key: nav, article, breadcrumb, footer, search, workflow) |
| `ui.apps/.../i18n/vi.json.dir/.content.xml` | Tao moi | Metadata JCR cho vi.json: `jcr:language="vi"`, `mix:language` |
| `ui.apps/.../i18n/en.json` | Tao moi | Tu dien tieng Anh (24 key tuong ung voi vi.json) |
| `ui.apps/.../i18n/en.json.dir/.content.xml` | Tao moi | Metadata JCR cho en.json: `jcr:language="en"`, `mix:language` |

### Phase 1: Content Structure (8 categories x 4 sub-topics)

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `ui.content/.../content/mysite/.content.xml` | Sua | Redirect sang `/content/mysite/vi`, them `<vi/>` va `<en/>` |
| `ui.content/.../content/mysite/vi/.content.xml` | Tao moi | Language master tieng Viet, `jcr:language="vi"`, 8 category con |
| `ui.content/.../content/mysite/vi/thoi-su/.content.xml` | Tao moi | Category "Thoi su" + 4 sub-topic con |
| `ui.content/.../content/mysite/vi/thoi-su/chinh-tri/.content.xml` | Tao moi | Sub-topic "Chinh tri" |
| `ui.content/.../content/mysite/vi/thoi-su/giao-thong/.content.xml` | Tao moi | Sub-topic "Giao thong" |
| `ui.content/.../content/mysite/vi/thoi-su/moi-truong/.content.xml` | Tao moi | Sub-topic "Moi truong" |
| `ui.content/.../content/mysite/vi/thoi-su/goc-nhin/.content.xml` | Tao moi | Sub-topic "Goc nhin" |
| `ui.content/.../content/mysite/vi/the-gioi/.content.xml` | Tao moi | Category "The gioi" + 4 sub-topic con |
| `ui.content/.../content/mysite/vi/the-gioi/phan-tich/.content.xml` | Tao moi | Sub-topic "Phan tich" |
| `ui.content/.../content/mysite/vi/the-gioi/quan-su/.content.xml` | Tao moi | Sub-topic "Quan su" |
| `ui.content/.../content/mysite/vi/the-gioi/cuoc-song-do-day/.content.xml` | Tao moi | Sub-topic "Cuoc song do day" |
| `ui.content/.../content/mysite/vi/the-gioi/nguoi-viet-5-chau/.content.xml` | Tao moi | Sub-topic "Nguoi Viet 5 chau" |
| `ui.content/.../content/mysite/vi/kinh-doanh/.content.xml` | Tao moi | Category "Kinh doanh" + 4 sub-topic con |
| `ui.content/.../content/mysite/vi/kinh-doanh/chung-khoan/.content.xml` | Tao moi | Sub-topic "Chung khoan" |
| `ui.content/.../content/mysite/vi/kinh-doanh/bat-dong-san/.content.xml` | Tao moi | Sub-topic "Bat dong san" |
| `ui.content/.../content/mysite/vi/kinh-doanh/doanh-nghiep/.content.xml` | Tao moi | Sub-topic "Doanh nghiep" |
| `ui.content/.../content/mysite/vi/kinh-doanh/vi-mo/.content.xml` | Tao moi | Sub-topic "Vi mo" |
| `ui.content/.../content/mysite/vi/the-thao/.content.xml` | Tao moi | Category "The thao" + 4 sub-topic con |
| `ui.content/.../content/mysite/vi/the-thao/bong-da/.content.xml` | Tao moi | Sub-topic "Bong da" |
| `ui.content/.../content/mysite/vi/the-thao/tennis/.content.xml` | Tao moi | Sub-topic "Tennis" |
| `ui.content/.../content/mysite/vi/the-thao/cac-mon-khac/.content.xml` | Tao moi | Sub-topic "Cac mon khac" |
| `ui.content/.../content/mysite/vi/the-thao/hau-truong/.content.xml` | Tao moi | Sub-topic "Hau truong" |
| `ui.content/.../content/mysite/vi/giao-duc/.content.xml` | Tao moi | Category "Giao duc" + 4 sub-topic con |
| `ui.content/.../content/mysite/vi/giao-duc/tuyen-sinh/.content.xml` | Tao moi | Sub-topic "Tuyen sinh" |
| `ui.content/.../content/mysite/vi/giao-duc/du-hoc/.content.xml` | Tao moi | Sub-topic "Du hoc" |
| `ui.content/.../content/mysite/vi/giao-duc/trac-nghiem/.content.xml` | Tao moi | Sub-topic "Trac nghiem" |
| `ui.content/.../content/mysite/vi/giao-duc/giao-duc-4-0/.content.xml` | Tao moi | Sub-topic "Giao duc 4.0" |
| `ui.content/.../content/mysite/vi/suc-khoe/.content.xml` | Tao moi | Category "Suc khoe" + 4 sub-topic con |
| `ui.content/.../content/mysite/vi/suc-khoe/dinh-duong/.content.xml` | Tao moi | Sub-topic "Dinh duong" |
| `ui.content/.../content/mysite/vi/suc-khoe/cac-benh/.content.xml` | Tao moi | Sub-topic "Cac benh" |
| `ui.content/.../content/mysite/vi/suc-khoe/khoe-dep/.content.xml` | Tao moi | Sub-topic "Khoe dep" |
| `ui.content/.../content/mysite/vi/suc-khoe/tu-van/.content.xml` | Tao moi | Sub-topic "Tu van" |
| `ui.content/.../content/mysite/vi/du-lich/.content.xml` | Tao moi | Category "Du lich" + 4 sub-topic con |
| `ui.content/.../content/mysite/vi/du-lich/diem-den/.content.xml` | Tao moi | Sub-topic "Diem den" |
| `ui.content/.../content/mysite/vi/du-lich/am-thuc/.content.xml` | Tao moi | Sub-topic "Am thuc" |
| `ui.content/.../content/mysite/vi/du-lich/cam-nang/.content.xml` | Tao moi | Sub-topic "Cam nang" |
| `ui.content/.../content/mysite/vi/du-lich/dau-chan/.content.xml` | Tao moi | Sub-topic "Dau chan" |
| `ui.content/.../content/mysite/vi/khoa-hoc-cong-nghe/.content.xml` | Tao moi | Category "Khoa hoc & Cong nghe" + 4 sub-topic con |
| `ui.content/.../content/mysite/vi/khoa-hoc-cong-nghe/ai/.content.xml` | Tao moi | Sub-topic "AI" |
| `ui.content/.../content/mysite/vi/khoa-hoc-cong-nghe/chuyen-doi-so/.content.xml` | Tao moi | Sub-topic "Chuyen doi so" |
| `ui.content/.../content/mysite/vi/khoa-hoc-cong-nghe/doi-moi-sang-tao/.content.xml` | Tao moi | Sub-topic "Doi moi sang tao" |
| `ui.content/.../content/mysite/vi/khoa-hoc-cong-nghe/vu-tru/.content.xml` | Tao moi | Sub-topic "Vu tru" |
| `ui.content/.../content/mysite/en/.content.xml` | Tao moi | Language root tieng Anh, `jcr:language="en"`, 8 category con |
| `ui.content/.../content/mysite/en/current-affairs/.content.xml` | Tao moi | Category "Current Affairs" + 4 sub-topic |
| `ui.content/.../content/mysite/en/current-affairs/politics/.content.xml` | Tao moi | Sub-topic "Politics" |
| `ui.content/.../content/mysite/en/current-affairs/traffic/.content.xml` | Tao moi | Sub-topic "Traffic" |
| `ui.content/.../content/mysite/en/current-affairs/environment/.content.xml` | Tao moi | Sub-topic "Environment" |
| `ui.content/.../content/mysite/en/current-affairs/perspective/.content.xml` | Tao moi | Sub-topic "Perspective" |
| `ui.content/.../content/mysite/en/world-news/.content.xml` | Tao moi | Category "World News" + 4 sub-topic |
| `ui.content/.../content/mysite/en/world-news/analysis/.content.xml` | Tao moi | Sub-topic "Analysis" |
| `ui.content/.../content/mysite/en/world-news/military/.content.xml` | Tao moi | Sub-topic "Military" |
| `ui.content/.../content/mysite/en/world-news/life-abroad/.content.xml` | Tao moi | Sub-topic "Life Abroad" |
| `ui.content/.../content/mysite/en/world-news/vietnamese-overseas/.content.xml` | Tao moi | Sub-topic "Vietnamese Overseas" |
| `ui.content/.../content/mysite/en/business/.content.xml` | Tao moi | Category "Business" + 4 sub-topic |
| `ui.content/.../content/mysite/en/business/stock-market/.content.xml` | Tao moi | Sub-topic "Stock Market" |
| `ui.content/.../content/mysite/en/business/real-estate/.content.xml` | Tao moi | Sub-topic "Real Estate" |
| `ui.content/.../content/mysite/en/business/enterprises/.content.xml` | Tao moi | Sub-topic "Enterprises" |
| `ui.content/.../content/mysite/en/business/macro-economy/.content.xml` | Tao moi | Sub-topic "Macro Economy" |
| `ui.content/.../content/mysite/en/sports/.content.xml` | Tao moi | Category "Sports" + 4 sub-topic |
| `ui.content/.../content/mysite/en/sports/football/.content.xml` | Tao moi | Sub-topic "Football" |
| `ui.content/.../content/mysite/en/sports/tennis/.content.xml` | Tao moi | Sub-topic "Tennis" |
| `ui.content/.../content/mysite/en/sports/other-sports/.content.xml` | Tao moi | Sub-topic "Other Sports" |
| `ui.content/.../content/mysite/en/sports/behind-the-scenes/.content.xml` | Tao moi | Sub-topic "Behind the Scenes" |
| `ui.content/.../content/mysite/en/education/.content.xml` | Tao moi | Category "Education" + 4 sub-topic |
| `ui.content/.../content/mysite/en/education/admissions/.content.xml` | Tao moi | Sub-topic "Admissions" |
| `ui.content/.../content/mysite/en/education/study-abroad/.content.xml` | Tao moi | Sub-topic "Study Abroad" |
| `ui.content/.../content/mysite/en/education/quizzes/.content.xml` | Tao moi | Sub-topic "Quizzes" |
| `ui.content/.../content/mysite/en/education/education-4-0/.content.xml` | Tao moi | Sub-topic "Education 4.0" |
| `ui.content/.../content/mysite/en/health/.content.xml` | Tao moi | Category "Health" + 4 sub-topic |
| `ui.content/.../content/mysite/en/health/nutrition/.content.xml` | Tao moi | Sub-topic "Nutrition" |
| `ui.content/.../content/mysite/en/health/diseases/.content.xml` | Tao moi | Sub-topic "Diseases" |
| `ui.content/.../content/mysite/en/health/wellness-beauty/.content.xml` | Tao moi | Sub-topic "Wellness & Beauty" |
| `ui.content/.../content/mysite/en/health/consultation/.content.xml` | Tao moi | Sub-topic "Consultation" |
| `ui.content/.../content/mysite/en/travel/.content.xml` | Tao moi | Category "Travel" + 4 sub-topic |
| `ui.content/.../content/mysite/en/travel/destinations/.content.xml` | Tao moi | Sub-topic "Destinations" |
| `ui.content/.../content/mysite/en/travel/cuisine/.content.xml` | Tao moi | Sub-topic "Cuisine" |
| `ui.content/.../content/mysite/en/travel/travel-guide/.content.xml` | Tao moi | Sub-topic "Travel Guide" |
| `ui.content/.../content/mysite/en/travel/footprints/.content.xml` | Tao moi | Sub-topic "Footprints" |
| `ui.content/.../content/mysite/en/science-and-tech/.content.xml` | Tao moi | Category "Science & Technology" + 4 sub-topic |
| `ui.content/.../content/mysite/en/science-and-tech/ai/.content.xml` | Tao moi | Sub-topic "AI" |
| `ui.content/.../content/mysite/en/science-and-tech/digital-transformation/.content.xml` | Tao moi | Sub-topic "Digital Transformation" |
| `ui.content/.../content/mysite/en/science-and-tech/innovation/.content.xml` | Tao moi | Sub-topic "Innovation" |
| `ui.content/.../content/mysite/en/science-and-tech/space/.content.xml` | Tao moi | Sub-topic "Space" |

### Phase 2: Content Fragment Model

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `ui.content/.../conf/mysite/settings/dam/.content.xml` | Sua | Them `<article/>` vao `<models>` |
| `ui.content/.../conf/mysite/settings/dam/cfm/models/article/.content.xml` | Tao moi | CF Model "Article" voi 8 field: headline, summary, body, featuredImage, author, publishedDate, category, tags |

### Phase 3: Components - HTL, Dialog, Sling Model

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `ui.apps/.../components/navigation/navigation.html` | Tao moi | HTL override Core Navigation: dropdown 2 cap, ARIA roles, mobile hamburger |
| `ui.apps/.../components/breadcrumb/breadcrumb.html` | Tao moi | HTL override Core Breadcrumb: Schema.org BreadcrumbList, separator, aria-current |
| `ui.apps/.../components/latest-news/.content.xml` | Tao moi | Component definition: "Latest News", componentGroup="MySite - Content" |
| `ui.apps/.../components/latest-news/_cq_dialog/.content.xml` | Tao moi | Dialog: rootPath (pathfield), limit (numberfield, default 10) |
| `ui.apps/.../components/latest-news/latest-news.html` | Tao moi | HTL: grid cac article card voi category, title, summary, author, date |
| `ui.apps/.../components/page-item/.content.xml` | Tao moi | Component definition: "Page Item", componentGroup="MySite - Content" |
| `ui.apps/.../components/page-item/_cq_dialog/.content.xml` | Tao moi | Dialog: pagePath (pathfield) de chon article page |
| `ui.apps/.../components/page-item/page-item.html` | Tao moi | HTL: article card voi image, title, summary, author, date |
| `core/.../models/dto/ArticleItem.java` | Tao moi | DTO: title, summary, imagePath, url, author, publishedDate, categoryTitle, formattedDate |
| `core/.../models/PageItemModel.java` | Tao moi | Sling Model (Resource): doc page properties tu pagePath hoac containing page |
| `core/.../models/LatestNewsModel.java` | Tao moi | Sling Model (SlingHttpServletRequest): inject LatestNewsService, tra ve List\<ArticleItem\> |
| `core/.../services/LatestNewsService.java` | Tao moi | Interface: getLatestArticles(resolver, rootPath, limit), invalidateCache(rootPath) |
| `core/.../services/impl/LatestNewsServiceImpl.java` | Tao moi | Impl: QueryBuilder query, ConcurrentHashMap cache voi TTL (configurable OSGi, default 300s) |

### Phase 4: Article Approval Workflow

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `core/.../workflow/ValidateArticleStep.java` | Tao moi | WorkflowProcess: validate jcr:title, jcr:description; set articleStatus="pending-review" |
| `core/.../workflow/AssignReviewerStep.java` | Tao moi | ParticipantStepChooser: route den group "reviewers" cho /content/mysite |
| `core/.../workflow/ApproveArticleStep.java` | Tao moi | WorkflowProcess: set articleStatus="approved", approvedBy, approvedDate |
| `core/.../workflow/RejectArticleStep.java` | Tao moi | WorkflowProcess: set articleStatus="rejected", rejectedBy, rejectedReason |
| `ui.content/.../var/workflow/models/article-review/.content.xml` | Tao moi | Workflow model: Start -> Validate -> Dynamic Participant -> OR Split (Approve/Reject) -> End |
| `ui.content/.../META-INF/vault/filter.xml` | Sua | Them filter root `/var/workflow/models/article-review` |

### Phase 5: ClientLibs

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `ui.apps/.../clientlibs/clientlib-site/css/navigation.css` | Tao moi | VnExpress-style nav: background #b80000, dropdown hover, mobile hamburger toggle |
| `ui.apps/.../clientlibs/clientlib-site/css/breadcrumb.css` | Tao moi | Breadcrumb: link color, separator, current page style |
| `ui.apps/.../clientlibs/clientlib-site/css/latest-news.css` | Tao moi | News grid: auto-fill cards, hover/focus states, image zoom, category badge |
| `ui.apps/.../clientlibs/clientlib-site/css/page-item.css` | Tao moi | Article card: horizontal layout, image + content, responsive stack mobile |
| `ui.apps/.../clientlibs/clientlib-site/css/responsive.css` | Tao moi | Global: box-sizing, container max-width 1200px, focus-visible, skip-link, print |
| `ui.apps/.../clientlibs/clientlib-site/js/navigation.js` | Tao moi | Mobile hamburger toggle, keyboard dropdown, click-outside close |
| `ui.apps/.../clientlibs/clientlib-site/css.txt` | Sua | Them 5 CSS file moi vao manifest |
| `ui.apps/.../clientlibs/clientlib-site/js.txt` | Sua | Them navigation.js vao manifest |

### Bugfix

| File | Trang thai | Tac dung |
|------|-----------|----------|
| `core/.../workflow/ApprovePageContentStep.java` | Sua | Fix bug line 34: `getPayloadType()` -> `getPayload().toString()` |

---

## Thong ke

| Loai | So luong |
|------|---------|
| File tao moi | ~100 |
| File sua | 6 |
| Java class moi | 9 |
| HTL file moi | 5 |
| CSS file moi | 5 |
| JS file moi | 1 |
| Content page (vi) | 41 (1 root + 8 category + 32 sub-topic) |
| Content page (en) | 41 (1 root + 8 category + 32 sub-topic) |
| i18n dictionary | 2 (vi, en) x 24 key |
| Workflow step | 4 moi + 1 fix |
| CF Model | 1 (Article, 8 field) |
