import heapq
graph = {
    'S': [('A', 7), ('B', 10), ('C', 5)],'A': [('D', 4), ('H', 6)],'B': [('G1', 10), ('H', 4)],
    'C': [('E', 7), ('F', 9)], 'D': [],'E': [('J', 8)],
    'F': [('G2', 2)],'H': [('J', 4)],'J': [('G2', 2)],
    'G1': [],'G2': []
}
heuristics = {
    'S': 14, 'A': 10, 'B': 9, 'C': 7, 'D': 999, 'E': 8, 'F': 6,
    'H': 6, 'J': 3, 'G1': 0, 'G2': 0
}
def a_star(start, goals):
    frontier = []
    heapq.heappush(frontier, (0 + heuristics[start], 0, start, [start]))

    visited = set()
    while frontier:
        est_total_cost, cost_so_far, current, path = heapq.heappop(frontier)
        if current in goals:
            return path, cost_so_far
        if current in visited:
            continue
        visited.add(current)
        for neighbor, step_cost in graph.get(current, []):
            if neighbor not in visited:
                total_cost = cost_so_far + step_cost
                est = total_cost + heuristics[neighbor]
                heapq.heappush(frontier, (est, total_cost, neighbor, path + [neighbor]))

    return None, float('inf')

# Usage
path, cost = a_star('S', {'G1', 'G2'})
print(f"Optimal Path to CDN Node: {path}, Total Cost: {cost}")
