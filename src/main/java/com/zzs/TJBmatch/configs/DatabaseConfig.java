package com.zzs.TJBmatch.configs;

import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class DatabaseConfig extends AbstractR2dbcConfiguration{

    @Value("${spring.data.mysql.port}")
    private int mport;
    @Value("${spring.data.mysql.database}")
    private String mdatabase;
    @Value("${spring.data.mysql.username}")
    private String musername;
    @Value("${spring.data.mysql.password}")
    private String mpassword;
    @Value("${spring.data.mysql.host}")
    private String mhost;

    @Bean(name = "R2DBCConn")
    @Override
    public ConnectionFactory connectionFactory() {
        MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
        .host(mhost)
        .port(mport)
        .database(mdatabase)
        .username(musername)
        .password(mpassword)
        .build();
        return MySqlConnectionFactory.from(configuration);
    }

    @Bean(name="connPool")
    public ConnectionPool getConnectionPool(){
//        ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
//                .option(DRIVER, "pool")
//                .option(PROTOCOL, "mysql") // driver identifier, PROTOCOL is delegated as DRIVER by the pool.
//                .option(HOST, mhost)
//                .option(PORT, mport)
//                .option(USER, musername)
//                .option(PASSWORD, mpassword)
//                .option(DATABASE, mdatabase)
//                .build());

        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory())
                .maxIdleTime( Duration.ofMillis(1000))
                .maxSize(15)
                .initialSize(10)
                .build();

        ConnectionPool pool = new ConnectionPool(configuration);
        return pool;
    }


    public Mono<Connection> getConnection(@Qualifier("connPool") ConnectionPool pool){
        return pool.create();
    }

    public Mono<Void> closeConnection(Connection connection) {
        return Mono.from(connection.close()); // released the connection back to the pool
    }

    public void closePool(ConnectionPool pool){
        pool.dispose();
    }

//    @Bean(name="DBClient")
//    public DatabaseClient DBClient(@Qualifier("R2DBCConn") ConnectionFactory connectionFactory){
//        return DatabaseClient.create(connectionFactory);
//    }

    @Bean(name="DBClient")
    public DatabaseClient DBClient(@Qualifier("connPool") ConnectionPool connectionPool){
        return DatabaseClient.create(connectionPool);
    }
}
