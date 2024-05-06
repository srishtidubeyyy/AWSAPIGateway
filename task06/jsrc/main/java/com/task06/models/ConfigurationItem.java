
package com.task06.models;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
@DynamoDBTable(tableName = "Configuration")
public class ConfigurationItem {
    private String key;
    private int value;
    @DynamoDBHashKey(attributeName = "Key")
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    @DynamoDBAttribute(attributeName = "Value")
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
}
