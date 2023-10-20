package com.zzs.TJBmatch.domain;

import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

public class UpdateLogObj {
    private DatabaseClient dbClient;
    private ServerRequest request;
    private String opcode;
    private String sqltype;
    private String sTables;
    private String sMark;
    private Map<String,Object> sMapParams;

    public UpdateLogObj(DatabaseClient dbClient, ServerRequest request, String opcode, String sqltype, String sTables, String sMark, Map<String,Object> sMapParams) {
        this.dbClient = dbClient;
        this.request = request;
        this.opcode = opcode;
        this.sqltype = sqltype;
        this.sTables = sTables;
        this.sMark = sMark;
        this.sMapParams = sMapParams;
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

    public String getSqltype() { return sqltype; }

    public Map<String, Object> getsMapParams() { return sMapParams; }
}
