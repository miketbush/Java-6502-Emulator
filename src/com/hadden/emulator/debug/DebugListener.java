package com.hadden.emulator.debug;

import com.hadden.emu.IOSize;

public interface DebugListener
{
	public static enum DebugReason
	{
		Step,
		Clock,
		Break
	}
	
	public static enum DebugCode
	{
		None
	}
	
	DebugCode debugEvent(DebugReason dr, byte data, IOSize size);
	DebugCode debugEvent(DebugReason dr, short data, IOSize size);
	DebugCode debugEvent(DebugReason dr, int data, IOSize size);
	DebugCode debugEvent(DebugReason dr, long data, IOSize size);
}
