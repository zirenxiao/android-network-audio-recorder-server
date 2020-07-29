package connection;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import ssl.SslContextFactory;

public class Connection extends Thread{
	
	private int port;
	private IoHandlerAdapter handler;
	private boolean sslOn;
	private SocketAcceptor acceptor = null;
	
	public Connection(IoHandlerAdapter handler, int port) throws Exception {
		this.port = port;
		this.handler = handler;
		this.sslOn = false;
	}
	
	public Connection(IoHandlerAdapter handler, int port, String sslPath, char[] sslPw) throws Exception {
		this(handler, port);
		this.sslOn = true;
		SslContextFactory.setKeystore(sslPath);
		SslContextFactory.setKeystorePassword(sslPw);
	}
	
	@Override
	public void run() {
		acceptor = new NioSocketAcceptor();
//		acceptor.getFilterChain().addLast( "logger", new LoggingFilter() );
//		TextLineCodecFactory tlcf = new TextLineCodecFactory( Charset.forName( "UTF-8" ));
//        acceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter(tlcf));
        acceptor.setReuseAddress(true);
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        
        // Add SSL filter if SSL is enabled.
        if (sslOn) {
            try {
				addSSLSupport(chain);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}
        }

        // Bind
        acceptor.setHandler(handler);//new ProtocolHandler()
        try {
			acceptor.bind(new InetSocketAddress(port));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}

        System.out.println("Listening on port " + port);
	}
	
    private void addSSLSupport(DefaultIoFilterChainBuilder chain)
            throws Exception {
        SslFilter sslFilter = new SslFilter(SslContextFactory
                .getInstance(true));
        chain.addLast("sslFilter", sslFilter);
        System.out.println("SSL ON");
    }
    
    
    public void broadcast(Object s, Long currentID) {
    	acceptor.getManagedSessions().forEach((id, session) -> {
    		if (id != currentID) {
    			session.write(((IoBuffer) s).duplicate());
    		}
    	});
    }
}
