package graph

// Transpose reverses all edges in a graph.
func Transpose(g [][]int) [][]int {
	n := len(g)
	size := make([]int, n)
	var total int
	for _, edges := range g {
		for _, to := range edges {
			size[to]++
		}
		total += len(edges)
	}
	pool := make([]int, total)
	ret := make([][]int, n)
	for i, size := range size {
		ret[i] = pool[:0:size]
		pool = pool[size:]
	}
	for from, edges := range g {
		for _, to := range edges {
			ret[to] = append(ret[to], from)
		}
	}
	return ret
}
