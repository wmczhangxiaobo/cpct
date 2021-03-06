package com.zjtelcom.cpct.common;

import com.github.pagehelper.PageInfo;

import java.io.Serializable;

/**
 * 分页
 */
public class Page  implements Serializable {

    /**
     * 页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 数据总数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPage;

    public Page() {
    }

    public Page(Integer page, Integer pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    public Page(Integer page, Integer pageSize, Long total, Integer totalPage) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPage = totalPage;
    }

    public Page(PageInfo pageInfo) {
        this.page = pageInfo.getPageNum();
        this.pageSize = pageInfo.getPageSize();
        this.total = pageInfo.getTotal();
        this.totalPage = pageInfo.getPages();
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }
}
