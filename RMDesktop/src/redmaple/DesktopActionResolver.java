package redmaple;

import redmaple.sql.ActionResolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DesktopActionResolver implements ActionResolver {

    @Override
    public Connection getConnection() {
            String url = "jdbc:sqlite:mapcache.db";
            try {
                    Class.forName("org.sqlite.JDBC");
                    return DriverManager.getConnection(url);
            } catch (ClassNotFoundException e) {
                    e.printStackTrace();
            } catch (SQLException e) {
                    e.printStackTrace();
            }
            return null;
    }

}