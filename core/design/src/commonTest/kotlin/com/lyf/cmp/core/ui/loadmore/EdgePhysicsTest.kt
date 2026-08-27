package com.lyf.cmp.core.ui.loadmore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EdgePhysicsTest {

    @Test
    fun dragAccumulatesWithDamping() {
        val result = EdgePhysics.drag(offsetPx = -10f, deltaPx = -20f, maxOffsetPx = 100f, damping = 0.5f)
        assertEquals(-20f, result.offsetPx)
        // 全量消耗：位移增量 (-20 - (-10)) 折回阻尼前的 delta。
        assertEquals(-20f, result.consumedDeltaPx)
    }

    @Test
    fun dragClampsAtMaxOffsetAndConsumesPartially() {
        val result = EdgePhysics.drag(offsetPx = -95f, deltaPx = -20f, maxOffsetPx = 100f, damping = 0.5f)
        assertEquals(-100f, result.offsetPx)
        // 夹紧后只消耗了 5px 位移对应的 delta。
        assertEquals(-10f, result.consumedDeltaPx)
    }

    @Test
    fun dragNeverProducesPositiveOffset() {
        val result = EdgePhysics.drag(offsetPx = -30f, deltaPx = 40f, maxOffsetPx = 100f, damping = 0.5f)
        // 反向 delta 不属于 drag 的职责（由 collapse 处理），但也不允许越界为正。
        assertTrue(result.offsetPx <= 0f)
    }

    @Test
    fun collapseCollapsesOneToOne() {
        val result = EdgePhysics.collapse(offsetPx = -30f, deltaPx = 10f)
        assertEquals(-20f, result.offsetPx)
        assertEquals(10f, result.consumedDeltaPx)
    }

    @Test
    fun collapseStopsAtZero() {
        val result = EdgePhysics.collapse(offsetPx = -30f, deltaPx = 50f)
        assertEquals(0f, result.offsetPx)
        assertEquals(30f, result.consumedDeltaPx)
    }

    @Test
    fun kickConvertsVelocityClamped() {
        val clamped = EdgePhysics.kick(offsetPx = 0f, velocityPx = -10_000f, maxOffsetPx = 100f, factor = 0.05f)
        assertEquals(-100f, clamped)
        val normal = EdgePhysics.kick(offsetPx = 0f, velocityPx = -1_000f, maxOffsetPx = 100f, factor = 0.05f)
        assertEquals(-50f, normal)
        // 正向速度不产生末端回弹。
        assertEquals(0f, EdgePhysics.kick(offsetPx = 0f, velocityPx = 1_000f, maxOffsetPx = 100f, factor = 0.05f))
    }
}
