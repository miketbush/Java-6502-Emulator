package com.juse.emulator.interfaces;


public interface BusEx extends Bus
{
	int read(int address, IOSize size); 	
	void write(int address, int data, IOSize size);
}
