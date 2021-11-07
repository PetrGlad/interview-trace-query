package interview.tracequery

import java.io.BufferedReader

typealias NodeKey = Char
typealias Cost = Long

data class Trace(val from: NodeKey, val to: NodeKey, val cost: Cost)

typealias TraceGraph = Map<NodeKey, Map<NodeKey, Trace>>

typealias NodePath = List<NodeKey>

val tracePattern = Regex("([A-Z])([A-Z])(\\d)")

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
                    m[t.to] = t
                    put(t.from, m)
                    // Also keep terminating nodes for convenience
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
     * Can be used for debugging output like
     * println(path.toNodePath())
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
 * @param handlePath Gets every path encountered. Must return false if the path should not be followed any further.
 */
fun walkTraces(
    traces: TraceGraph,
    fromNode: NodeKey,
    handlePath: (path: TracePath) -> Boolean
) {
    assert(traces.containsKey(fromNode))
    val queue = ArrayDeque<TracePath>()
    queue.add(newTracePath(fromNode))
    while (true) {
        val here = queue.removeFirstOrNull() ?: break
        for (p in traces[here.node]!!.values.map(here::append)) {
            if (handlePath(p))
                queue.add(p);
        }
    }
}

fun countCtoCTracesWithDepthTo3(traces: TraceGraph): Int {
    var pathCount = 0
    walkTraces(traces, 'C') { path ->
        if (path.depth > 3) {
            false
        } else {
            if (path.node == 'C') pathCount++
            true
        }
    }
    return pathCount
}

fun countAtoCTracesWithDepth4(traces: TraceGraph): Int {
    var pathCount = 0
    walkTraces(traces, 'A') { path ->
        if (path.depth == 4) {
            if (path.node == 'C') pathCount++
            false
        } else {
            true
        }
    }
    return pathCount
}

fun countCtoCTracesWithCostLess30(traces: TraceGraph): Int {
    var pathCount = 0
    walkTraces(traces, 'C') { path ->
        if (path.cost >= 30) {
            false
        } else {
            if (path.node == 'C') pathCount++
            true
        }
    }
    return pathCount
}

fun shortestPathCost(traces: TraceGraph, fromNode: NodeKey, toNode: NodeKey): Cost? {
    var minCost: Cost? = null
    val visited = mutableSetOf<NodeKey>()
    walkTraces(traces, fromNode) { path ->
        /*if (minCost != null && minCost!! <= path.cost) // An optimization
            false
        else */if (visited.contains(path.node)) {
            // Any loop is guaranteed to cost more than the shortcut.
            // It is also a stopping condition in case no path to target has been found yet
            // (we may be going in cycles without it).
            false
        } else {
            visited.add(path.node)
            if ((minCost == null || minCost!! > path.cost)
                && path.node == toNode
            ) {
                minCost = path.cost
                false
            } else
                true
        }
    }
    return minCost
}


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

fun <T> maybeResultMsg(x: T?) = x?.toString() ?: "NO SUCH TRACE"

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        // val exampleInput = System.`in`.bufferedReader()
        val exampleInput = """AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"""
            .reader().buffered()
        val traces = loadTraceGraph(exampleInput)
        println(traces)
        println(maybeResultMsg(pathCost(traces, listOf('A', 'A', 'A', 'A', 'A'))))
    }
}
