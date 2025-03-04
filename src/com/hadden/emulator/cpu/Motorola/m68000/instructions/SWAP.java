package com.hadden.emulator.cpu.Motorola.m68000.instructions;

import com.hadden.emulator.cpu.Motorola.m68000.*;

/*
//  M68k - Java Amiga MachineCore
//  Copyright (c) 2008-2010, Tony Headford
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
//  following conditions are met:
//
//    o  Redistributions of source code must retain the above copyright notice, this list of conditions and the
//       following disclaimer.
//    o  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
//       following disclaimer in the documentation and/or other materials provided with the distribution.
//    o  Neither the name of the M68k Project nor the names of its contributors may be used to endorse or promote
//       products derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
//  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
//  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
*/
public class SWAP implements InstructionHandler
{
	protected final Cpu cpu;

	public SWAP(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base = 0x4840;
		Instruction i = new Instruction() {
			public int execute(int opcode)
			{
				return swap(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode);
			}
		};

		for(int reg = 0; reg < 8; reg++)
		{
			is.addInstruction(base + reg, i);
		}
	}

	protected int swap(int opcode)
	{
		int reg = (opcode & 0x007);
		int v = cpu.getDataRegisterLong(reg);
		int vh = (v >> 16) & 0x0000ffff;
		v = (v << 16) + vh;
		cpu.setDataRegisterLong(reg, v);
		// swap affects the registers : z set if result = 0, N set if bit 31 set, v & c always cleared
		cpu.calcFlags(InstructionType.SWAP, v,v,v, Size.Long);
		return 4;
	}

	public final DisassembledInstruction disassembleOp(int address, int opcode)
	{
		DisassembledOperand src = new DisassembledOperand("d" + (opcode & 0x07));
		return new DisassembledInstruction(address, opcode, "swap", src);
	}
}
