# OSGi Configuration Summary

**Ngay:** 07/04/2026  
**Vi tri:** `ui.config/src/main/content/jcr_root/apps/mysite/osgiconfig/`

---

## Tong quan

Cac file OSGi config (.cfg.json) dieu khien hanh vi runtime cua AEM khi deploy. Thay vi tao content structure bang .content.xml thu cong, cach chuan la su dung **Sling Repository Initializer (repoinit)** de tao paths, service users, groups, va ACLs khi AEM khoi dong.

---

## Cau truc thu muc

```
osgiconfig/
├── config/                          <-- Tat ca run modes (author + publish)
│   ├── com.adobe.cq.wcm.core...TableOfContentsFilter~mysite.cfg.json
│   ├── com.mysite.core.services.impl.LatestNewsServiceImpl.cfg.json      [MOI]
│   ├── org.apache.sling.jcr.repoinit.RepositoryInitializer~mysite.cfg.json
│   ├── org.apache.sling.jcr.repoinit.RepositoryInitializer~mysite-content.cfg.json  [MOI]
│   ├── org.apache.sling.jcr.repoinit.RepositoryInitializer~mysite-workflow.cfg.json [MOI]
│   └── org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-mysite.cfg.json  [MOI]
│
├── config.author/                   <-- Chi Author instance
│   ├── com.adobe.granite.cors.impl.CORSPolicyImpl~mysite.cfg.json
│   ├── com.day.cq.wcm.mobile...MobileEmulatorProvider~mysite.cfg.json
│   ├── com.mysite.core.services.impl.LatestNewsServiceImpl.cfg.json      [MOI]
│   ├── org.apache.sling.commons.log.LogManager.factory.config~mysite.cfg.json  [SUA]
│   ├── org.apache.sling.distribution...(3 files)
│   ├── org.apache.sling.jcr.repoinit.RepositoryInitializer-mysite.cfg.json     [SUA]
│   └── org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-mysite.cfg.json
│
├── config.publish/                  <-- Chi Publish instance
│   ├── org.apache.sling.jcr.resource.internal.JcrResourceResolverFactoryImpl.cfg.json
│   ├── org.apache.sling.jcr.repoinit.RepositoryInitializer-mysite.cfg.json    [MOI]
│   └── org.apache.sling.commons.log.LogManager.factory.config~mysite.cfg.json [MOI]
│
├── config.stage/                    <-- Chi Stage
│   └── org.apache.sling.commons.log.LogManager.factory.config~mysite.cfg.json
│
└── config.prod/                     <-- Chi Production
    └── org.apache.sling.commons.log.LogManager.factory.config~mysite.cfg.json
```

---

## Chi tiet cac file da tao/sua

### 1. Repoinit - Content Structure (MOI)

| File | `config/org.apache.sling.jcr.repoinit.RepositoryInitializer~mysite-content.cfg.json` |
|------|---|
| Run mode | Tat ca (author + publish) |
| Tac dung | Tao toan bo cau truc content pages qua repoinit thay vi .content.xml thu cong |

**Noi dung:**
- Tao `/content/mysite/vi` (language root, jcr:language=vi)
- Tao `/content/mysite/en` (language root, jcr:language=en)
- 8 categories tieng Viet: thoi-su, the-gioi, kinh-doanh, the-thao, giao-duc, suc-khoe, du-lich, khoa-hoc-cong-nghe
- 32 sub-topics tieng Viet (4 moi category)
- 8 categories tieng Anh: current-affairs, world-news, business, sports, education, health, travel, science-and-tech
- 32 sub-topics tieng Anh (4 moi category)
- Tong: 82 pages (2 roots + 16 categories + 64 sub-topics)

### 2. Repoinit - Workflow Groups + ACL (MOI)

| File | `config/org.apache.sling.jcr.repoinit.RepositoryInitializer~mysite-workflow.cfg.json` |
|------|---|
| Run mode | Tat ca |
| Tac dung | Tao groups cho workflow, cap quyen cho groups |

**Noi dung:**
- Tao group `reviewers` tai `/home/groups/mysite` (de dung trong AssignReviewerStep)
- Tao group `journalists` tai `/home/groups/mysite`
- Tao path `/var/workflow/models/article-review`
- Cap `jcr:read,rep:write` cho reviewers tren `/content/mysite`
- Cap `jcr:read,rep:write` cho journalists tren `/content/mysite`

### 3. Repoinit - Service User - Author (SUA)

| File | `config.author/org.apache.sling.jcr.repoinit.RepositoryInitializer-mysite.cfg.json` |
|------|---|
| Run mode | Author |
| Thay doi | Mo rong ACL tu chi /content/mysite sang them DAM, conf, XF, workflow |

**ACL moi cho mysiteServiceUser:**
- `jcr:read` tren `/content/mysite`
- `jcr:read` tren `/content/dam/mysite`
- `jcr:read` tren `/conf/mysite`
- `jcr:read` tren `/content/experience-fragments/mysite`
- `jcr:read` tren `/var/workflow/models`

### 4. Repoinit - Service User - Publish (MOI)

| File | `config.publish/org.apache.sling.jcr.repoinit.RepositoryInitializer-mysite.cfg.json` |
|------|---|
| Run mode | Publish |
| Tac dung | Tao service user voi quyen read-only tren publish |

**ACL cho mysiteServiceUser (publish):**
- `jcr:read` tren `/content/mysite`
- `jcr:read` tren `/content/dam/mysite`
- `jcr:read` tren `/conf/mysite`

### 5. Service User Mapping (MOI)

| File | `config/org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-mysite.cfg.json` |
|------|---|
| Run mode | Tat ca |
| Tac dung | Map bundle `mysite.core` subservice `mysite-service-user` → `mysiteServiceUser` cho tat ca run modes |

### 6. LatestNewsService Config - Default (MOI)

| File | `config/com.mysite.core.services.impl.LatestNewsServiceImpl.cfg.json` |
|------|---|
| Run mode | Tat ca (default) |
| Tac dung | Cache TTL = 300 giay (5 phut) |

### 7. LatestNewsService Config - Author (MOI)

| File | `config.author/com.mysite.core.services.impl.LatestNewsServiceImpl.cfg.json` |
|------|---|
| Run mode | Author |
| Tac dung | Cache TTL = 60 giay (1 phut) - ngan hon de author thay content moi nhanh hon |

### 8. Logging - Author (SUA)

| File | `config.author/org.apache.sling.commons.log.LogManager.factory.config~mysite.cfg.json` |
|------|---|
| Run mode | Author |
| Thay doi | Them packages workflow, services, models; doi file name → mysite.log |

**Packages duoc log (DEBUG):**
- `com.mysite.core`
- `com.mysite.core.workflow`
- `com.mysite.core.services`
- `com.mysite.core.models`

### 9. Logging - Publish (MOI)

| File | `config.publish/org.apache.sling.commons.log.LogManager.factory.config~mysite.cfg.json` |
|------|---|
| Run mode | Publish |
| Tac dung | Log level WARN cho publish (performance) |

---

## Tong hop

| Loai | File moi | File sua | Tong |
|------|----------|----------|------|
| Repoinit | 4 | 1 | 5 |
| Service User Mapping | 1 | 0 | 1 |
| LatestNewsService | 2 | 0 | 2 |
| Logging | 1 | 1 | 2 |
| **Tong** | **8** | **2** | **10** |

---

## Luu y quan trong

1. **Repoinit vs .content.xml**: Repoinit la cach chuan AEM de tao content structure, service users, groups, va ACLs. No chay khi AEM khoi dong va dam bao idem-potent (chay nhieu lan khong loi).

2. **Run mode hierarchy**: `config` (tat ca) → `config.author` / `config.publish` (override). Neu cung mot PID co o ca `config` va `config.author`, AEM se dung `config.author` tren author instance.

3. **Service user mapping**: Duoc dung boi `ResourceResolverFactory.getServiceResourceResolver()` trong code Java (vd: `ReadJCRViaResourceResolverServiceServlet`, `TagByTitleProcess`).

4. **Workflow steps** (`ValidateArticleStep`, `ApproveArticleStep`, `RejectArticleStep`) su dung `workflowSession.adaptTo(Session.class)` - dung session cua workflow user, KHONG dung service user. Day la pattern chuan cho workflow.
