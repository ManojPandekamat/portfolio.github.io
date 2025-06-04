import hashlib
import bisect

class ConsistentHashRing:
    def __init__(self, servers=None, replicas=3):
        self.replicas = replicas  # virtual nodes for better distribution
        self.ring = dict()
        self.sorted_keys = []
        
        if servers:
            for server in servers:
                self.add_server(server)
    
    def _hash(self, key):
        # Use md5, convert to int
        return int(hashlib.md5(key.encode('utf-8')).hexdigest(), 16)
    
    def add_server(self, server):
        for i in range(self.replicas):
            virtual_node_key = f"{server}#{i}"
            hash_key = self._hash(virtual_node_key)
            self.ring[hash_key] = server
            bisect.insort(self.sorted_keys, hash_key)
    
    def remove_server(self, server):
        for i in range(self.replicas):
            virtual_node_key = f"{server}#{i}"
            hash_key = self._hash(virtual_node_key)
            del self.ring[hash_key]
            index = bisect.bisect_left(self.sorted_keys, hash_key)
            self.sorted_keys.pop(index)
    
    def get_server(self, key):
        if not self.ring:
            return None
        hash_key = self._hash(key)
        # Find the first server hash >= key hash
        index = bisect.bisect(self.sorted_keys, hash_key)
        if index == len(self.sorted_keys):
            index = 0  # wrap around
        return self.ring[self.sorted_keys[index]]


# Example usage:
servers = ["server1", "server2", "server3"]
ring = ConsistentHashRing(servers)

buckets = ["bucket-alpha", "bucket-beta", "bucket-gamma", "bucket-delta"]

for bucket in buckets:
    server = ring.get_server(bucket)
    print(f"{bucket} is stored on {server}")

# Try removing a server
ring.remove_server("server2")
print("\nAfter removing server2:")

for bucket in buckets:
    server = ring.get_server(bucket)
    print(f"{bucket} is stored on {server}")
