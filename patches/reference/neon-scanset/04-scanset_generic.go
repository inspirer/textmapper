//go:build !arm64 || tmnosimd

package fastjs

const hasSIMD = false

func scanSet(s string, nib *[32]uint8) int { return 0 }
