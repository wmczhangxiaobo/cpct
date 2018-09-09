package com.zjtelcom.cpct.dto.channel;

import java.io.Serializable;
import java.util.List;

public class LabelCatalogTree implements Serializable {
    private Long id;
    private String name;
    private List<CatalogTreeTwo> children;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CatalogTreeTwo> getChildren() {
        return children;
    }

    public void setChildren(List<CatalogTreeTwo> children) {
        this.children = children;
    }
}
