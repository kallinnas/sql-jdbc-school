import db.ConnectionPool;
import exception.SystemMalfunctionException;

import java.sql.Connection;

public class CheckConnection {

    public static void main(String[] args) throws SystemMalfunctionException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();


        System.out.println("Connection created!");
    }
}
