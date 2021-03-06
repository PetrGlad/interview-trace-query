package interview.tracequery

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.fail
import java.io.StringReader

internal class MainKtTest {

    @Test
    fun testLineToTraces() {
        assertEquals(
            listOf(
                Trace('A', 'B', 5),
                Trace('B', 'C', 4),
                Trace('C', 'D', 8),
                Trace('D', 'C', 891),
                Trace('E', 'B', 31),
                Trace('A', 'E', 7),
            ),
            lineToTraces(
                """AB5, BC4, CD8
                    DC891, 
                    EB31, AE7"""
            ).toList()
        )
    }

    private val sampleGraph = mapOf(
        'A' to mapOf(
            'B' to Trace('A', 'B', 5),
            'D' to Trace('A', 'D', 6),
            'E' to Trace('A', 'E', 7)
        ),
        'B' to mapOf(
            'C' to Trace('B', 'C', 4)
        ),
        'C' to mapOf(
            'D' to Trace('C', 'D', 8)
        ),
        'D' to mapOf(
            'C' to Trace('D', 'C', 4)
        ),
        'E' to mapOf()
    )

    @Test
    fun testLoadTraceGraph() {
        assertEquals(
            sampleGraph,
            loadTraceGraph(
                StringReader(
                    """AB5, BC4, CD8
                    DC4, 
                    AD6, AE7"""
                ).buffered()
            )
        )
    }

    @Test
    fun testTracePath() {
        val root = newTracePath('W')
        assertEquals(0, root.cost)
        assertNull(root.prev)
        val path1 = root.append(Trace('W', 'E', 23))
        val path2 = path1.append(Trace('E', 'B', 51))
        assertEquals(23, path1.cost)
        assertEquals(74, path2.cost)
        assertEquals(listOf('W', 'E', 'B'), path2.toNodePath())
    }

    private val exampleMultipathTraces = loadTraceGraph(
        "AB2,AC3,BE5,BD7,CD11,CE13,DF17,EF19,FA23,BZ29"
            .reader().buffered()
    )

    @Test
    fun testWalkTraces() {
        val paths = mutableSetOf<TracePath>()
        walkTraces(exampleMultipathTraces, 'A') { t ->
            if (t.depth > 5) false
            else {
                paths.add(t)
                true
            }
        }
        assertEquals(
            setOf(
                listOf('A', 'B'),
                listOf('A', 'C'),
                listOf('A', 'B', 'E'),
                listOf('A', 'B', 'D'),
                listOf('A', 'B', 'Z'),
                listOf('A', 'C', 'D'),
                listOf('A', 'C', 'E'),
                listOf('A', 'B', 'E', 'F'),
                listOf('A', 'B', 'D', 'F'),
                listOf('A', 'C', 'D', 'F'),
                listOf('A', 'C', 'E', 'F'),
                listOf('A', 'B', 'E', 'F', 'A'),
                listOf('A', 'B', 'D', 'F', 'A'),
                listOf('A', 'C', 'D', 'F', 'A'),
                listOf('A', 'C', 'E', 'F', 'A'),
                listOf('A', 'B', 'E', 'F', 'A', 'B'),
                listOf('A', 'B', 'E', 'F', 'A', 'C'),
                listOf('A', 'B', 'D', 'F', 'A', 'B'),
                listOf('A', 'B', 'D', 'F', 'A', 'C'),
                listOf('A', 'C', 'D', 'F', 'A', 'B'),
                listOf('A', 'C', 'D', 'F', 'A', 'C'),
                listOf('A', 'C', 'E', 'F', 'A', 'B'),
                listOf('A', 'C', 'E', 'F', 'A', 'C')
            ),
            paths.map(TracePath::toNodePath).toSet()
        )
    }

    @Test
    fun testWalkTraces_empty() {
        walkTraces(mapOf(), 'R') {
            fail("Shoulid not be invoked.")
        }
    }

    @Test
    fun testCheapestPathCost() {
        fun testCost(from: NodeKey, to: NodeKey, expected: Cost?) {
            if (expected == null)
                assertNull(cheapestPathCost(exampleMultipathTraces, from, to))
            else
                assertEquals(expected, cheapestPathCost(exampleMultipathTraces, from, to))
        }

        // Unreachable paths
        for (n in listOf('A', 'B', 'C', 'D', 'E', 'F')) {
            testCost('Z', n, null)
        }

        testCost('A', 'B', 2)
        testCost('A', 'C', 3)
        testCost('A', 'D', 9)
        testCost('A', 'E', 7)
        testCost('A', 'F', 26)

        testCost('D', 'A', 40)
        testCost('D', 'B', 42)
        testCost('D', 'C', 43)
        testCost('D', 'E', 47)
        testCost('D', 'F', 17)
    }

    @Test
    fun testPathCost() {
        assertEquals(0, pathCost(sampleGraph, listOf()))
        assertNull(pathCost(sampleGraph, listOf('Z')))
        assertEquals(5, pathCost(sampleGraph, listOf('A', 'B')))
        assertNull(pathCost(sampleGraph, listOf('A', 'B', 'D')))
        assertEquals(16, pathCost(sampleGraph, listOf('B', 'C', 'D', 'C')))
    }

    @Test
    fun testTaskExample() {
        val taskExampleInput = """AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7"""
            .reader().buffered()
        val traces = loadTraceGraph(taskExampleInput)
        assertEquals(9, pathCost(traces, listOf('A', 'B', 'C')))
        assertEquals(5, pathCost(traces, listOf('A', 'D')))
        assertEquals(13, pathCost(traces, listOf('A', 'D', 'C')))
        assertEquals(22, pathCost(traces, listOf('A', 'E', 'B', 'C', 'D')))
        assertNull(pathCost(traces, listOf('A', 'E', 'D')))
        assertEquals(2, countCtoCTracesWithDepthTo3(traces))
        assertEquals(3, countAtoCTracesWithDepth4(traces))
        assertEquals(9, cheapestPathCost(traces, 'A', 'C'))
        assertEquals(9, cheapestPathCost(traces, 'B', 'B'))
        assertEquals(9, cheapestPathCost(traces, 'E', 'E')) // Extra
        assertNull(cheapestPathCost(traces, 'C', 'A')) // Extra
        assertEquals(7, countCtoCTracesWithCostLess30(traces))
    }

}
