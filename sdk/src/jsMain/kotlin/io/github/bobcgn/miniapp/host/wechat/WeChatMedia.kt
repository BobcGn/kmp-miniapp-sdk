package io.github.bobcgn.miniapp.host.wechat

/**
 * A media type `wx.chooseMedia` can be asked for.
 *
 * `mix` asks for images and videos together, which is also what leaving the option
 * unset does; the SDK asks for one of these explicitly, because the host's own
 * default when the option is absent is not a value it documents.
 */
internal enum class WeChatMediaType(
    /** The value [io.github.bobcgn.miniapp.host.wechat.interop.wx] expects. */
    internal val hostValue: String,
) {
    /** Still images only. */
    IMAGE("image"),

    /** Videos only. */
    VIDEO("video"),

    /** Images and videos together. */
    MIX("mix"),
}

/**
 * Where `wx.chooseMedia` may take media from.
 *
 * The album is the host's own picker, which is why reading media needs no
 * permission the SDK could map: nothing in the host contract ties a scope to it.
 */
internal enum class WeChatMediaSource(
    /** The value [io.github.bobcgn.miniapp.host.wechat.interop.wx] expects. */
    internal val hostValue: String,
) {
    /** The device's photo library. */
    ALBUM("album"),

    /** The device's camera. */
    CAMERA("camera"),
}

/**
 * How much the host may compress an image it returns.
 *
 * WeChat documents this as effective only when the media type is an image, and it
 * does not restrict videos at all.
 */
internal enum class WeChatMediaSizeType(
    /** The value [io.github.bobcgn.miniapp.host.wechat.interop.wx] expects. */
    internal val hostValue: String,
) {
    /** The image as it is stored on the device. */
    ORIGINAL("original"),

    /** The image the host compressed. */
    COMPRESSED("compressed"),
}

/**
 * Which camera the host may use.
 *
 * WeChat documents this as effective only when the camera is among the requested
 * sources; asking for it without one is not an error, it simply has no effect.
 */
internal enum class WeChatCameraPosition(
    /** The value [io.github.bobcgn.miniapp.host.wechat.interop.wx] expects. */
    internal val hostValue: String,
) {
    /** The camera facing away from the user. */
    BACK("back"),

    /** The camera facing the user. */
    FRONT("front"),
}

/**
 * The kind of file the host reported.
 *
 * The host's vocabulary is open in the same way its scan formats are: a base
 * library could name a kind this SDK has never seen. [fromHostValue] therefore
 * answers `null` for anything unrecognized, and the host's own name is carried on
 * [WeChatMediaFile.hostFileType] regardless, so nothing the host actually said is
 * lost.
 */
internal enum class WeChatMediaFileType(
    /** The host's own name for this kind of file. */
    internal val hostValue: String,
) {
    /** A still image. */
    IMAGE("image"),

    /** A video. */
    VIDEO("video"),
    ;

    internal companion object {
        /** Returns the kind [hostValue] names, or `null` when it names none. */
        internal fun fromHostValue(hostValue: String): WeChatMediaFileType? =
            entries.firstOrNull { it.hostValue == hostValue }
    }
}

/**
 * What one media selection asks the host for.
 *
 * The options that exist but are documented as not always applying are still
 * modelled, because a caller who asks for them should get what the host does with
 * them rather than a SDK-invented interpretation.
 *
 * The SDK validates what it can state exactly and refuses what it cannot: it never
 * quietly corrects a caller's input into something else. A count, a duration, or a
 * media type the host could not accept is a caller mistake and is rejected here
 * instead of being sent and failing somewhere less obvious.
 *
 * @property mediaType the media types to ask for, with duplicates removed and the
 *   caller's order preserved; the host documents this option as required, so it
 *   must not be empty
 * @property count maximum number of files to ask for; the host applies its own
 *   limit and may return fewer than requested
 * @property sourceType where the host may take media from, with duplicates
 *   removed; empty means no restriction, which is not the same as asking for none
 * @property maxDurationSeconds longest video recording to ask for, in seconds, or
 *   `null` to leave the host's own default; WeChat documents the range as 3 to 60
 *   and states that it does not restrict album selection
 * @property sizeType how much the host may compress the images it returns, with
 *   duplicates removed; empty means no restriction. WeChat documents this as
 *   effective only for images
 * @property camera which camera to use, or `null` to leave it to the host; WeChat
 *   documents this as effective only when the camera is among [sourceType]
 */
internal class WeChatMediaRequest(
    mediaType: List<WeChatMediaType>,
    internal val count: Int = 1,
    sourceType: List<WeChatMediaSource> = emptyList(),
    internal val maxDurationSeconds: Int? = null,
    sizeType: List<WeChatMediaSizeType> = emptyList(),
    internal val camera: WeChatCameraPosition? = null,
) {
    internal val mediaType: List<WeChatMediaType> = mediaType.distinct()
    internal val sourceType: List<WeChatMediaSource> = sourceType.distinct()
    internal val sizeType: List<WeChatMediaSizeType> = sizeType.distinct()

    init {
        require(mediaType.isNotEmpty()) {
            "chooseMedia requires at least one media type, because WeChat documents the option as required"
        }
        require(count >= 1) {
            "chooseMedia count must be at least 1, but was $count"
        }
        if (maxDurationSeconds != null) {
            require(maxDurationSeconds in MIN_MAX_DURATION_SECONDS..MAX_MAX_DURATION_SECONDS) {
                "chooseMedia maxDurationSeconds must be between $MIN_MAX_DURATION_SECONDS and " +
                    "$MAX_MAX_DURATION_SECONDS, but was $maxDurationSeconds"
            }
        }
    }

    private companion object {
        /** Shortest video recording WeChat's own schema documents. */
        const val MIN_MAX_DURATION_SECONDS: Int = 3

        /** Longest video recording WeChat's own schema documents. */
        const val MAX_MAX_DURATION_SECONDS: Int = 60
    }
}

/**
 * One file the host returned.
 *
 * The path is a host temporary resource, not a durable one: it belongs to the
 * current session and nothing in the contract promises it survives. A caller that
 * needs the media later has to copy it somewhere it owns, and the SDK does not do
 * that on the caller's behalf.
 *
 * Nothing here is logged, cached, or uploaded by the SDK. What the user selected
 * is theirs, and a caller that keeps it takes on the responsibility that comes
 * with it.
 *
 * @property tempFilePath the host's temporary path to the file
 * @property sizeBytes the file's size in bytes
 * @property fileType the kind the host named, or `null` when it named one this SDK
 *   does not know; [hostFileType] preserves that unknown name
 * @property hostFileType the required kind name the host reported, verbatim
 * @property durationSeconds a video's duration, or `null` when the host reported
 *   none, which is what an image reports
 * @property width a video's width in pixels, or `null` when the host reported none
 * @property height a video's height in pixels, or `null` when the host reported none
 * @property thumbTempFilePath a temporary path to a video's thumbnail, or `null`
 *   when the host reported none
 */
internal class WeChatMediaFile(
    internal val tempFilePath: String,
    internal val sizeBytes: Long,
    internal val fileType: WeChatMediaFileType?,
    internal val hostFileType: String,
    internal val durationSeconds: Double?,
    internal val width: Double?,
    internal val height: Double?,
    internal val thumbTempFilePath: String?,
)
