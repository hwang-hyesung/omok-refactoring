package org.omok.newomok.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

/*
ConnectionUtil.INSTANCE.getConnection()을 통해 접근할 수 있도록 구성한다.
 */
@Log4j2
public enum ConnectionUtil {
    INSTANCE;

    private HikariDataSource ds; //사용할 method는 여기 넣어두기

    ConnectionUtil(){ //생성자 안에 구성
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.mariadb.jdbc.Driver"); //driverSetting
        Properties props = new Properties();

        try(InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")){
            props.load(input); //가져온 프로퍼티 load한다.
            String url = props.getProperty("db.url"); //getProperty를 이용해서 props의 정보를 가져온다.
            String user = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            //HikariConfig에 설정 정보를 담는다.
            config.setPassword(password);
            config.setJdbcUrl(url);
            config.setUsername(user);

            //이외에 캐싱 정보등 넣기 시작..
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            ds = new HikariDataSource(config); //HikariDataSource(HikariConfig configuration)이므로 config 값 넣어주기
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws Exception{
        return ds.getConnection();
    }
}
