package com.zzs.TJBmatch.domain;

import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

/**
 * @author zzs
 * @date 2021年06月30日 19:13
 */
public class UpdateLogObjBatch {

    private DatabaseClient dbClient;
    private ServerRequest request;
    private String opcode;
    private String[] sqls;
    private String sMark;
    private Map<String,Object> sMapParams;

    public UpdateLogObjBatch(DatabaseClient dbClient, ServerRequest request, String opcode, String[] sqls, String sMark, Map<String,Object> sMapParams) {
        this.dbClient = dbClient;
        this.request = request;
        this.opcode = opcode;
        this.sqls = sqls;
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

    public String[] getSqls() {
        return sqls;
    }

    public String getsMark() {
        return sMark;
    }

    public Map<String, Object> getsMapParams() { return sMapParams; }
}
