package redmaple.sql;

import java.sql.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 23.3.2013
 * Time: 13:32
 * To change this template use File | Settings | File Templates.
 */
public interface ActionResolver {
    public Connection getConnection();
}
