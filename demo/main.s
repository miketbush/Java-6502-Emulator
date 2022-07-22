;
;ca65 main.s -D BASE=0x0000
;cl65 main.o --target none --start-addr 0x0000 -o main.bin
;
;
	.setcpu		"65C02"
	.debuginfo	off
    	
.segment  "VECTORS"

;.addr      _nmi_int    ; NMI vector
;.addr      _init       ; Reset vector
.addr      _irq_int    ; IRQ/BRK vector    	
    	
.segment  "CODE"

; push fake return address
	lda     #$FF
	pha 
	pha
; set IRQ
	SEI	
	lda #<_irq_int
    sta     $FFFE
    lda #>_irq_int	
	sta     $FFFE
	CLI
; test some instructions
	ldx     #$A0
	lda     #65
	ldy     #$C1
	dex
	inx
	inx
loop:
	CLD
	ADC		#02
	CMP 	#69
	BNE loop
; move data
	lda     #$AB
	sta     $00F8
	lda     #$CD
	sta     $00F9
	lda     #$EF
	sta     $00FA
; put text on display device
    lda     #65
	sta     $A000
	lda     #66
	sta     $A001
	ldy     #67
	ldx     #00
	tya
	sta     $A002,X
	inx
	iny
	tya
	sta     $A002,X
	inx
	iny
	tya
	sta     $A002,X
	
	
; print to LCD
	lda     #$00
	sta     $B000
	lda     #$0F
	sta     $B001
	lda     #$01
	sta     $B000
	lda     #'A'
	sta     $B001
; return back to start
	rts

_irq_int:
		PHX
		TSX
		PHA
		INX
		INX
		lda     #'A'
		sta     $00f8
		lda     #'B'
		sta     $00f9
; ---------------------------------------------------------------------------
; IRQ detected, return

irq:
		    PLA                    ; Restore accumulator contents
		    PLX                    ; Restore X register contents
		    RTI                    ; Return from all IRQ interrupts
