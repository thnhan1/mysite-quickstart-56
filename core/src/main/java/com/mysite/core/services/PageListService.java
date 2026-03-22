package com.mysite.core.services;

import java.util.List;

/**
 * Service đọc danh sách title các page con trực tiếp
 * Dưới 1 path cho trước trong JCR
 */
public interface PageListService {

    /**
     *
     * @param parentPath đường dẫn JCR của page cha ví dụ "/content/mysite/us/en"
     * @return List title (jcr:title) của các child page
     *          trả về empty list nếu không có hoặc lỗi
     */
    List<String> getChildPageTitles(String parentPath);
}
