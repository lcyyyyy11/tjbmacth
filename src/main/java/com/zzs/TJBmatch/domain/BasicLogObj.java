package com.zzs.TJBmatch.domain;

import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.web.reactive.function.server.ServerRequest;

public class BasicLogObj {

    private DatabaseClient dbClient;
    private ServerRequest request;
    private String opcode;
    private String sTables;
    private String sMark;

    public BasicLogObj(DatabaseClient dbClient, ServerRequest request, String opcode, String sTables, String sMark) {
        this.dbClient = dbClient;
        this.request = request;
        this.opcode = opcode;
        this.sTables = sTables;
        this.sMark = sMark;
    }

    public DatabaseClient getDbClient() {
        return dbClient;
    }

    public ServerRequest getRequest() {
        return request;
    }

    public String getOpcode() {
        return opcode;
    }

    public String getsTables() {
        return sTables;
    }

    public String getsMark() {
        return sMark;
    }
}
