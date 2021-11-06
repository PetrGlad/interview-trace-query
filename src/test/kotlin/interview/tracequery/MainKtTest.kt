package interview.tracequery

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.StringReader

internal class MainKtTest {

    @Test
    fun testLineToTraces() {
        assertEquals(
            listOf(
                Trace('A', 'B', 5),
                Trace('B', 'C', 4),
                Trace('C', 'D', 8),
                Trace('D', 'C', 8),
                Trace('E', 'B', 3),
                Trace('A', 'E', 7),
            ),
            lineToTraces(
                """AB5, BC4, CD8
                    DC8, 
                    EB3, AE7"""
            ).toList()
        )
    }

    val sampleGraph = mapOf(
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

        // TODO To be implemented

//        6. The number of traces originating in service C and ending in service C with a maximum of
//        3 hops. In the sample data below there are two such traces: C-D-C (2 stops) and
//        C-E-B-C (3 stops).
//        7. The number of traces originating in A and ending in C with exactly 4 hops. In the sample
//        data below there are three such traces: A to C (via B, C, D); A to C (via D, C, D); and A
//                to C (via D, E, B).
//        8. The length of the shortest trace (in terms of latency) between A and C.
//        9. The length of the shortest trace (in terms of latency) between B and B.
//
//        10. The number of different traces from C to C with an average latency of less than 30. In
//        the same data, the traces are C-D-C, C-E-B-C, C-E-B-C-D-C, C-D-C-E-B-C, C-D-E-B-C,
//        C-E-B-C-E-B-C, C-E-B-C-E-B-C-E-B-C.

//        6. 2
//        7. 3
//        8. 9
//        9. 9
//        10. 7
    }

}
