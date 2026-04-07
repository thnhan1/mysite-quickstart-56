# Fix: Di chuyen component vao dung thu muc structure/ va content/

**Ngay:** 07/04/2026  
**Ly do:** Yeu cau task.md quy dinh component phai nam trong thu muc `components/structure/` hoac `components/content/`, khong duoc dat flat trong `components/`.

---

## Loi cu

Tat ca component do toi tao deu nam flat trong `components/`:

```
components/
├── navigation/        ← sai, phai la structure
├── breadcrumb/        ← sai, phai la structure
├── latest-news/       ← sai, phai la content
├── page-item/         ← sai, phai la content
└── article-detail/    ← sai, phai la content
```

Ngoai ra `componentGroup` cung sai format:
- Archetype co san dung: `My Site - Content`
- Toi tao moi dung: `MySite - Content` (thieu space)
- Yeu cau: `mysite - structure` / `mysite - content` (lowercase)

---

## Sau khi sua

```
components/
├── structure/
│   ├── navigation/
│   │   ├── .content.xml         (componentGroup="mysite - structure")
│   │   └── navigation.html
│   └── breadcrumb/
│       ├── .content.xml         (componentGroup="mysite - structure")
│       └── breadcrumb.html
├── content/
│   ├── latest-news/
│   │   ├── .content.xml         (componentGroup="mysite - content")
│   │   ├── _cq_dialog/.content.xml
│   │   └── latest-news.html
│   ├── page-item/
│   │   ├── .content.xml         (componentGroup="mysite - content")
│   │   ├── _cq_dialog/.content.xml
│   │   └── page-item.html
│   └── article-detail/
│       ├── .content.xml         (componentGroup="mysite - content")
│       ├── _cq_dialog/.content.xml
│       └── article-detail.html
```

---

## Danh sach thay doi

### File da xoa (vi tri cu)

| File cu | Loai |
|---------|------|
| `components/navigation/.content.xml` | Xoa |
| `components/navigation/navigation.html` | Xoa |
| `components/breadcrumb/.content.xml` | Xoa |
| `components/breadcrumb/breadcrumb.html` | Xoa |
| `components/latest-news/.content.xml` | Xoa |
| `components/latest-news/_cq_dialog/.content.xml` | Xoa |
| `components/latest-news/latest-news.html` | Xoa |
| `components/page-item/.content.xml` | Xoa |
| `components/page-item/_cq_dialog/.content.xml` | Xoa |
| `components/page-item/page-item.html` | Xoa |
| `components/article-detail/.content.xml` | Xoa |
| `components/article-detail/_cq_dialog/.content.xml` | Xoa |
| `components/article-detail/article-detail.html` | Xoa |

### File da tao (vi tri moi)

| File moi | componentGroup |
|----------|---------------|
| `components/structure/navigation/.content.xml` | mysite - structure |
| `components/structure/navigation/navigation.html` | - |
| `components/structure/breadcrumb/.content.xml` | mysite - structure |
| `components/structure/breadcrumb/breadcrumb.html` | - |
| `components/content/latest-news/.content.xml` | mysite - content |
| `components/content/latest-news/_cq_dialog/.content.xml` | - |
| `components/content/latest-news/latest-news.html` | - |
| `components/content/page-item/.content.xml` | mysite - content |
| `components/content/page-item/_cq_dialog/.content.xml` | - |
| `components/content/page-item/page-item.html` | - |
| `components/content/article-detail/.content.xml` | mysite - content |
| `components/content/article-detail/_cq_dialog/.content.xml` | - |
| `components/content/article-detail/article-detail.html` | - |

### Reference da cap nhat

| File | Thay doi |
|------|---------|
| `core/.../ArticleContentFragmentModel.java` | `RESOURCE_TYPE`: `mysite/components/article-detail` → `mysite/components/content/article-detail` |
| `ui.content/.../header/master/.content.xml` | `sling:resourceType`: `mysite/components/navigation` → `mysite/components/structure/navigation` |

---

## Thong ke

| Hanh dong | So luong |
|-----------|---------|
| File xoa | 13 |
| File tao moi | 13 |
| Reference cap nhat | 2 |
