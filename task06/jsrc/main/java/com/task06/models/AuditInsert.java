package com.task06.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AuditInsert {
    private String id;
    private String itemKey;
    private LocalDateTime modificationTime;
    private String updatedAttribute;
    private Object newValue;

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute
    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    @DynamoDBAttribute
    public LocalDateTime getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(LocalDateTime modificationTime) {
        this.modificationTime = modificationTime;
    }

    @DynamoDBAttribute
    public String getUpdatedAttribute() {
        return updatedAttribute;
    }

    public void setUpdatedAttribute(String updatedAttribute) {
        this.updatedAttribute = updatedAttribute;
    }

    @DynamoDBAttribute
    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    public Map<String, AttributeValue> toMap() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("id", new AttributeValue().withS(id));
        map.put("itemKey", new AttributeValue().withS(itemKey));
        map.put("modificationTime", new AttributeValue().withS(modificationTime.toString())); // Convert LocalDateTime to String
        map.put("updatedAttribute", new AttributeValue().withS(updatedAttribute));
        map.put("newValue", new AttributeValue().withN(String.valueOf(newValue)));
        return map;
    }
}
