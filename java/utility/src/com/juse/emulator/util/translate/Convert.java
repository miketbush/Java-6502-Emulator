package com.juse.emulator.util.translate;

import com.juse.emulator.interfaces.Instruction;

public class Convert
{
	public static String byteToHexString(byte b)
	{
		if (Byte.toUnsignedInt(b) < 16)
			return "0" + Integer.toHexString(Byte.toUnsignedInt(b)).toUpperCase();
		return Integer.toHexString(Byte.toUnsignedInt(b)).toUpperCase();
	}

	
	public static String padStringWithZeroes(String s, int padLength)
	{
		try
		{
			if(s.length() > padLength)
				padLength = s.length();
			
			char[] pads = new char[padLength - s.length()];
			for (int i = 0; i < pads.length; i++)
			{
				pads[i] = '0';
			}
			return String.valueOf(pads) + s;
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static String getAddressAbsoluteAsString(Instruction dbgOp, short addressAbsolute)
	{
		if (dbgOp.addressMode.equals("REL"))
		{
			return ("$" + Integer.toHexString(Byte.toUnsignedInt((byte) addressAbsolute)));
		}
		return ("$" + Integer.toHexString(Short.toUnsignedInt(addressAbsolute)));
	}

	public static String toHex16String(int addressAbsolute)
	{
		return padStringWithZeroes(Integer.toHexString((int)addressAbsolute & 0x0000FFFF),4).toUpperCase();
	}
	
	public static String toHex16String(short addressAbsolute)
	{
		return padStringWithZeroes(Integer.toHexString((int)addressAbsolute & 0x0000FFFF),4).toUpperCase();
	}

	public static String toHex8String(byte addressAbsolute)
	{
		return padStringWithZeroes(Integer.toHexString((int)addressAbsolute & 0x000000FF),2).toUpperCase();
	}		
	
	public static String toHex8String(short addressAbsolute)
	{
		return padStringWithZeroes(Integer.toHexString((int)addressAbsolute & 0x000000FF),2).toUpperCase();
	}	
	
}
