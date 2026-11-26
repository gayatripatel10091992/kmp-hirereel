package com.gayatripatel.hirereel.shared

/** What the video feed should do with its players after the user lands on a page. */
data class PoolPlan(
    /** Pages that should hold a prepared (buffering or ready) player. */
    val prepare: Set<Int>,
    /** Pages whose players must be released to free decoder and buffer memory. */
    val release: Set<Int>,
    /** The single page allowed to play, or null while the user is mid-swipe. */
    val play: Int?,
)

/**
 * Keeps at most `1 + 2 * lookAround` ExoPlayer instances alive in a vertical video feed.
 *
 * Every prepared player holds a hardware decoder and a few MB of buffer, so a naive
 * "one player per page" feed runs out of decoders or memory on low-end phones. The current page
 * plays, its neighbours are prepared so the swipe starts instantly, everything else is released.
 */
object PlayerPoolPolicy {

    fun plan(
        currentPage: Int,
        pageCount: Int,
        alreadyPrepared: Set<Int>,
        isScrolling: Boolean,
        lookAround: Int = 1,
    ): PoolPlan {
        if (pageCount <= 0) return PoolPlan(emptySet(), alreadyPrepared, null)
        val current = currentPage.coerceIn(0, pageCount - 1)
        val wanted = ((current - lookAround)..(current + lookAround)).filter { it in 0 until pageCount }.toSet()
        return PoolPlan(
            prepare = wanted - alreadyPrepared,
            release = alreadyPrepared - wanted,
            play = if (isScrolling) null else current,
        )
    }
}
