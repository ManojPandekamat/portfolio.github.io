import heapq

def dijkstra(graph, start):
    queue = [(0, start)]
    distances = {node: float('inf') for node in graph}
    distances[start] = 0
    previous = {node: None for node in graph}

    while queue:
        current_distance, current_node = heapq.heappop(queue)

        if current_distance > distances[current_node]:
            continue

        for neighbor, weight in graph[current_node]:
            distance = current_distance + weight

            if distance < distances[neighbor]:
                distances[neighbor] = distance
                previous[neighbor] = current_node
                heapq.heappush(queue, (distance, neighbor))

    return distances, previous

graph = {
    'S': [('A', 7), ('B', 10), ('C', 5)],'A': [('D', 4), ('H', 6)],
    'B': [('G1', 10), ('H', 4)],'C': [('E', 7), ('F', 9)],
    'D': [],'E': [('J', 8)],'F': [('G2', 2)],
    'H': [('J', 4)],'J': [('G2', 2)],'G1': [],
    'G2': []
}

distances, previous = dijkstra(graph, 'S')

print("Shortest distances from S:")
for node, dist in distances.items():
    print(f"{node}: {dist}")
