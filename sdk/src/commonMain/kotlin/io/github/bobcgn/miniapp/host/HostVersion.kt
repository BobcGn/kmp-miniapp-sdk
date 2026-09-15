package io.github.bobcgn.miniapp.host

/**
 * A host runtime version that is compared by numeric segment.
 *
 * Version strings must never be compared as text: `"2.20.1"` sorts before
 * `"2.3.0"` character by character even though it is the later release. A host
 * reports its version in whatever form its runtime uses, so only numeric dotted
 * segments are understood here. Anything else is undeterminable rather than
 * guessed, because a wrong comparison would silently misreport support.
 *
 * Trailing zero segments are insignificant, so `1.0` and `1.0.0` denote the same
 * version. [toString] keeps the text the host reported so diagnostics show what
 * was actually read.
 *
 * @property text the version exactly as the host reported it
 */
public class HostVersion private constructor(
    private val segments: List<Int>,
    private val text: String,
) : Comparable<HostVersion> {
    override fun compareTo(other: HostVersion): Int {
        val length = maxOf(segments.size, other.segments.size)
        for (index in 0 until length) {
            val mine = segments.getOrElse(index) { 0 }
            val theirs = other.segments.getOrElse(index) { 0 }
            if (mine != theirs) return mine.compareTo(theirs)
        }
        return 0
    }

    override fun equals(other: Any?): Boolean =
        this === other || (other is HostVersion && segments == other.segments)

    override fun hashCode(): Int = segments.hashCode()

    override fun toString(): String = text

    public companion object {
        /**
         * Parses a dotted numeric version.
         *
         * @return the version, or `null` when [raw] is not a sequence of numeric
         *   dot-separated segments
         */
        public fun parse(raw: String): HostVersion? {
            val parts = raw.trim().split('.')
            if (parts.isEmpty()) return null

            val parsed = ArrayList<Int>(parts.size)
            for (part in parts) {
                val segment = part.toIntOrNull() ?: return null
                if (segment < 0) return null
                parsed += segment
            }

            // Trailing zeros carry no meaning, so drop them to keep equality and
            // ordering consistent for 1.0 and 1.0.0.
            while (parsed.size > 1 && parsed.last() == 0) {
                parsed.removeAt(parsed.lastIndex)
            }

            return HostVersion(segments = parsed, text = raw.trim())
        }
    }
}
