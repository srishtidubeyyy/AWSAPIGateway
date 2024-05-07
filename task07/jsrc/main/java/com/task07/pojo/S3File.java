package com.task07.pojo;

import java.util.List;
import java.util.UUID;

public class S3File {
    private List<UUID> ids;

    public S3File(List<UUID> ids) {
        this.ids = ids;
    }

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }
}
