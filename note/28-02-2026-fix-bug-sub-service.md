## Debug Checklist — LoginException with SubService User

`{bundleSymbolicName}:{subServiceName}=[{systemUser}].`
- subServiceName: ten mapping, dung trong code de getServiceUserID
- systemUser: ten user trong JCR, co node type rep:SystemUser
- tim thay trong: /home/users/system/mysiteServiceUser 
- ACL: phai co quyen jcr:read tren /content/mysite
- OSGi Config: phai co 2 config, 1 cho ServiceUserMapperImpl, 1 cho RepositoryInitializer
- Log: phai co log tao user, set ACL, install config
- Bundle: phai co bundle chua code goi getServiceUserID, va symbolic name phai khop voi mapping
- Mapping: phai co mapping trong ServiceUserMapperImpl, va mapping phai khop voi symbolic name cua bundle
- File config: phai co file config dung ten va dung noi dung, deploy vao AEM
- Error: neu co loi, se gap LoginException khi goi getServiceUserID, va log se co thong tin ve loi do.
- Debug: neu gap loi, can check lai tung buoc tren, tu bundle, mapping, user, ACL, config, log.
- Fix: neu gap loi, fix theo tung buoc, tu bundle, mapping, user, ACL, config, log.

### 1. Verify Bundle Symbolic Name

```
http://localhost:4502/system/console/bundles
```

Tìm bundle `mysite.core` → cột **Symbolic Name** — phải khớp chính xác với vế trái trong mapping:

```json
"com.mysite.core:mysite-service-user=[mysiteServiceUser]"
```

### 2. Verify Service User Mapping đã load

```
http://localhost:4502/system/console/serviceusers
```

Tìm dòng có `mysite-service-user` — phải thấy:

```
com.mysite.core:mysite-service-user → mysiteServiceUser
```

Nếu không thấy → file config chưa deploy hoặc sai tên file.

### 3. Verify System User tồn tại trong JCR

```
http://localhost:4502/crx/de/index.jsp#/home/users/system
```

Tìm node `mysiteServiceUser` — phải có node type `rep:SystemUser`.

### 4. Verify ACL đã được set

```
http://localhost:4502/useradmin
```

Tìm `mysiteServiceUser` → tab **Permissions** → phải thấy `jcr:read` trên `/content/mysite`.

### 5. Verify OSGi Config files đã install

```
http://localhost:4502/system/console/configMgr
```

Tìm 2 config: `Apache Sling Service User Mapper Service Amendment` và `Apache Sling Repository Initializer` factory `mysite`.

### 6. Check error.log

```bash
tail -f crx-quickstart/logs/error.log | grep -i "repoinit\|serviceuser\|mysiteServiceUser"
```

Các log cần thấy:

```
Creating service user mysiteServiceUser          ✅
Adding ACL 'allow' entry '[jcr:read]'...         ✅
Installed configuration ServiceUserMapperImpl... ✅
```

---

## File config liên quan

**ServiceUserMapperImpl** — `ui.config/.../osgiconfig/config/org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-mysite.cfg.json`:

```json
{
  "user.mapping": [
    "com.mysite.core:mysite-service-user=[mysiteServiceUser]"
  ]
}
```

**RepositoryInitializer** — `ui.config/.../osgiconfig/config/org.apache.sling.jcr.repoinit.RepositoryInitializer-mysite.cfg.json`:

```json
{
  "scripts": [
    "create service user mysiteServiceUser\n\nset principal ACL for mysiteServiceUser\n    allow jcr:read on /content/mysite\nend"
  ]
}
```