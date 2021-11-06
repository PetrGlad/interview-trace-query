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
        val traces = lines.flatMap(::lineToTraces)
        return mutableMapOf<NodeKey, MutableMap<NodeKey, Trace>>()
            .withDefault { mutableMapOf() }
            .apply {
                for (t in traces) {
                    val m = getValue(t.from)
                    m[t.to] = t
                    put(t.from, m)
                }
            }
    }

data class TracePath(
    val prev: TracePath?,
    val node: NodeKey,
    val depth: Int,
    val cost: Cost
) {
    fun append(path: TracePath, trace: Trace): TracePath {
        assert(path.node == trace.from)
        assert(trace.cost > 0)
        return TracePath(path, trace.to, path.depth + 1, path.cost + trace.cost)
    }

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

fun emptyTracePath(startNode: NodeKey) = TracePath(null, startNode, 0, 0);

sealed class TraceResult<R>
class NoAnswer<R> : TraceResult<R>()
class QueryResult<R>(val result: R) : TraceResult<R>()
class Continue<R> : TraceResult<R>()

fun <R> traverse(
    handleTrace: (depth: Int, t: Trace) -> TraceResult<R>,
    traces: TraceGraph,
    depth: Int
) {
    assert(depth >= 0)
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
