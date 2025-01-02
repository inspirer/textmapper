// Package debug provides utilities for printing basic data structures for human consumption.
package debug

import (
	"fmt"
)

// Size outputs a rounded size in bytes.
func Size(bytes int) string {
	if bytes < 1024 {
		return fmt.Sprintf("%v B", bytes)
	}
	if bytes < 1024*1024 {
		return fmt.Sprintf("%.2f KiB", float64(bytes)/1024.)
	}
	return fmt.Sprintf("%.2f MiB", float64(bytes)/(1024*1024))
}
