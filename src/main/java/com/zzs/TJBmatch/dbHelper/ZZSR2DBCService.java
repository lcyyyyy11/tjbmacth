package com.zzs.TJBmatch.dbHelper;

import com.sun.istack.internal.NotNull;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/************************************************************
 * zzs 2020-05
 * All Methods are reactive streams access to SQL databases based on R2DBC (Reactive Relational Database Connectivity).
 * They are non-blocking SPI for database, and provide asynchronous mechanism.
 ************************************************************/

@Service
public abstract class ZZSR2DBCService {

//    private static Logger log = LoggerFactory.getLogger( ZZSR2DBCService.class );

    /**
     * for simple update sql access
     * @param sql
     * @return
     */
    public static Mono<Integer> insertUpdateDelete(DatabaseClient client, String sql){
        return client.execute( sql )
                .fetch()
                .rowsUpdated();
    }

    /**
     * this works for sqlserver tested
     * @param sql
     * @param sId
     * @return
     */
    public static Mono<Object> getInsertKey(DatabaseClient client, String sql, String sId){
        return client.execute( sql )
                 .fetch()
                 .rowsUpdated()
                 .then( client.select()
                        .from(findTableFromSql(sql)[0])
                        .project( sId )
                        .orderBy( Sort.Order.desc( sId ) )
                        .map((row, rowMetadata) -> row.get(sId, Object.class))
                        .first()
                        .switchIfEmpty( Mono.just( "" ) ));
    }

    public static Mono<String> getInsertIdentityKeyForMSSQL(DatabaseClient client, String insertSql){
        insertSql = insertSql.trim().endsWith( ";" )?insertSql+"Select SCOPE_IDENTITY();":insertSql+";Select SCOPE_IDENTITY();";
        return getSingleValue(client, insertSql);
    }

    public static Mono<String> getSingleValue(DatabaseClient client, String sql){
        return client.execute( sql )
                .map((row, rowMetadata) -> row.get(0, String.class))
                .first()
                .switchIfEmpty( Mono.just( "" ) );
    }

    public static Mono<Map<String,Object>> getMapDataMono(DatabaseClient client, String sql){
        return client
                .execute( sql )
                .fetch()
                .first();
    }


    public static Flux<Map<String,Object>> getMapDataFlux(DatabaseClient client, String sql){
        return client
                .execute( sql )
                .fetch()
                .all()
                .switchIfEmpty( Mono.just( new HashMap<>(  ) ) );
    }

    public static Mono<JSONObject> getJSONObjMono(DatabaseClient client, String sql){
        return client
                .execute( sql )
                .fetch()
                .first()
                .map( map -> new JSONObject( map ) )
                .switchIfEmpty( Mono.just(new JSONObject(  )));
    }


    public static Flux<JSONObject> getJSONObjFlux(DatabaseClient client, String sql){
        return client
                .execute( sql )
                .fetch()
                .all()
                .map( map -> new JSONObject( map ) )
                .switchIfEmpty( Mono.just(new JSONObject(  )));
    }

    private static io.r2dbc.spi.Batch builderBatch(io.r2dbc.spi.Batch batch,String[] sqls){
         Arrays.stream( sqls )
                .forEach( sql -> batch.add( sql ) );
         return batch;
    }

    public static Mono<Result> insertUpdateDeleteForBatch(ConnectionFactory conn, String[] sqls){
        return Mono.from( conn.create())
                .flatMap( c -> Mono.from(builderBatch(c.createBatch(),sqls).execute())
                            .doFinally( cc -> c.close()));
    }


    public static Mono<Result> insertUpdateDeleteForBatch_ori(ConnectionFactory conn, String[] sqls){
        return Mono.from( conn.create())
                .flatMap( c -> Mono.from( c.createBatch()
                        .add( sqls.length > 0 ? sqls[0] : "")
                        .add( sqls.length > 1 ? sqls[1] : "")
                        .add( sqls.length > 2 ? sqls[2] : "")
                        .add( sqls.length > 3 ? sqls[3] : "")
                        .add( sqls.length > 4 ? sqls[4] : "")
                        .add( sqls.length > 5 ? sqls[5] : "")
                        .add( sqls.length > 6 ? sqls[6] : "")
                        .add( sqls.length > 7 ? sqls[7] : "")
                        .add( sqls.length > 8 ? sqls[8] : "")
                        .add( sqls.length > 9 ? sqls[9] : "")
                        .execute())
                        .doFinally( (st) -> c.close()
                ));
    }

    public static Mono<Integer> insertUpdateDeleteForBatch(DatabaseClient client, String[] sqls){
        // modify by zj add condition
        String linkSql = Stream.of(sqls)
                .filter(x -> !x.equals(""))
                .collect( Collectors.joining(";"));

        return client.execute( linkSql )
                .fetch()
                .rowsUpdated();
    }

    public static Mono<Void> insertUpdateDeleteForTrans(ConnectionFactory conn, String[] sqls){
        int d = sqls.length;
        if (d < 1) return null;

        ReactiveTransactionManager tm = new R2dbcTransactionManager(conn);
        TransactionalOperator rxtx = TransactionalOperator.create(tm);
        DatabaseClient client = DatabaseClient.create(conn);

        Mono<Void> ges = client.execute( sqls[0] )
                .fetch()
                .rowsUpdated()
//                .then( client.execute(sqls[1]).then());
                .then();
        if(d > 1){
            for (int i = 1; i < d; i++) {
                ges = ges.then(
                        client.execute( sqls[i] )
                                .then()
                );
            }
        }

        ges.as(rxtx::transactional);
        return ges;
    }

    public static Mono<List<Map<String, Object>>> getListMap(DatabaseClient client, String sql){
        return client.execute( sql )
                .fetch()
                .all()
                .collect( Collectors.toList() );
    }

    public static Mono<JSONArray> getJsonArray(DatabaseClient client, String sql){
        return client.execute( sql )
                .fetch()
                .all()
                .collect( Collectors.toList() )
                .map( li ->  new JSONArray(li))
                .switchIfEmpty( Mono.just( new JSONArray(  ) ) );
    }

    /**
     * 注意:2020-05
     * sql中只能有一个参数，便是reader填充或更新字段
     * @param client
     * @param sql
     * @param reader
     * @return
     */

    public static Mono<Integer> insertUpdateCLOB(DatabaseClient client, String sql, Reader reader){
        return client.execute( sql )
                .bind( 0,reader )
                .fetch()
                .rowsUpdated();
    }

    public static Mono<Reader> getCLOBReader(DatabaseClient client, @NotNull String sql) {
        return client.execute( sql )
                .map( (row, rowMateData) -> row.get( 0, Reader.class ) )
                .first()
                .switchIfEmpty( Mono.just(new Reader() {
                    @Override
                    public int read(char[] cbuf, int off, int len) throws IOException {
                        return 0;
                    }
                    @Override
                    public void close() throws IOException {

                    }
                } ));
    }

    /**
     * 注意:2020-05
     * sql中只能有一个参数，便是inputStream填充或更新字段
     * @param client
     * @param sql
     * @param ips
     * @return
     */
    public static Mono<Integer> insertUpdateBLOB(DatabaseClient client, String sql, InputStream ips){
        return client.execute( sql )
                .bind( 0,ips )
                .fetch()
                .rowsUpdated();
    }

    public static Mono<InputStream> getBLOBStream(DatabaseClient client, @NotNull String sql) {
        return client.execute( sql )
                .map( (row, rowMateData) -> row.get( 0, InputStream.class ) )
                .first()
                .switchIfEmpty( Mono.just(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }
                } ));
    }


    /********************************************************************
     * zzs 2020-05
     * for parameter match method
     * @param paraMap
     * @return
     *********************************************************************/

    public static Mono<Integer> insertUpdateDelete(DatabaseClient client, String sql, Map<String,Object> paraMap){
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .fetch()
                .rowsUpdated();
    }


    public static Mono<String> getInsertIdentityKeyForMSSQL(DatabaseClient client, String insertSql, Map<String,Object> paraMap){
//        insertSql = insertSql.trim().endsWith( ";" )?insertSql+"Select SCOPE_IDENTITY();":insertSql+";Select SCOPE_IDENTITY();";
        insertSql = insertSql.trim().endsWith( ";" )?insertSql+"Select cast(SCOPE_IDENTITY() as varchar(20));":insertSql+";Select cast(SCOPE_IDENTITY() as varchar(20));";
        return getSingleValue(client, insertSql, paraMap);
    }

    public static Mono<Object> getInsertKey(DatabaseClient client, String sql, String sId, Map<String,Object> paraMap){
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .fetch()
                .rowsUpdated()
                .then( client.select()
                        .from(findTableFromSql(sql)[0])
                        .project( sId )
                        .orderBy( Sort.Order.desc( sId ) )
                        .map((row, rowMetadata) -> row.get(sId, Object.class))
                        .first() );
    }


    public static Mono<String> getSingleValue(DatabaseClient client, String sql, Map<String,Object> paraMap){
        return  makeBindGenericExecuteSpec(client,sql,paraMap)
                .map((row, rowMetadata) -> row.get(0, String.class))
                .first()
                .switchIfEmpty( Mono.just( "" ) );
    }


    public static Mono<Map<String,Object>> getMapDataMono(DatabaseClient client, String sql, Map<String,Object> paraMap){
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .fetch()
                .first();
    }


    public static Flux<Map<String,Object>> getMapDataFlux(DatabaseClient client, String sql, Map<String,Object> paraMap){
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .fetch()
                .all()
                .switchIfEmpty( Mono.just(  new HashMap<String,Object>()) );
    }

    public static Mono<JSONObject> getJSONObjMono(DatabaseClient client, String sql, Map<String,Object> paraMap){
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .fetch()
                .first()
                .map( map -> new JSONObject( map ) )
                .switchIfEmpty( Mono.just(  new JSONObject(  )) );
    }

    public static Flux<JSONObject> getJSONObjFlux(DatabaseClient client, String sql, Map<String,Object> paraMap){
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .fetch()
                .all()
                .map( map -> new JSONObject( map ) )
                .switchIfEmpty( Mono.just(  new JSONObject(  )) );
    }

    public static Mono<Integer> insertUpdateDeleteForBatch(DatabaseClient client, String[] sqls, Map<String,Object> paraMap){
        String linkSql = Stream.of(sqls)
                .collect( Collectors.joining(";"));

        return makeBindGenericExecuteSpec(client,linkSql,paraMap)
                .fetch()
                .rowsUpdated();
    }

    public static Mono<Void> insertUpdateDeleteForTrans(ConnectionFactory conn, String[] sqls, Map<String,Object> paraMap){
        int d = sqls.length;
        if (d < 1) return null;
//        log.info("到达");
        ReactiveTransactionManager tm = new R2dbcTransactionManager(conn);
        TransactionalOperator rxtx = TransactionalOperator.create(tm);
        DatabaseClient client = DatabaseClient.create(conn);

        Mono<Void> ges = makeBindGenericExecuteSpec(client,sqls[0],paraMap)
                .fetch()
                .rowsUpdated()
                .then();
//                .then( makeBindGenericExecuteSpec(client,sqls[1],paraMap).then());

        if (d > 1)
            for (int i = 1; i < d; i++) {
                ges = ges.then(
                        makeBindGenericExecuteSpec(client,sqls[i],paraMap)
                                .then()
                );
            }
        ges.as(rxtx::transactional);
//        log.info("离开");
        return ges;
    }


    public static Mono<List<Map<String, Object>>> getListMap(DatabaseClient client, String sql, Map<String,Object> paraMap){
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .fetch()
                .all()
                .collect( Collectors.toList());
    }

    public static Mono<JSONArray> getJsonArray(DatabaseClient client, String sql, Map<String,Object> paraMap){
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .fetch()
                .all()
                .collect( Collectors.toList() )
                .map( li ->  {
                  //  System.out.println("查询结果："+li);
                    JSONArray jsonArray = new JSONArray(li);
//                    System.out.println("查看结果，字段："+jsonArray);
                    return  jsonArray;
                });
    }

    /**
     * 注意: zzs 2020-05
     * Clob参数必须单独写，且在sql中必须是第一个参数，参数Map中不能含这个clob参数
     * @param client
     * @param sql
     * @param reader
     * @param otherParaMap
     * @return
     */
    public static Mono<Integer> insertUpdateCLOB(DatabaseClient client, String sql, Reader reader, Map<String,Object> otherParaMap){
        return makeBindGenericExecuteSpec(client,sql,otherParaMap)
                .bind( 0,reader )
                .fetch()
                .rowsUpdated();
    }

    public static Mono<Reader> getCLOBReader(DatabaseClient client, @NotNull String sql, Map<String,Object> paraMap) {
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .map( (row, rowMateData) -> row.get( 0, Reader.class ) )
                .first()
                .switchIfEmpty( Mono.just(new Reader() {
                    @Override
                    public int read(char[] cbuf, int off, int len) throws IOException {
                        return 0;
                    }
                    @Override
                    public void close() throws IOException {

                    }
                } ) );
    }

    /**
     * 注意: zzs 2020-05
     * Blob参数必须单独写，且在sql中必须是第一个参数，参数Map中不能含这个Blob参数
     * @param client
     * @param sql
     * @param ips
     * @param otherParaMap
     * @return
     */
    public static Mono<Integer> insertUpdateBLOB(DatabaseClient client, String sql, InputStream ips, Map<String,Object> otherParaMap){
        return makeBindGenericExecuteSpec(client,sql,otherParaMap)
                .bind( 0,ips )
                .fetch()
                .rowsUpdated();
    }

    public static Mono<InputStream> getBLOBStream(DatabaseClient client, @NotNull String sql, Map<String,Object> paraMap) {
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .map( (row, rowMateData) -> row.get( 0, InputStream.class ) )
                .first()
                .switchIfEmpty( Mono.just(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }
                } ) );
    }


    /********************************************************************
     * zzs 2010-05
     * assitant methods
     * @param client
     * @param sql
     * @param paraMap
     * @return
     ********************************************************************/

    public static GenericExecuteSpec makeBindGenericExecuteSpec(DatabaseClient client, String sql, Map<String,Object> paraMap){
        GenericExecuteSpec ges = client.execute( sql );
        List<String> lVarables = new ArrayList<>(  );
        Matcher matcher = Pattern.compile("[@:]\\s*(\\w+)").matcher(sql);
        while (matcher.find()) {
            lVarables.add(matcher.group(1));
        }

        lVarables.stream()
                .forEach( lv -> {
                    if (!paraMap.containsKey(lv))
                        paraMap.putIfAbsent( lv,null );
                });


//        for (int i = 0; i < lVarables.size(); i++) {
//            if (!paraMap.containsKey(lVarables.get( i )))
//                paraMap.putIfAbsent( lVarables.get( i ),null );
//        }

        for(String key:paraMap.keySet()) {
            if (lVarables.contains( key ))
                if (null == paraMap.get( key ))
                    ges = ges.bindNull( key.trim(),Object.class );
                else
                    ges = ges.bind( key.trim(), paraMap.get( key ) );
        }

       return ges;
    }

    private static String[] findTableFromSql(String sql){
        List<String> ltables =new ArrayList<String>();
//        sql = sql.toUpperCase();
        ltables = Arrays.stream(sql.split("FROM|JOIN|INTO|UPDATE"))
                .map(s1-> s1.trim().split(" ")[0].trim())
                .filter(s2 -> !s2.equals("SELECT") && !s2.equals("") && !s2.equals("INSERT") && !s2.equals("DELETE"))
                .distinct()
                .collect( Collectors.toList());
        return ltables.toArray(new String[0]);
    }

    ///
    // zzs Common Application Utilities
    ///

    public static Mono<String> getSelectLinkStr(DatabaseClient client, String sql) {
        return client.execute( sql )
                .map((row, rowMetadata) -> row.get(0, Object.class).toString().trim()+","+row.get(1, Object.class).toString().trim())
                .all()
                .collect( Collectors.joining( ";" ) )
                .switchIfEmpty( Mono.just( "" ) );
    }

    public static Mono<String> getSelectLinkStr(DatabaseClient client, @NotNull String sql, Map<String,Object> paraMap) {
        return makeBindGenericExecuteSpec(client,sql,paraMap)
                .map((row, rowMetadata) -> row.get(0, Object.class).toString().trim()+","+row.get(1, Object.class).toString().trim())
                .all()
                .collect( Collectors.joining( ";" ) )
                .switchIfEmpty( Mono.just( "" ) );
    }

    public static Mono<Integer> updateForTrans(ConnectionFactory conn, String[] sqls, Map<String,Object> paraMap){
        if (sqls.length < 1) return null;

        ReactiveTransactionManager tm = new R2dbcTransactionManager(conn);
        TransactionalOperator rxtx = TransactionalOperator.create(tm);
        DatabaseClient client = DatabaseClient.create(conn);

        Mono<Integer> ges = makeBindGenericExecuteSpec(client,sqls[0],paraMap)
                .fetch()
                .rowsUpdated();
        //.then( makeBindGenericExecuteSpec(client,sqls[1],paraMap).then());

        if (sqls.length>1)
            for (int i = 1; i < sqls.length; i++) {
                ges = ges.then(
                        makeBindGenericExecuteSpec(client,sqls[i],paraMap)
                                .fetch()
                                .rowsUpdated()
                );
            }
        ges.as(rxtx::transactional);
        return ges;
    }


    public static Mono<Integer> updateForTrans(ConnectionFactory conn, String[] sqls){
        if (sqls.length < 1) return null;

        ReactiveTransactionManager tm = new R2dbcTransactionManager(conn);
        TransactionalOperator rxtx = TransactionalOperator.create(tm);
        DatabaseClient client = DatabaseClient.create(conn);

        Mono<Integer> ges = client.execute( sqls[0] )
                .fetch()
                .rowsUpdated();

        if (sqls.length>1)
            for (int i = 1; i < sqls.length; i++) {
                ges = ges.then(
                        client.execute( sqls[i] )
                                .fetch()
                                .rowsUpdated()
                );
            }
        ges.as(rxtx::transactional);

        return ges;
    }


}
