package com.task07.pojo;

import java.util.List;
import java.util.UUID;

public class S3File {
    private List<UUID> uuiDlist;
    public S3File(List<UUID> uuiDlist) {
        this.uuiDlist = uuiDlist;
    }


    public List<UUID> getUuiDlist() {
        return uuiDlist;
    }

    public void setUuiDlist(List<UUID> uuiDlist) {
        this.uuiDlist = uuiDlist;
    }
}
