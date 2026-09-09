//go:build arm64 && !tmnosimd

#include "textflag.h"

// func scanSet(s string, nib *[32]uint8) int
//
// Returns the index of the first byte b of s for which
// nib[b&15] & nib[16+(b>>4)] == 0, considering only the leading
// len(s)&^15 bytes. If every byte of those full 16-byte chunks is in the
// set, returns len(s)&^15.
TEXT ·scanSet(SB), NOSPLIT|NOFRAME, $0-32
	MOVD	s_base+0(FP), R0
	MOVD	s_len+8(FP), R1
	MOVD	nib+16(FP), R2
	AND	$-16, R1, R11		// bytes covered by full chunks
	MOVD	$0, R9
	CBZ	R11, done
	VLD1	(R2), [V0.B16, V1.B16]	// V0: low-nibble table, V1: high-nibble table
	VMOVI	$15, V2.B16
	VEOR	V10.B16, V10.B16, V10.B16

loop:
	VLD1	(R0), [V3.B16]
	VAND	V2.B16, V3.B16, V4.B16	// low nibbles
	VUSHR	$4, V3.B16, V5.B16	// high nibbles
	VTBL	V4.B16, [V0.B16], V6.B16
	VTBL	V5.B16, [V1.B16], V7.B16
	VAND	V6.B16, V7.B16, V8.B16
	VCMEQ	V10.B16, V8.B16, V9.B16	// 0xff in lanes NOT in the set
	VMOV	V9.D[0], R6
	VMOV	V9.D[1], R7
	ORR	R6, R7, R8
	CBNZ	R8, found
	ADD	$16, R0
	ADD	$16, R9
	CMP	R11, R9
	BLO	loop

done:
	MOVD	R9, ret+24(FP)
	RET

found:
	CBZ	R6, hi
	RBIT	R6, R6
	CLZ	R6, R6
	LSR	$3, R6
	ADD	R6, R9
	MOVD	R9, ret+24(FP)
	RET

hi:
	RBIT	R7, R7
	CLZ	R7, R7
	LSR	$3, R7
	ADD	$8, R7
	ADD	R7, R9
	MOVD	R9, ret+24(FP)
	RET
