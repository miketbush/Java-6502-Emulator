package com.hadden.emu.c64;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.*;

import com.hadden.emu.AddressMap;
import com.hadden.emu.BusAddressRange;
import com.hadden.emu.BusDevice;
import com.hadden.emu.HasPorts;
import com.hadden.emu.IOSize;
import com.hadden.fonts.FontManager;

public class ScreenDevice extends JFrame implements BusDevice, HasPorts, ActionListener
{
	private static final int CONST_COLORPAGE   = 0;
	private static final int CONST_PALETTEPORT = 1;
	
	DisplayPanel p = null; //new DisplayPanel();
	Timer t;
	Timer cursorTimer;
	Font lcdFont;
	Scanner s;

	 int baseAddress = 0;
	 int displayColumns = 0;
	 int displayRows = 0;
	
	boolean graphicalCursorBlinkFlag = false;

	boolean debug = true;

	// Internal flags
	int cursorPos = 0;
	boolean increment = true;
	boolean displayPower = true;
	boolean cursor = false;
	boolean cursorBlink = false;
	boolean fourBitMode = false;

	BusAddressRange bar = null;
	char[] bank = null;
	int bankSize = 0;
	TextPort[] ports = null;
	int regsPalette[] = {0,0,0};

	Color palette[] = {
			Color.getHSBColor(0.62f, 0.87f, 0.78f),
			Color.DARK_GRAY,
            	Color.BLACK,
		    Color.DARK_GRAY,
		    Color.GRAY,
		    Color.LIGHT_GRAY,
		    Color.RED,
		    Color.GREEN,
		    Color.BLUE,
		    Color.CYAN,
		    Color.ORANGE,
		    Color.YELLOW,
		    Color.MAGENTA,
		    Color.PINK,
		    Color.DARK_GRAY,
		    Color.WHITE
		  };
	private int basePort;
	private int x_offset;
	private int x_width;
	private int y_offset;
	private int y_width;
	private int x_margin;
	private int y_margin;
	private int y_border;
	private float fontScale;
	private int x_border;
	private int screenFactor;
	private int[] bankColor;	

	final class TextPort implements BusDevice
	{
		private String name;
		private int port;
		private BusAddressRange bar;
		private BusDevice parent = null;
		private int portValue = 0;
		
		public TextPort(String name, int port, BusAddressRange bar)
		{
			this(name, port, bar,null);
		}

		public TextPort(String name, int port, BusAddressRange bar, BusDevice parent)
		{
			this.name = name;
			this.port = port;
			this.bar  = bar;
			this.parent = parent;
			
			//System.out.println("TextPort:" + name + "[" + port + "]");
		}		
		
		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public BusAddressRange getBusAddressRange()
		{
			return bar;
		}

		@Override
		public void writeAddress(int address, int value, IOSize size)
		{
			if(this.parent!=null)
			{
				this.parent.writeAddress(address, value,size);
				return;
			}
				
			if(address == port)
				portValue = value;				
		}

		@Override
		public int readAddressSigned(int address, IOSize size)
		{
			if(this.parent!=null)
				return this.parent.readAddressSigned(address, size);
			
			if(address == port)
				return portValue;
			
			return 0;			
		}

		@Override
		public int readAddressUnsigned(int address, IOSize size)
		{
			if(this.parent!=null)
				return this.parent.readAddressUnsigned(address, size);
			
			if(address == port)
				return portValue;
			
			return 0;		
		}

		@Override
		public void reset()
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	
	public ScreenDevice(int baseAddress, int displayColumns, int displayRows)
	{
		this.x_offset = 12;
		this.x_width  = 30;
		this.x_margin = 3;
		this.x_border = 15;
		this.y_offset = 25;
		this.y_width  = 47;
		this.y_margin = 3;
		this.y_border = 70;
		this.fontScale = 35.0f; 

		this.screenFactor = 1;
		
		if(displayColumns == 80)
		{
			this.x_offset /= 2;
			this.x_width  /= 2;
			this.x_margin /= 2;
			this.y_offset /= 2;
			this.y_width  /= 2;
			this.y_margin /= 2;
			this.y_border /= 2;
			this.fontScale /= 2;
			this.screenFactor*=2;
		}
		
		this.setSize((x_width + x_margin) * (displayColumns + screenFactor), 
				     (y_width + y_margin) * displayRows + (y_border * screenFactor));
		
		
		this.p = new DisplayPanel(fontScale);
		
		this.bankSize = displayColumns*displayRows;
		this.baseAddress = baseAddress;
		this.displayColumns = displayColumns;
		this.displayRows = displayRows;
		this.bank = new char[bankSize + 1];
		this.bankColor = new int[bankSize + 1];
		this.bar = new BusAddressRange(baseAddress,
									   bankSize,
				                       1);
		this.basePort = this.baseAddress  + bank.length;
		
		
		ports = new TextPort[2];
		ports[CONST_COLORPAGE]   = new TextPort("TEXT-COLORPAGE",bar.getHighAddress()+1,
				                                new BusAddressRange(bar.getHighAddress()+1,bankSize,1), this);
		ports[CONST_PALETTEPORT] = new TextPort("TEXT-PALETTEPORT",bar.getHighAddress()+bankSize+1,
				                                new BusAddressRange(bar.getHighAddress()+bankSize+1,3 + 1,1), this); 			
		
		
		t = new Timer(100, this);
		t.start();
		cursorTimer = new Timer(500, this);
		cursorTimer.start();

		for(int i=0;i<bankSize;i++)
		{
			this.bank[i] = 0;
			this.bankColor[i] = 0;
		}
		
		
		this.setTitle("Display");
		this.setContentPane(p);
		this.setAlwaysOnTop(false);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public class DisplayPanel extends JPanel
	{
		public DisplayPanel(float fontScale)
		{
			try
			{
				lcdFont = FontManager.loadFont(FontManager.C64_TEXT,fontScale);
				//FONT_ARCADE_CLS
				//lcdFont = FontManager.loadFont(FontManager.FONT_JOYSTICK_TEXT);
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(lcdFont);
			}
			catch (Exception e)
			{
				System.out.println(e.toString());
			}
		}

		public void paintComponent(Graphics g)
		{
			//g.setColor(Color.getHSBColor(0.62f, 0.83f, 1f));
			g.setColor(Color.getHSBColor(0.62f, 0.87f, 0.78f));
			
			g.fillRect(0, 0, p.getWidth(), p.getHeight());
			g.setColor(Color.getHSBColor(0.62f, 0.87f, 0.78f));
			for (int i = 0; i < displayColumns ; i++)
			{
				for (int j = 0; j < displayRows; j++)
				{
					g.fillRect(x_offset + (x_width + x_margin) * i, y_offset + (y_width + y_margin) * j, x_width, y_width);
				}
			}
			if (displayPower)
			{
				g.setFont(lcdFont);
				for (int i = 0; i < displayColumns; i++)
				{
					for (int j = 0; j < displayRows; j++)
					{
						int color = 15;//(int)bankColor[(i + j * displayColumns)];
						int fcolor = 15;//color & 0x000F;
						int bcolor = 0;//(color & 0x00F0) >> 4;
						
					    g.setColor(palette[bcolor]);
					    g.fillRect(x_offset + (x_width + x_margin) * i, y_offset + (y_width + y_margin) * j, x_width, y_width);
					    g.setColor(palette[fcolor]);
						
						char dv = bank[i + j * displayColumns];
						if(dv == 0)
							dv = ' ';
								
						if(dv < 32)
							dv += 64;
						
						g.drawString(String.valueOf(dv), x_offset + (x_width + x_margin) * i, y_border + (y_width + y_margin) * j);
						/*
						if (i + j * 40 == cursorPos)
						{
							if (graphicalCursorBlinkFlag)
								g.fillRect(x_offset + (x_width + x_margin) * i, 66 + 50 * j, 30, 5);
						}
						*/
					}
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource().equals(t))
		{			
			p.repaint();
		}
		if (arg0.getSource().equals(cursorTimer))
		{
			if (cursor)
			{
				if (cursorBlink)
				{
					graphicalCursorBlinkFlag = !graphicalCursorBlinkFlag;
				}
				else
				{
					graphicalCursorBlinkFlag = cursor;
				}
			}
			else
			{
				graphicalCursorBlinkFlag = false;
			}
		}
	}


	@Override
	public String getName()
	{
		return "C64-DISPLAY";
	}

	@Override
	public BusAddressRange getBusAddressRange()
	{
		return this.bar;
	}

	@Override
	public void writeAddress(int address, int value, IOSize size)
	{
		//System.out.println("ADDR:" + AddressMap.toHexAddress(address,IOSize.IO16Bit) + ":" + value);
		
		if(address >= 0xD800 && address <= 0xDBE7)
		{
			bankColor[address - 0xD800] = (char) value;
			//System.out.println("COLOR:" + value);
		}
		else
		{				
			bank[this.bar.getRelativeAddress(address)] = (char) value;
			
			if(value!=0 && value!=85 && value!=32 && value!=-85)
				System.out.println("CC:" + value + "[" + Integer.toHexString( (int)value & 0xFF) + "]" + (char) value);
		
		}
		
		p.repaint();
	}

	@Override
	public int readAddressSigned(int address, IOSize size)
	{
		int v = 0;
		
		try
		{
			
			if(address >= 0xD800 && address <= 0xDBE7)
			{
				v = bankColor[address - 0xD800];
			}
			else
			{				
				v = bank[this.bar.getRelativeAddress(address)];
			}
		} 
		catch (Exception e)
		{
			System.out.println(Integer.toHexString(address));
			e.printStackTrace();
		}
		
		return v;
	}

	@Override
	public int readAddressUnsigned(int address, IOSize size)
	{
		return readAddressSigned(address, size);
	}

	public void dumpContents(int max)
	{
		if(max == -1)
		{
			max = bank.length - 1;
		}
		int bytes = 0;
		for(int i=0;i<max;i++)
		{	
			if(bytes == 0)
				System.out.print(BusAddressRange.makeHexAddress(this.bar.getLowAddress() + i) + ": ");
				
			System.out.print(BusAddressRange.makeHex((byte)(int)bank[i]));
			System.out.print(" ");
			bytes++;
			if(bytes>7)
			{				
				System.out.println();
				bytes = 0;
			}
		}
	}

	
	@Override
	public BusDevice[] ports(int baseAddress)
	{
		BusDevice[] ports = 
		{
			//new TextPort("TEXT-COLORPAGE",bar.getHighAddress()+1,new BusAddressRange(bar.getHighAddress()+1,bankSize,1), this),
			//new TextPort("TEXT-PALETTEPORT",bar.getHighAddress()+bankSize+1,new BusAddressRange(bar.getHighAddress()+bankSize+1,3 + 1,1), this),
			new TextPort("C64-COLORPAGE",0xD800,new BusAddressRange(0xD800,0x03E7,1), this),
		};
		return ports;
	}	
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		int cols = 40;
		int rows = 25;
		
		ScreenDevice display = new ScreenDevice(0x00000400,cols,rows);
		System.out.println(display.getBusAddressRange().getLowAddressHex()+ ":" +  display.getBusAddressRange().getHighAddressHex());	
		
		int colorPort = 0xD800;
		int curColor = 0x6F;
		
		int ascii = 32;
		for(int row=0;row<rows;row++)
		{
			/*
			if(row == 2)
			{
				display.writeAddress(colorPort + 1,127, IOSize.IO8Bit);
				display.writeAddress(colorPort + 2,127, IOSize.IO8Bit);
				display.writeAddress(colorPort + 3,127, IOSize.IO8Bit);
				display.writeAddress(colorPort,5, IOSize.IO8Bit);
				//display.writeAddress(0x0000A000 + (40*row) + col + display.bankSize, 0x6F, IOSize.IO8Bit);
				//display.writeAddress(0x0000A000 + (40*row) + col + display.bankSize, 0x6F, IOSize.IO8Bit);
				curColor = 0x65;
			}
			*/
			for(int col=0;col<cols;col++)
			{
				//if(ascii == 65)
				//	display.writeAddress(0x0000A000 + (cols*row) + col + display.bankSize, curColor, IOSize.IO8Bit);
				
				display.writeAddress(0x00000400 + (cols*row) + col, ascii++, IOSize.IO8Bit);
				if(ascii > 126)
				{
					ascii = 32;
				}
			}
		}
		
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.println("[ADDRESS] [CHAR]");

		
		
		
		while (true)
		{
			String input = scan.nextLine();

			String address  = input.substring(0,8);
			String asciiChar = input.substring(8);
			
			int location = Integer.parseInt(address,16);
			int asciiCode = (int)asciiChar.charAt(0);
						
			display.writeAddress(location, asciiCode, IOSize.IO8Bit);
			display.dumpContents(40);

		}
	}

	@Override
	public void reset()
	{
		// TODO Auto-generated method stub
		
	}	
}
