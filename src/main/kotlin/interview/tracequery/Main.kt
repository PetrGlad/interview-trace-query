package interview.tracequery

import java.io.BufferedReader

typealias NodeKey = Char

typealias Cost = Long

data class Trace(val from: NodeKey, val to: NodeKey, val cost: Cost)

typealias TraceGraph = Map<NodeKey, Map<NodeKey, Trace>>

typealias NodePath = List<NodeKey>

val tracePattern = Regex("([A-Z])([A-Z])(\\d+)")

fun lineToTraces(line: String): Sequence<Trace> =
    tracePattern.findAll(line)
        .map { match ->
            Trace(
                match.groups[1]!!.value[0],
                match.groups[2]!!.value[0],
                match.groups[3]!!.value.toLong()
            )
        }

fun loadTraceGraph(reader: BufferedReader): TraceGraph =
    reader.useLines { lines ->
        mutableMapOf<NodeKey, MutableMap<NodeKey, Trace>>()
            .withDefault { mutableMapOf() }
            .apply {
                for (t in lines.flatMap(::lineToTraces)) {
                    val m = getValue(t.from)
                    assert(!m.containsKey(t.to)) // No duplicate links (e.g. ABx ABy) allowed
                    m[t.to] = t
                    put(t.from, m)
                    // Ensure nodes without outgoing links are also available:
                    put(t.to, getValue(t.to))
                }
            }
    }

data class TracePath(
    val prev: TracePath?,
    val node: NodeKey,
    val depth: Int,
    val cost: Cost
) {
    fun append(trace: Trace): TracePath {
        assert(node == trace.from)
        assert(trace.cost > 0) // Otherwise, cost-dependent procedures are not guaranteed to stop.
        return TracePath(this, trace.to, depth + 1, cost + trace.cost)
    }

    /**
     * Can be used for debugging output like println(path.toNodePath())
     */
    fun toNodePath(): NodePath {
        val nodes = arrayListOf<NodeKey>()
        var here: TracePath? = this;
        while (here != null) {
            nodes.add(here.node)
            here = here.prev
        }
        nodes.reverse()
        return nodes
    }
}

fun newTracePath(startNode: NodeKey) = TracePath(null, startNode, 0, 0)

/**
 * Traverse all possible paths that start from specified node.
 *
 * @param handlePath Gets every path encountered except root.
 *        Must return false if the path should not be followed any further.
 */
fun walkTraces(
    traces: TraceGraph,
    fromNode: NodeKey,
    handlePath: (path: TracePath) -> Boolean
) {
    val queue = ArrayDeque<TracePath>()
    queue.add(newTracePath(fromNode))
    while (true) {
        val here = queue.removeFirstOrNull() ?: break
        val fromHere = traces[here.node] ?: break
        for (p in fromHere.values.map(here::append)) {
            if (handlePath(p))
                queue.add(p);
        }
    }
}

/**
 * Count all paths from node C which have not more than 3 connections.
 */
fun countCtoCTracesWithMaxDepthLimit(traces: TraceGraph, maxDepth: Int = 3): Int {
    var pathCount = 0
    walkTraces(traces, 'C') { path ->
        if (path.depth > maxDepth) {
            false
        } else {
            if (path.node == 'C') pathCount++
            true
        }
    }
    return pathCount
}

/**
 * Count all paths from node C which have 4 edges.
 */
fun countAtoCTracesWithDepth4(traces: TraceGraph, depth: Int = 4): Int {
    var pathCount = 0
    walkTraces(traces, 'A') { path ->
        if (path.depth == depth) {
            if (path.node == 'C') pathCount++
            false
        } else {
            true
        }
    }
    return pathCount
}

/**
 * Count all paths from node C which cost less than specified.
 */
fun countCtoCTracesWithLimitedCost(traces: TraceGraph, costLimit: Cost = 30): Int {
    var pathCount = 0
    walkTraces(traces, 'C') { path ->
        if (path.cost >= costLimit) {
            false
        } else {
            if (path.node == 'C')
                pathCount++
            true
        }
    }
    return pathCount
}

/**
 * Find cost of cheapest non-empty path between specified nodes.
 */
fun cheapestPathCost(traces: TraceGraph, fromNode: NodeKey, toNode: NodeKey): Cost? {
    var minCost: Cost? = null
    val visited = mutableMapOf<NodeKey, TracePath>()
    walkTraces(traces, fromNode) { path ->
        if ((visited[path.node]?.cost ?: Cost.MAX_VALUE) < path.cost) {
            // It is a stopping condition in case no path to target has been found yet
            // (we may be going in cycles without it).
            false
        } else {
            visited[path.node] = path
            if (minCost != null && minCost!! <= path.cost)
                // An optimization: the path is already known to be too expensive
                false
            else if (path.node == toNode) {
                minCost = path.cost
                false
            } else
                true
        }
    }
    return minCost
}

/**
 * Get cost of specified path.
 */
fun pathCost(traces: TraceGraph, path: NodePath): Cost? {
    if (path.isEmpty()) return 0
    if (path.size == 1) {
        if (traces[path.first()] == null)
            return null
        else
            return 0
    }
    var cost = 0L
    for ((a, b) in path.zipWithNext()) {
        val c = traces[a]?.get(b)?.cost
        if (c == null) return null;
        cost += c
    }
    return cost
}

fun <T> resultMsg(x: T?) = x?.toString() ?: "NO SUCH TRACE"

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val traces = loadTraceGraph(System.`in`.bufferedReader())
        assert(traces.isNotEmpty())
        println(resultMsg(pathCost(traces, listOf('A', 'B', 'C'))))
        println(resultMsg(pathCost(traces, listOf('A', 'D'))))
        println(resultMsg(pathCost(traces, listOf('A', 'D', 'C'))))
        println(resultMsg(pathCost(traces, listOf('A', 'E', 'B', 'C', 'D'))))
        println(resultMsg(pathCost(traces, listOf('A', 'E', 'D'))))
        println(countCtoCTracesWithMaxDepthLimit(traces))
        println(countAtoCTracesWithDepth4(traces))
        println(resultMsg(cheapestPathCost(traces, 'A', 'C')))
        println(resultMsg(cheapestPathCost(traces, 'B', 'B')))
        println(countCtoCTracesWithLimitedCost(traces))
    }
}
