import java.util.*;
import java.util.List;

class VM {
    String name;
    int cpu;
    int ram;

    public VM(String name, int cpu, int ram) {
        this.name = name;
        this.cpu = cpu;
        this.ram = ram;
    }
}

class Host {
    String name;
    int availableCpu;
    int availableRam;
    int currentVms;
    int capacity;

    public Host(String name, int cpu, int ram, int currentVms, int capacity) {
        this.name = name;
        this.availableCpu = cpu;
        this.availableRam = ram;
        this.currentVms = currentVms;
        this.capacity = capacity;
    }
}

public class EC2_Resource_allocation {

    static Map<String, List<String>> computeVmPreferences(List<VM> vms, List<Host> hosts) {
        Map<String, List<String>> preferences = new HashMap<>();

        System.out.println("\n--- VM Preferences (hosts sorted by available resource score) ---");
        for (VM vm : vms) {
            Map<String, Double> scores = new HashMap<>();
            for (Host host : hosts) {
                double score = (host.availableCpu + host.availableRam) / (double) (host.currentVms == 0 ? 1 : host.currentVms);
                scores.put(host.name, score);
            }
            List<String> sortedHosts = scores.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .map(Map.Entry::getKey)
                    .toList();
            preferences.put(vm.name, sortedHosts);

            System.out.print(vm.name + " preferences: ");
            sortedHosts.forEach(h -> System.out.print(h + "(" + String.format("%.2f", scores.get(h)) + ") "));
            System.out.println();
        }

        return preferences;
    }

    static Map<String, List<String>> computeHostPreferences(List<VM> vms, List<Host> hosts) {
        Map<String, List<String>> preferences = new HashMap<>();
        int totalHosts = hosts.size();

        System.out.println("\n--- Host Preferences (VMs sorted by resource requirement ascending) ---");
        for (Host host : hosts) {
            Map<String, Double> scores = new HashMap<>();
            for (VM vm : vms) {
                double score = (vm.cpu + vm.ram) / (double) totalHosts;
                scores.put(vm.name, score);
            }
            List<String> sortedVms = scores.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .toList();
            preferences.put(host.name, sortedVms);

            System.out.print(host.name + " preferences: ");
            sortedVms.forEach(vm -> System.out.print(vm + "(" + String.format("%.2f", scores.get(vm)) + ") "));
            System.out.println();
        }

        return preferences;
    }

    static Map<String, List<String>> stableMatch(
            Map<String, List<String>> vmPrefs,
            Map<String, List<String>> hostPrefs,
            Map<String, Integer> hostCapacities
    ) {
        Map<String, List<String>> hostAssignments = new HashMap<>();
        Map<String, Integer> proposalIndex = new HashMap<>();
        Queue<String> freeVms = new LinkedList<>();

        for (String vm : vmPrefs.keySet()) {
            freeVms.add(vm);
            proposalIndex.put(vm, 0);
        }
        for (String host : hostPrefs.keySet()) {
            hostAssignments.put(host, new ArrayList<>());
        }

        int iteration = 1;
        System.out.println("\n--- Starting stable matching iterations ---");
        while (!freeVms.isEmpty()) {
            String vm = freeVms.poll();
            List<String> preferredHosts = vmPrefs.get(vm);
            if (proposalIndex.get(vm) >= preferredHosts.size()) continue;

            String host = preferredHosts.get(proposalIndex.get(vm));
            proposalIndex.put(vm, proposalIndex.get(vm) + 1);
            List<String> assignedVms = hostAssignments.get(host);

            System.out.printf("\nIteration %d: VM %s proposes to Host %s\n", iteration++, vm, host);

            if (assignedVms.size() < hostCapacities.get(host)) {
                assignedVms.add(vm);
                System.out.printf("Host %s has capacity. Assigned VM %s.\n", host, vm);
            } else {
                List<String> hostRanking = hostPrefs.get(host);
                String worst = assignedVms.stream()
                        .max(Comparator.comparingInt(hostRanking::indexOf))
                        .orElse(null);

                if (hostRanking.indexOf(vm) < hostRanking.indexOf(worst)) {
                    assignedVms.remove(worst);
                    assignedVms.add(vm);
                    freeVms.add(worst);
                    System.out.printf("Host %s replaced VM %s with VM %s. VM %s is free again.\n", host, worst, vm, worst);
                } else {
                    freeVms.add(vm);
                    System.out.printf("Host %s rejected VM %s. VM %s remains free.\n", host, vm, vm);
                }
            }

            // Print current allocation matrix
            printCurrentAllocations(hostAssignments);
        }

        return hostAssignments;
    }

    static void printCurrentAllocations(Map<String, List<String>> hostAssignments) {
        System.out.println("Current Host Assignments:");
        hostAssignments.forEach((host, vms) -> {
            System.out.print(host + " -> ");
            if (vms.isEmpty()) System.out.print("(none)");
            else vms.forEach(vm -> System.out.print(vm + " "));
            System.out.println();
        });
    }

    static void printInitialHostScores(List<Host> hosts) {
        System.out.println("--- Initial Hosts resource scores ---");
        for (Host host : hosts) {
            double score = (host.availableCpu + host.availableRam) / (double) (host.currentVms == 0 ? 1 : host.currentVms);
            System.out.printf("%s: AvailableCpu=%d, AvailableRam=%d, CurrentVMs=%d, Score=%.2f\n",
                    host.name, host.availableCpu, host.availableRam, host.currentVms, score);
        }
    }

    static void printVmRequirements(List<VM> vms) {
        System.out.println("--- VM Resource Requirements ---");
        for (VM vm : vms) {
            System.out.printf("%s: CPU=%d, RAM=%d\n", vm.name, vm.cpu, vm.ram);
        }
    }

    public static void main(String[] args) {

        List<VM> vms = List.of(
                new VM("vm1", 2, 4),
                new VM("vm2", 4, 6),
                new VM("vm3", 1, 2)
        );

        List<Host> hosts = List.of(
                new Host("h1", 6, 12, 1, 2),
                new Host("h2", 8, 16, 2, 2),
                new Host("h3", 10, 8, 1, 2)
        );

        printVmRequirements(vms);
        printInitialHostScores(hosts);

        Map<String, Integer> hostCapacities = new HashMap<>();
        for (Host h : hosts) hostCapacities.put(h.name, h.capacity);

        Map<String, List<String>> vmPrefs = computeVmPreferences(vms, hosts);
        Map<String, List<String>> hostPrefs = computeHostPreferences(vms, hosts);

        Map<String, List<String>> result = stableMatch(vmPrefs, hostPrefs, hostCapacities);

        System.out.println("\n--- Final Assignments ---");
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            System.out.println(entry.getKey() + " → " + entry.getValue());
        }
    }
}
