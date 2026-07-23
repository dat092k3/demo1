package com.example.demo.module.core.dto.message;

import java.io.Serializable;

public class SearchIndexMessage implements Serializable {
    private String entityType; // "BOOK" or "CHAPTER"
    private Long entityId;
    private String action; // "CREATE", "UPDATE", "DELETE"

    public SearchIndexMessage() {
    }

    public SearchIndexMessage(String entityType, Long entityId, String action) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
