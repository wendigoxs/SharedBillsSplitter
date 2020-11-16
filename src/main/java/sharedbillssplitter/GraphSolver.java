package sharedbillssplitter;

import sharedbillssplitter.person.Person;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphSolver {

    private final List<MoneyTransaction> moneyTransactions;

    public GraphSolver(List<MoneyTransaction> moneyTransactions) {
        this.moneyTransactions = moneyTransactions;
    }

    public List<MoneyTransaction> solvePerfectBalance() {
        Graph graph = new Graph();

        moneyTransactions.forEach(it -> graph.addEdge(
                it.getPersonFrom().getName(),
                it.getPersonTo().getName(),
                it.getAmount()));

        moneyTransactions.forEach(it -> graph.addEdge(
                it.getPersonTo().getName(),
                it.getPersonFrom().getName(),
                BigDecimal.ZERO));

        DinitzSolver dinitzSolver = new DinitzSolver(graph);
        Graph resultGraph = dinitzSolver.solve();

        return getMoneyTransactionsFromGraph(resultGraph);
    }

    private List<MoneyTransaction> getMoneyTransactionsFromGraph(Graph resultGraph) {
        return resultGraph.getEdges().stream().map(
                it -> new MoneyTransaction(null, new Person(it.from), "", it.weight, new Person(it.to))
        ).collect(Collectors.toList());
    }


    static class Graph {
        private static final BigDecimal INF = new BigDecimal("9".repeat(9));

        private Map<String, List<Edge>> graphMap = new LinkedHashMap<>();
        private List<Edge> edges = new ArrayList<>();
        private Set<String> vertexes = new TreeSet<>();

        private String source;
        private String sink;
        boolean isSolved;
        private BigDecimal maxFlow = BigDecimal.ZERO;
        Map<String, Integer> levels;

        private String getSource() {
            return source;
        }

        private void setSource(String source) {
            this.source = source;
        }

        private String getSink() {
            return sink;
        }

        private void setSink(String sink) {
            this.sink = sink;
        }

        public BigDecimal getMaxFlow() {
            return maxFlow;
        }

        public void solve() {
            if (isSolved) {
                return;
            }
            isSolved = true;
            processSolve();
        }

        private void processSolve() {
            int[] next = new int[vertexes.size()];

            while (bfs()) {
                Arrays.fill(next, 0);
                // Find max flow by adding all augmenting path flows.
                for (BigDecimal f = dfs(source, next, INF); f.compareTo(BigDecimal.ZERO) != 0; f = dfs(source, next, INF)) {
                    maxFlow = maxFlow.add(f);
                }
            }
        }

        // Do a BFS from source to sink and compute the depth/level of each node
        // which is the minimum number of edges from that node to the source.
        private boolean bfs() {
            levels = new LinkedHashMap<>();

            levels.put(source, 0);
            Deque<String> q = new ArrayDeque<>();
            q.offer(source);
            while (!q.isEmpty()) {
                String node = q.poll();
                for (Edge edge : graphMap.get(node)) {
                    BigDecimal cap = edge.remainingCapacity();
                    if (cap.compareTo(BigDecimal.ZERO) > 0 && levels.get(edge.to) == null) {
                        levels.put(edge.to, Optional.ofNullable(levels.get(node)).orElse(-1) + 1);
                        q.offer(edge.to);
                    }
                }
            }
            return levels.get(sink) != null;
        }

        private BigDecimal dfs(String at, int[] next, BigDecimal flow) {
            if (at.equals(sink)) {
                return flow;
            }
            final int numEdges = graphMap.get(at).size();


            int atIdx = new ArrayList<>(vertexes).indexOf(at);

            for (; next[atIdx] < numEdges; next[atIdx]++) {
                Edge edge = graphMap.get(at).get(next[atIdx]);
                BigDecimal cap = edge.remainingCapacity();
                if (cap.compareTo(BigDecimal.ZERO) > 0 && levels.get(edge.to).equals(levels.get(at) + 1)) {
                    BigDecimal bottleNeck = dfs(edge.to, next, flow.min(cap));
                    if (bottleNeck.compareTo(BigDecimal.ZERO) > 0) {
                        edge.augment(bottleNeck);
                        return bottleNeck;
                    }
                }
            }
            return BigDecimal.ZERO;
        }

        /**
         * reduce loops (bidirectional edges) in resulting graph
         */
        public void reduceLoops() {
            Predicate<Edge> isDirected = (e) -> e.from.compareTo(e.to) > 0; // constant arbitrary order
            UnaryOperator<Edge> reverse = e -> new Edge(e.to, e.from, e.weight.negate());
            Function<Edge, List<String>> keyMapper = e -> Arrays.asList(e.from, e.to);
            // sum(capacity) group by keyMapper implementation
            Map<List<String>, BigDecimal> groupedMap = edges.stream()
                    .map(e -> isDirected.test(e) ? e : reverse.apply(e))
                    .collect(Collectors.toMap(
                            keyMapper,
                            e -> e.weight,
                            BigDecimal::add
                    ));
            // reverse negative edges
            Predicate<Edge> isNegative = (e) -> e.weight.compareTo(BigDecimal.ZERO) < 0;
            edges = groupedMap.entrySet().stream()
                    .map(it -> new Edge(it.getKey().get(0), it.getKey().get(1), it.getValue()))
                    .filter(it -> it.weight.compareTo(BigDecimal.ZERO) != 0)
                    .map(e -> isNegative.test(e) ? reverse.apply(e) : e)
                    .collect(Collectors.toList());
        }

        static class Edge {
            String from;
            String to;
            BigDecimal weight;
            Edge residual;

            BigDecimal flow = BigDecimal.ZERO;

            public Edge(String from, String to, BigDecimal weight) {
                this.from = from;
                this.to = to;
                this.weight = weight;
            }

            public static Edge createWithResidual(String from, String to, BigDecimal weight) {
                assert weight.compareTo(BigDecimal.ZERO) >= 0;
                assert !from.equals(to);

                Edge edge = new Edge(from, to, weight);
                Edge edgeResidual = new Edge(to, from, BigDecimal.ZERO);
                edge.residual = edgeResidual;
                edgeResidual.residual = edge;
                return edge;
            }


            public BigDecimal remainingCapacity() {
                return weight.subtract(flow);
            }

            public void augment(BigDecimal bottleNeck) {
                flow = flow.add(bottleNeck);
                residual.flow = flow.subtract(bottleNeck);
            }

            public String getHashString() {
                return String.format("from %s to %s", from, to);
            }
        }

        void addEdge(String from, String to, BigDecimal weight) {
            Stream.of(from, to).forEach(vertexes::add);
            Edge edge = Edge.createWithResidual(from, to, weight);
            graphMap.computeIfAbsent(from, (key) -> new ArrayList<>()).add(edge);
            graphMap.computeIfAbsent(to, (key) -> new ArrayList<>()).add(edge.residual);
            edges.add(edge);
        }

        public List<Edge> getEdges() {
            return edges;
        }

        public Map<String, List<Edge>> getGraphMap() {
            return graphMap;
        }
    }

    static class DinitzSolver {
        private Graph graph;

        public DinitzSolver(Graph graph) {
            this.graph = graph;
        }

        private void setGraph(Graph graph) {
            this.graph = graph;
        }

        public Graph solve() {
            Set<String> visitedEdges = new HashSet<>();
            while (true) {
                Graph.Edge firstEdge = graph.edges.stream().filter(it -> !visitedEdges.contains(it.getHashString()))
                        .reduce((a, b) -> b) //get last
                        .orElse(null);
                if (firstEdge == null) {
                    break;
                }
                graph.isSolved = false;
                graph.setSource(firstEdge.from);
                graph.setSink(firstEdge.to);
                graph.solve();
                Map<String, List<Graph.Edge>> graphMap = graph.getGraphMap();
                List<Graph.Edge> newEdges = graphMap.entrySet().stream().flatMap(it -> it.getValue().stream())
                        .map(edge -> {
                            BigDecimal remainFlow = edge.flow.compareTo(BigDecimal.ZERO) < 0
                                    ? edge.weight
                                    : edge.weight.subtract(edge.flow);
                            return remainFlow.compareTo(BigDecimal.ZERO) > 0
                                    ? new Graph.Edge(edge.from, edge.to, remainFlow)
                                    : null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                graph.solve();
                BigDecimal maxFlow = graph.getMaxFlow();
                String source = graph.getSource();
                String sink = graph.getSink();
                visitedEdges.add(new Graph.Edge(source, sink, null).getHashString());

                Graph newGraph = new Graph();
                newEdges.forEach(e -> newGraph.addEdge(e.from, e.to, e.weight));
                newGraph.addEdge(source, sink, maxFlow);
                setGraph(newGraph);
            }
            graph.reduceLoops();
            return graph;
        }
    }

}
