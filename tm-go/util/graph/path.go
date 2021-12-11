package graph

// LongestPath returns the longest path in a given graph.
func LongestPath(graph [][]int) []int {
	type node struct{ height, link int }
	data := make([]node, len(graph))

	var dfs func(i int)
	dfs = func(i int) {
		if data[i].height != 0 {
			// Do not enter a node twice.
			return
		}
		data[i].height = -1
		ret := node{1, -1}
		for _, next := range graph[i] {
			dfs(next)
			if height := data[next].height; height >= ret.height {
				ret = node{height + 1, next}
			}
		}
		data[i] = ret
	}
	first := -1
	for i := 0; i < len(graph); i++ {
		dfs(i)
		if first == -1 || data[first].height < data[i].height {
			first = i
		}
	}
	var ret []int
	for i := first; i != -1; i = data[i].link {
		ret = append(ret, i)
	}
	return ret
}
