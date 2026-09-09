//go:build arm64 && !tmnosimd

package fastjs

const hasSIMD = true

//go:noescape
func scanSet(s string, nib *[32]uint8) int
