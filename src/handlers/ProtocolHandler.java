package handlers;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.ssl.SslFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolHandler extends IoHandlerAdapter {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProtocolHandler.class);
    
    File file;
	DataOutputStream dos;
    
    @Override
    public void sessionCreated(IoSession session) {
        session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        // We're going to use SSL negotiation notification.
        session.setAttribute(SslFilter.USE_NOTIFICATION);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        LOGGER.info("CLOSED");
        try {
			dos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        File tmpOutFile = new File("D:/b.wav");
        pcmToWave(file.getAbsolutePath(), tmpOutFile.getAbsolutePath());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
    	file = new File("D:/a.pcm");
    	dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        LOGGER.info("OPENED");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) {
        LOGGER.info("*** IDLE #" + session.getIdleCount(IdleStatus.BOTH_IDLE) + " ***");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
    	LOGGER.error(cause.toString());
        session.close(true);
        
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
    	IoBuffer data = (IoBuffer) message;
    	// create a byte array to hold the bytes
    	byte[] buf = new byte[data.limit()];
    	// pull the bytes out
    	data.get(buf);
    	
//    	String str = message.toString();
//        if( str.trim().equalsIgnoreCase("quit") ) {
//            session.close();
//            return;
//        }
//    	MainClass.getConnection().broadcast(message, session.getId());
    	
    	dos.write(buf);
    	
    	
//        session.write(((IoBuffer) message).duplicate());
    	
//    	// cast message to io buffer
//    	IoBuffer data = (IoBuffer) message;
//    	// create a byte array to hold the bytes
//    	byte[] buf = new byte[data.limit()];
//    	// pull the bytes out
//    	data.get(buf);
//    	// look at the message as a string
//    	System.out.println("Message: " + new String(buf));

        
    }
    
    public void pcmToWave(String inFileName, String outFileName){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudiolen = 0;
        long longSamplRate = 48000;
        long totalDataLen = totalAudiolen+36;//由于不包括RIFF和WAV
        int channels = 2;
        long byteRate = 16*longSamplRate*channels/8;
        byte[] data = new byte[1024];
        try {
            in = new FileInputStream(inFileName);
 
            out = new FileOutputStream(outFileName);
            totalAudiolen = in.getChannel().size();
            totalDataLen = totalAudiolen+36;
            writeWaveFileHeader(out, totalAudiolen, totalDataLen, longSamplRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
                                    int channels, long byteRate) {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        try {
            out.write(header, 0, 44);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}