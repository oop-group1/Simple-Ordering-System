import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 *
 * @author Generic
 */
public class SQLConn {
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    
    private Connection connection;
    
    
    public SQLConn(String host, int port, String database, String username, String password){
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        
    };
    /*For Default*/
    public SQLConn(String host, String database, String username, String password){
            this(host, 3306, database, username, password);
    };
    
    
    // Connection
    
    public void connect() throws SQLException, ClassNotFoundException {
        if(isConnected()){
            System.out.println("Already Connected");
            return;
        }
        
        Class.forName(JDBC_DRIVER);
        
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", 
                host, port, database);
        
        connection = DriverManager.getConnection(url, username, password);
        System.out.println("Connected to: " + database);
        
        
    };
    
    
    // Checks whether connection is open and valid
    
    
    public void close() throws SQLException{
        if (isConnected()) {
            connection.close();
            connection = null;
            System.out.println("Database connection closed.");
        }
    };
    
    // Check if connected
    
    public boolean isConnected(){
        try{
            return connection != null && !connection.isClosed();
        }catch (SQLException e){
            return false;
        }
    };
       
    // used for establishing connection without auto-commit
    public void beginTransaction() throws SQLException{
        ensureConnection();
        connection.setAutoCommit(false);
        
    }
    
    // used for committing current transaction and for re-enabling auto-commut
    public void commit() throws SQLException {
        ensureConnection();
        connection.commit();
        connection.setAutoCommit(true);
        
    }
    // used for rollback previous queries when failure occurs
    public void rollback() throws SQLException {
        if (isConnected()) {
            connection.rollback();
            connection.setAutoCommit(true);
        }
    }
    // ------------------HELPERS-----------------------------------------------------
    
    // Set Parameters for SQL Query
    
    private void setParameters(PreparedStatement stmt, Object... params ) throws SQLException{
        for (int i = 0; i < params.length; i++){
            stmt.setObject(i+1, params[i]);
        }
        
    };
    
    // Creates statement with parameters
    private PreparedStatement buildStatement(String sql, Object... params) throws SQLException{
        PreparedStatement statement = connection.prepareStatement(sql);
        setParameters(statement, params);
        return statement;
       
    };
    
    // Helper function to verify connection
    private void ensureConnection() throws SQLException {
        if (!isConnected()) {
            throw new SQLException("No active database connection. Call connect() first.");
           
        }
        return;
    }
    
    // used for Excecuting GET or SELECT queries
    public ResultSet executeQuery(String sql, Object... params) throws SQLException{
        ensureConnection();
        PreparedStatement stmt = buildStatement(sql, params);
        return stmt.executeQuery();
    }
    
    // used for Executing POST, PUT or DELETE or UPDATE and DELETE queries
    public int executeUpdateDelete(String sql, Object... params) throws SQLException{
        ensureConnection();
        try (PreparedStatement stmt = buildStatement(sql, params)) {
            return stmt.executeUpdate();
        }
    }
    
    public long executeInsert(String sql, Object... params) throws SQLException {
        ensureConnection();
        try (PreparedStatement stmt =
                     connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            setParameters(stmt, params);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }
        return -1;
    }
    
}


