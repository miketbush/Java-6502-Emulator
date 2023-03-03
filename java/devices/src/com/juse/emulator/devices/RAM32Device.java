package com.juse.emulator.devices;

import com.juse.emulator.util.loaders.ROMLoader;
import com.juse.emulator.interfaces.AddressMap;
import com.juse.emulator.interfaces.BusAddressRange;
import com.juse.emulator.interfaces.BusDevice;
import com.juse.emulator.interfaces.IOSize;
import com.juse.emulator.interfaces.RAM;

public class RAM32Device implements BusDevice, RAM
{
	private byte[][] banks;
	private BusAddressRange bar;

	protected byte[][] createBanks(long allocationSize)
	{
		byte[][] banks;
		long bankCount = 0;
		
		System.out.println("RAM32Device::createBanks:" + Long.toHexString(allocationSize));

		int high = (int)((allocationSize & 0xFFFF0000L) >> 16);
		System.out.println("createBanks high:" + high);
		
		banks = new byte[(int)high][0x0000FFFF + 1];
		
		System.out.println("\tbanks high:" + high + " (" + Integer.toHexString(high) + ")");
		System.out.println("\tbanks low :" + 0x0000FFFF  + " (" + Integer.toHexString(0x0000FFFF) + ")");		
		
		return banks;
	}	
	
	protected byte readBank(int address, IOSize size)
	{
		int high = (int)((address & 0xFFFF0000L) >> 16);
		int low = (int)((address & 0x0000FFFFL));
		
		System.out.println("readBank linear address:" + Long.toHexString(address));
		System.out.println("\thigh:" + high + " (" + Integer.toHexString(high) + ")");
		System.out.println("\tlow :" + low  + " (" + Integer.toHexString(low) + ")");
		
		
		return banks[high][low];		
	}

	protected int readBankValue(int address, IOSize size)
	{
		int value = 0;
		
		int high = (int)((address & 0xFFFF0000L) >> 16);
		int low  = (int)((address & 0x0000FFFFL));
		
		if(IOSize.IO8Bit == size)
		{
			value = (byte)(banks[high][low] & 0xFF);
		}
		else if(IOSize.IO16Bit == size)
		{
			value  = (int)((banks[high][low] & 0xFF));
			value |= (int)((banks[high][low+1] & 0xFF) << 8);
		}
		else if(IOSize.IOBig16Bit == size)
		{
			value = (int)((banks[high][low+1] & 0xFF));
			value |= (int)((banks[high][low] & 0xFF) << 8);
		}		
		else if(IOSize.IO32Bit == size)
		{
			value =  (int)((banks[high][low]   & 0xFF));
			value |= (int)((banks[high][low+1] & 0xFF) << 8);
			value |= (int)((banks[high][low+2] & 0xFF) << 16);
			value |= (int)((banks[high][low+3] & 0xFF) << 24);   
		}
		else if(IOSize.IOBig32Bit == size)
		{
			value  = (int)((banks[high][low]   & 0xFF) << 24);
			value |= (int)((banks[high][low+1] & 0xFF) << 16);
			value |= (int)((banks[high][low+2] & 0xFF) << 8);
			value |= (int)((banks[high][low+3] & 0xFF));
		}						
		
		return value;		
	}	

	
	public  void writeBank(int address, int value, IOSize size)
	{
		int high = (int)((address & 0xFFFF0000L) >> 16);
		int low = (int)((address & 0x0000FFFFL));
		
		if(IOSize.IO8Bit == size)
		{
			banks[high][low] = (byte)(value & 0xFF);
		}
		else if(IOSize.IO16Bit == size)
		{
			banks[high][low] = (byte)((value & 0x00FF));
			banks[high][low+1] = (byte)((value & 0xFF00) >> 8);
		}
		else if(IOSize.IOBig16Bit == size)
		{
			banks[high][low] = (byte)((value & 0xFF00) >> 8);
			banks[high][low+1] = (byte)((value & 0x00FF));
		}		
		else if(IOSize.IO32Bit == size)
		{
			banks[high][low]   = (byte)((value & 0x000000FF));
			banks[high][low+1] = (byte)((value & 0x0000FF00) >> 8);
			banks[high][low+2] = (byte)((value & 0x00FF0000) >> 16);
			banks[high][low+3] = (byte)((value & 0xFF000000) >> 24);
		}
		else if(IOSize.IOBig32Bit == size)
		{
			banks[high][low]   = (byte)((value & 0xFF000000) >> 24);
			banks[high][low+1] = (byte)((value & 0x00FF0000) >> 16);
			banks[high][low+2] = (byte)((value & 0x0000FF00) >> 8);
			banks[high][low+3] = (byte)((value & 0x000000FF));
		}						
	}	

	
	
	public RAM32Device(BusAddressRange bar)
	{
		int bankSize = bar.getSize();
		this.banks = createBanks(bankSize);
		this.bar = bar;
	}

	public RAM32Device(int bankAddress, int bankSize)
	{
		this(new BusAddressRangeImpl(bankAddress, bankSize, 1));
	}

	@Override
	public String getName()
	{
		return "RAM32";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		if(address < 0)
		{
			address = 0xFFFF + address + 1;
			//System.out.print(BusAddressRange.makeHexAddress(address));
		}
		int effectiveAddress = address - this.bar.getLowAddress();
		writeBank(effectiveAddress,(byte) (value & 0x0FF), IOSize.IO8Bit);
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		int effectiveAddress = address - this.bar.getLowAddress();
		return readBank(effectiveAddress, size);
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		int effectiveAddress = address - this.bar.getLowAddress();
		return readBank(effectiveAddress, size);
	}

	public void dumpContents(int max)
	{
		if (max == -1)
		{
			max = bar.getSize() - 1;
		}
		int bytes = 0;
		for (int i = 0; i < max; i++)
		{
			if (bytes == 0)
				System.out.print(BusAddressRange.makeHexAddress(this.bar.getLowAddress() + i) + ": ");

			System.out.print(BusAddressRange.makeHex(readBank(i,IOSize.IO8Bit)));
			System.out.print(" ");
			bytes++;
			if (bytes > 7)
			{
				System.out.println();
				bytes = 0;
			}
		}
	}

	public String toString(int bytesPerLine, boolean addresses)
	{
		StringBuilder sb = new StringBuilder();

		if (addresses)
			sb.append("0000: ");

		for (int i = 1; i <= bar.getSize(); i++)
		{
			if ((i % bytesPerLine != 0) || (i == 0))
			{
				sb.append(ROMLoader.byteToHexString((byte)readBank(i - 1, IOSize.IO8Bit)) + " ");
			}
			else
			{
				String zeroes = "0000";
				sb.append(ROMLoader.byteToHexString((byte)readBank(i - 1, IOSize.IO8Bit)) + "\n");
				if (addresses)
					sb.append(zeroes.substring(0, Math.max(0, 4 - Integer.toHexString(i).length()))
							+ Integer.toHexString(i) + ": ");
			}
		}

		return sb.toString();
	}

	public static void main(String[] args)
	{
		RAM32Device rd = new RAM32Device(0x00010000, 64 * 1024);
		System.out.println(rd.getName());
		System.out.println(
				rd.getBusAddressRange().getLowAddressHex() + ":" + rd.getBusAddressRange().getHighAddressHex());
		rd.writeAddress(0x10000, 0xFF, IOSize.IO8Bit);
		rd.writeAddress(0x10001, 0xFE, IOSize.IO8Bit);
		rd.writeAddress(0x10002, 0xFD, IOSize.IO8Bit);
		rd.dumpContents(32);

		System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x10000, IOSize.IO8Bit)));
		System.out.println(BusAddressRange.makeHex((byte) rd.readAddressUnsigned(0x10002, IOSize.IO8Bit)));

	}

	@Override
	public void setRAMArray(byte[] array)
	{
		setRAMArray(0,array);	
	}	
	
	@Override
	public void setRAMArray(int base, byte[] array) 
	{
		//this.array = array;
		reset();
		
		for(int p=0;p<array.length;p++)
			writeBank(base + p,array[p], IOSize.IO8Bit);
		
	}

	@Override
	public byte read(short address)
	{
		System.out.println(AddressMap.toHexAddress(address, IOSize.IO16Bit));
		return (byte)this.readAddressSigned((int)address, IOSize.IO8Bit);
	}

	@Override
	public void write(short address, byte data)
	{
		this.writeBank((int)address, data, IOSize.IO8Bit);		
	}

	@Override
	public String getRAMString()
	{
		return  this.toString(8, true);
	}

	@Override
	public void reset()
	{
		for(int p=0;p<bar.getSize();p++)
			writeBank(p, 0x00, IOSize.IO8Bit);	
	}

}
