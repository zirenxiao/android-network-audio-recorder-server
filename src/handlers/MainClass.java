package handlers;

import connection.Connection;

public class MainClass {

	private static Connection c;
    public static void main(String[] args) throws Exception {
        
    	c = new Connection(new ProtocolHandler(), 53330);
    	c.start();
    	new Connection(new EchoHandler(), 53331).start();
    	
    	

    }
    
    public static Connection getConnection() {
    	return c;
    }


}
