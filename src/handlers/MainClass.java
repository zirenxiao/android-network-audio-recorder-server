package handlers;
import connection.Connection;

public class MainClass {

	private static Connection c;
    public static void main(String[] args) throws Exception {
        
    	c = new Connection(new ProtocolHandler(), 8080);
    	c.start();
    	

    }
    
    public static Connection getConnection() {
    	return c;
    }


}
