package org.kaipan.jserver.socket.protocol.websocket;

import java.util.Random;

import org.kaipan.jserver.socket.util.IntegerUtil;

public class WsUtil
{
	private static byte[] getLengthBytes(short length) 
	{
		return IntegerUtil.short2BigEndian(length);
	}
	
	private static byte[] getLengthBytes(long length) 
	{
		return IntegerUtil.long2BigEndian(length);
	}
	
	private static int getPayloadLength(int length) 
	{
		int Length;
		
		if ( length == 0x7e ) {
			Length = 0x7e;
		}
		else if ( length > 0x7f ) {
			Length = 0x7f;
		}
		else {
			Length = length;
		}
		
		return Length;
	}
	
	private static boolean isFin(byte fin) 
	{
		if ( (fin & 0x80) > 0 ) {
			return true;
		}
		
		return false;
	}
	
	private static boolean isMask(byte mask) 
	{
		if ( (mask & 0x80) > 0 ) {
			return true;
		}
		
		return false;
	}
	
	private static byte[] parseMessage(byte[] data, byte[] mask) 
	{
		for ( int i = 0; i < data.length; i++ ) {
			data[i] = (byte) (data[i] ^ mask[i % 4]);
		}
		
		return data;
	}
	
	public static WsFrame parseFrame(byte[] data, int offset, int length) 
	{
		WsFrame frame = new WsFrame();
	
		int index = offset;
		
		if ( isFin(data[index]) ) {
			frame.setFin(true);
		}
		
		int rsv1 = data[index] & 0x40;
		int rsv2 = data[index] & 0x20;
		int rsv3 = data[index] & 0x10;
		
		if ( rsv1 == 1 || rsv2 == 1 || rsv3 == 1 ) {
			throw new IllegalArgumentException("RSV must be set to 0");
		}
		
		frame.setOpcode((byte) (data[index] & 0x0f));
		
		index++;
		
		boolean mask = isMask(data[index]);
		
		long Length = data[index] & 0x7f;
		if ( Length == 0x7e ) {
			Length = IntegerUtil.bigEndian2Short(data, index);
			
			index += 2;
		}
		else if ( Length == 0x7f ) {		
			Length = IntegerUtil.bigEndian2Long(data, index);
			
			index += 8;
		}
		
		byte[] masking = null;
		
		if ( mask ) {
			masking = new byte[4];
			System.arraycopy(data, ++index, masking, 0, 4);
			index += 3;
		}
		
		byte[] message = new byte[(int) Length];
		System.arraycopy(data, ++index, message, 0, (int) Length);
		index += Length - 1;
		
		if ( mask ) {
			frame.setData(parseMessage(message, masking));
		}
		else {
			frame.setData(message);
		}
		
		frame.setComplete(true);
		
		return frame;
	}
	
	public static WsFrame parseFrame(byte[] data) 
	{
		return parseFrame(data, 0, data.length);
	}
	
	private static byte[] buildFrame(int opcode, boolean mask, byte[] data, boolean close) 
	{
		int total = 2 + data.length;
		if ( mask == true ) {
			total +=  4;
		}
		
		if ( data.length == 0x7e ) {
			total += 2;
		}
		else if ( data.length >= 0x7f ) {
			total += 8;
		}

		byte[] frame = new byte[total];
		
		int index = 0;
		
		if ( close ) {
			frame[index] = (byte) (0x01 << 7 | (0x00 << 6) | (0x00 << 5) | (0x00 << 4) | opcode);
			
		}
		else {
			frame[index] = (byte) (0x00 << 7 | (0x00 << 6) | (0x00 << 5) | (0x00 << 4) | opcode);
		}
		
		if ( mask ) {
			frame[++index] = (byte) (0x01 << 7 | getPayloadLength(data.length));
		}
		else {
			frame[++index] = (byte) (0x00 << 7 | getPayloadLength(data.length));
		}
		
		if ( data.length == 0x7e ) {
			System.arraycopy(getLengthBytes((short) data.length), 0, frame, ++index, 2);
			index += 1;
		}
		else if ( data.length > 0x7f ) {
			System.arraycopy(getLengthBytes(data.length), 0, frame, ++index, 8);
			index += 7;
		}
		
		if ( mask ) {
			Random random = new Random();
			
			byte[] masking = IntegerUtil.int2BigEndian(Math.abs(random.nextInt()));
			
			System.arraycopy(masking, 0, frame, ++index, 4);
			index += 3;
			
			parseMessage(data, masking);
		}
		
		System.arraycopy(data, 0, frame, ++index, data.length);
		index += data.length - 1;
		
		return frame;
	}
	
	public static byte[] newFrame(int opcode, boolean mask, byte[] data, boolean close) 
	{
		return buildFrame(opcode, mask, data, close);
	}
	
	public static byte[] newCloseFrame(int code) 
	{
		return newFrame(WsFrame.OPCODE_CLOSE, false, String.valueOf(code).getBytes(), true);
	}
}
