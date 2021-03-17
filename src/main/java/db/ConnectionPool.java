package db;

import config.ScriptRunner;
import exception.SystemMalfunctionException;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionPool {
    private static ConnectionPool instance;
    private BlockingQueue<Connection> connections;
    private static final int MAX_CONNECTIONS = 1;
    private static final String NAME_OF_PROPERTIES = "config.properties";
    private static final String PATH_TO_TABLE = "src/main/resources/createTables";

    public ConnectionPool() throws SystemMalfunctionException {
        connections = new LinkedBlockingQueue<>(MAX_CONNECTIONS);

        try {
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                connections.offer(createConnection());
            }
        } catch (SQLException | IOException e) {
            String msg = String.format("Unable to instantiate ConnectionPool (%s)", e.getMessage());
            throw new SystemMalfunctionException(msg);
        }
    }

    public static ConnectionPool getInstance() throws SystemMalfunctionException {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SystemMalfunctionException {
        try {
            return connections.take();
        } catch (InterruptedException e) {
            String msg = String.format("Unable to get Connection! (%s) ", e.getMessage());
            throw new SystemMalfunctionException(msg);
        }
    }

    private Connection createConnection() throws IOException, SQLException {
        Properties properties = new Properties();
        properties.load(ReadProperties());

        String db_url = properties.getProperty("DB_URL");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");
        System.out.println("Table creation...");

        Connection connection = DriverManager.getConnection(db_url, user, password);
        executeTablesSQL(connection);
        return connection;
    }

    private void executeTablesSQL(Connection connection) throws SQLException, IOException {
        Statement statement = connection.createStatement();
        String query = "";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(PATH_TO_TABLE));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            sb.append(line);
        }
        query = sb.toString();
        statement.executeUpdate(query);
    }

    private InputStream ReadProperties() {
        ClassLoader loader = getClass().getClassLoader();
        return loader.getResourceAsStream(NAME_OF_PROPERTIES);
    }

}
