package com.task06.models;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@DynamoDBTable(tableName = "Audit")
public class AuditEntry {
    private String id;
    private String itemKey;
    private LocalDateTime modificationTime;
    private String updatedAttribute;
    private int oldValue;
    private int newValue;
    @DynamoDBHashKey(attributeName = "Id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    @DynamoDBAttribute(attributeName = "ItemKey")
    public String getItemKey() {
        return itemKey;
    }
    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }
    @DynamoDBAttribute(attributeName = "ModificationTime")
    public LocalDateTime getModificationTime() {
        return modificationTime;
    }
    public void setModificationTime(LocalDateTime modificationTime) {
        this.modificationTime = modificationTime;
    }
    @DynamoDBAttribute(attributeName = "UpdatedAttribute")
    public String getUpdatedAttribute() {
        return updatedAttribute;
    }
    public void setUpdatedAttribute(String updatedAttribute) {
        this.updatedAttribute = updatedAttribute;
    }
    @DynamoDBAttribute(attributeName = "OldValue")
    public int getOldValue() {
        return oldValue;
    }
    public void setOldValue(int oldValue) {
        this.oldValue = oldValue;
    }
    @DynamoDBAttribute(attributeName = "NewValue")
    public int getNewValue() {
        return newValue;
    }
    public void setNewValue(int newValue) {
        this.newValue = newValue;
    }
    public Map<String, AttributeValue> toMap() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("Id", new AttributeValue().withS(this.id));
        item.put("ItemKey", new AttributeValue().withS(this.itemKey));
        return item;
    }
}
