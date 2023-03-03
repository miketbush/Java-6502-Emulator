package com.juse.emulator.interfaces;
public interface BusExListener extends BusListener
{
	void readListener(int address, IOSize size); 	
	void writeListener(int address, int data, IOSize size);
}
