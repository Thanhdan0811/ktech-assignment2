package helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private String url;
    private String user;
    private String pass;
    private Connection connect = null;

    public DB() {
        url = "jdbc:oracle:thin:@localhost:1521:orcl";
        user = "C##danontap";
        pass = "123456";
    }

    public Connection openConnect() throws SQLException {
        connect = DriverManager.getConnection(url, user, pass);
        return connect;
    }

    public void closeConnect() throws SQLException {
        if (connect != null)connect.close();
    }

    public Connection getConnect() {
        return connect;
    }

}
