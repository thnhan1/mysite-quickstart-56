Service User = Quyền đọc/ghi JCR, không liên quan HTTP
-u admin:admin
→ Xác thực HTTP "mày là ai?"
→ Không liên quan JCR bên trong


Service User
→ Code Java được phép đọc/ghi path nào trong JCR
→ Không liên quan HTTP authentication

Tại sao cần Service User?
Vì bên trong OSGi Service, code Java cần mở ResourceResolver để đọc JCR — nhưng không có HTTP request nào ở đó cả, nên không thể dùng quyền của user đang login.
java// OSGi Service chạy background — không có request, không có user
// → Lấy resolver bằng ai? → Service User
try (ResourceResolver resolver =
resolverFactory.getServiceResourceResolver(param)) {
// mysite-service-user chỉ được đọc /content, không được ghi
// → an toàn, đúng nguyên tắc least privilege
}
```

---

## So sánh thực tế
```
Servlet (có HTTP request)
→ request.getResourceResolver()
→ dùng quyền của người đang login (admin, john, anonymous...)
→ Không cần Service User


OSGi Service / Scheduled Job / Event Handler (không có HTTP request)
→ resolverFactory.getServiceResourceResolver(param)
→ dùng Service User
→ BẮT BUỘC phải có Service User

Một câu nhớ mãi

-u admin:admin = HTTP — "Tôi là admin, cho tôi vào"
Service User = JCR — "Code này được phép đọc/ghi chỗ nào trong repository"
Hai thứ trực giao, không ảnh hưởng nhau.
# Service user mapping đúng chưa? http://localhost:4502/system/console/serviceusers → Tìm "mysite-service-user"cai url nay sai8:34 PMĐúng, url đó không tồn tại trong AEMaaCS SDK. Verify service user đúng chỗ là:

Kiểm tra Service User Mapping
# 1. Kiểm tra mapping đã load chưa
http://localhost:4502/system/console/configMgr
→ Tìm "ServiceUserMapper"
→ Click vào "Apache Sling Service User Mapper Service Amended"
→ Xem user.mapping có dòng "com.mysite.core:mysite-read-service=[mysite-service-user]" không

Kiểm tra System User đã tạo chưa
# 2. Kiểm tra user tồn tại trong JCR
http://localhost:4502/crx/de
→ Vào path: /home/users/system
→ Tìm node "mysite-service-user"

Kiểm tra Bundle active chưa
# 3. Bundle
http://localhost:4502/system/console/bundles
→ Tìm "com.mysite.core" → Status phải là "Active"

# 4. Component/Service
http://localhost:4502/system/console/components
→ Tìm "PageListServiceImpl" → Status phải là "satisfied"

Nếu vẫn LoginException
Kiểm tra log tại:
bashtail -f crx-quickstart/logs/error.log | grep -i "mysite-service-user\|LoginException"
Lỗi thường gặp là repoinit script chưa chạy → system user chưa được tạo trong /home/users/system.tôi thấy như ảnh là tôi dang sai cho nao8:36 PMNhìn vào ảnh, mapping của bạn thiếu dấu ngoặc vuông [] bao quanh tên user.

Sai vs Đúng
# ❌ Bạn đang có
com.mysite.core:mysite-read-service=mysite-service-user

# ✅ Phải là
com.mysite.core:mysite-read-service=[mysite-service-user]

Fix trong file cfg.json
json// ui.config/src/main/content/jcr_root/apps/mysite/osgiconfig/config/org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-mysite.cfg.json
{
"user.mapping": [
"com.mysite.core:mysite-read-service=[mysite-service-user]"
]
}
Dấu [] là bắt buộc — đây là cú pháp để AEM hiểu đây là principal name chứ không phải username thông thường.

Sau khi fix
Rebuild deploy lại:
bashmvn clean install -PautoInstallBundle
Vào lại configMgr kiểm tra mapping đã có [] chưa, rồi thử call API lại xem còn LoginException không.22.03.2026 20:38:13.738 *ERROR* [[0:0:0:0:0:0:0:1] [1774186683980] GET /bin/mysite/pagelist HTTP/1.1] com.mysite.core.services.impl.PageListServiceImpl Cannot obtain ResourceResolver with subservice 'mysite-read-service': Cannot derive user name for bundle mysite.core [610] and sub service mysite-read-serviceorg.apache.sling.api.resource.LoginException: Cannot derive user name for bundle mysite.core [610] and sub service mysite-read-service    at org.apache.sling.resourceresolver.impl.ResourceResolverFactoryImpl.getServiceResourceResolver(ResourceResolverFactoryImpl.java:79) [org.apache.sling.resourceresolver:1.10.0]    at com.mysite.core.services.impl.PageListServiceImpl.getChildPageTitles(PageListServiceImpl.java:41) [mysite.core:1.0.0.SNAPSHOT]    at com.mysite.core.servlets.PageListServlet.doGet(PageListServlet.java:46) [mysite.core:1.0.0.SNAPSHOT]    at org.apache.sling.api.servlets.SlingSafeMethodsServlet.mayService(SlingSafeMethodsServlet.java:266) [org.apache.sling.api:2.27.2]    at org.apache.sling.api.servlets.SlingSafeMethodsServlet.service(SlingSafeMethodsServlet.java:342) [org.apache.sling.api:2.27.2]    at org.apache.sling.api.servlets.SlingSafeMethodsServlet.service(SlingSafeMethodsServlet.java:374) [org.apache.sling.api:2.27.2]    at org.apache.sling.engine.impl.request.RequestData.service(RequestData.java:539) [org.apache.sling.engine:2.15.2.SLING11974]    at org.apache.sling.engiShow more8:39 PMLỗi rõ ràng:
Cannot derive user name for bundle mysite.core [610]
and sub service mysite-read-service
Bundle Symbolic Name thực tế là mysite.core (dùng dấu chấm), không phải com.mysite.core.

Fix file cfg.json
json// ui.config/src/main/content/jcr_root/apps/mysite/osgiconfig/config/org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-mysite.cfg.json
{
"user.mapping": [
"mysite.core:mysite-read-service=[mysite-service-user]"
]
}
```

---

## Cách tìm Bundle Symbolic Name chính xác cho lần sau
```
http://localhost:4502/system/console/bundles
→ Tìm bundle của project
→ Click vào tên bundle
→ Xem field "Symbolic Name"
Hoặc trong core/pom.xml tìm:
xml<plugin>
<groupId>biz.aQute.bnd</groupId>
<artifactId>bnd-maven-plugin</artifactId>
...
  <!-- Bundle-SymbolicName được generate từ -->
<Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
  <!-- → mysite.core, không phải com.mysite.core -->
</plugin>

Sau khi fix
bashmvn clean install -PautoInstallBundle
Vào configMgr kiểm tra lại mapping đã là mysite.core:mysite-read-service=[mysite-service-user] chưa, rồi call API lại.