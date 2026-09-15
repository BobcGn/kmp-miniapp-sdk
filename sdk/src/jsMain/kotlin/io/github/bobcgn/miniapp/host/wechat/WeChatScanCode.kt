package io.github.bobcgn.miniapp.host.wechat

/**
 * A scan category `wx.scanCode` can be asked for.
 *
 * These are the four coarse categories the request option accepts. They are not
 * what the host answers with: a completed scan names the specific format it
 * decoded, which is [WeChatScanFormat]. The two vocabularies are different, and
 * conflating them is a mistake the types are named to prevent.
 */
internal enum class WeChatScanCategory(
    /** The value [io.github.bobcgn.miniapp.host.wechat.interop.wx] expects. */
    internal val hostValue: String,
) {
    /** One-dimensional barcodes, such as EAN and UPC. */
    BAR_CODE("barCode"),

    /** QR codes. */
    QR_CODE("qrCode"),

    /** Data Matrix codes. */
    DATA_MATRIX("datamatrix"),

    /** PDF417 codes. */
    PDF_417("pdf417"),
}

/**
 * The format the host reported having decoded.
 *
 * The host's vocabulary is open: a base library may name a format this SDK has
 * never seen, and a scan must not fail because of that. [fromHostValue] therefore
 * answers `null` for anything unrecognized, and the host's own name is carried on
 * [WeChatScanResult.scanType] regardless, so nothing the host actually said is
 * lost.
 *
 * The names are the host's, not this SDK's, and are passed through unchanged.
 */
internal enum class WeChatScanFormat(
    /** The host's own name for this format. */
    internal val hostValue: String,
) {
    /** QR code. */
    QR_CODE("QR_CODE"),

    /** Aztec code. */
    AZTEC("AZTEC"),

    /** Codabar. */
    CODABAR("CODABAR"),

    /** Code 39. */
    CODE_39("CODE_39"),

    /** Code 93. */
    CODE_93("CODE_93"),

    /** Code 128. */
    CODE_128("CODE_128"),

    /** Data Matrix. */
    DATA_MATRIX("DATA_MATRIX"),

    /** EAN-8. */
    EAN_8("EAN_8"),

    /** EAN-13. */
    EAN_13("EAN_13"),

    /** Interleaved 2 of 5. */
    ITF("ITF"),

    /** MaxiCode. */
    MAXICODE("MAXICODE"),

    /** PDF417. */
    PDF_417("PDF_417"),

    /** RSS-14. */
    RSS_14("RSS_14"),

    /** RSS Expanded. */
    RSS_EXPANDED("RSS_EXPANDED"),

    /** UPC-A. */
    UPC_A("UPC_A"),

    /** UPC-E. */
    UPC_E("UPC_E"),

    /** A UPC or EAN extension. */
    UPC_EAN_EXTENSION("UPC_EAN_EXTENSION"),

    /** A WeChat mini program code, which only this host produces. */
    WX_CODE("WX_CODE"),

    /** Code 25. */
    CODE_25("CODE_25"),
    ;

    internal companion object {
        /** Returns the format [hostValue] names, or `null` when it names none. */
        internal fun fromHostValue(hostValue: String): WeChatScanFormat? =
            entries.firstOrNull { it.hostValue == hostValue }
    }
}

/**
 * What one scan asks the host for.
 *
 * Only the options the SDK has a reason to expose are modelled: whether the host
 * may scan from anywhere other than its camera, and which categories it may
 * report. WeChat's own scanning interface, and everything about how it looks, is
 * the host's business and is not modelled here.
 *
 * @property onlyFromCamera when true the host must scan through its camera; when
 *   false it may also offer an image the user already has
 * @property allowedCategories categories to ask for, with duplicates removed and
 *   the caller's order preserved; empty means "every category the host supports",
 *   which is not the same as asking for none
 */
internal class WeChatScanRequest(
    internal val onlyFromCamera: Boolean = false,
    allowedCategories: List<WeChatScanCategory> = emptyList(),
) {
    internal val allowedCategories: List<WeChatScanCategory> = allowedCategories.distinct()
}

/**
 * One scan the host reported.
 *
 * The decoded content is the user's data the moment it exists, so nothing here is
 * logged, cached, or uploaded by the SDK, and the result is not retained after it
 * is returned. A caller that persists [text] or [rawData] does so on its own
 * authority, over content the user chose to scan.
 *
 * @property text the decoded content
 * @property format the format the host named, or `null` when it named one this SDK
 *   does not know, or named none at all; [scanType] distinguishes those two
 * @property scanType the host's own name for the format, verbatim, or `null` when
 *   the host reported none
 * @property charSet the character set of [text], or `null` when the host reported none
 * @property rawData the decoded bytes as the host reports them, or `null` when the
 *   host reported none
 * @property path a path to the scanned image, or `null`; the host is documented as
 *   reporting this only in some cases
 */
internal class WeChatScanResult(
    internal val text: String,
    internal val format: WeChatScanFormat?,
    internal val scanType: String?,
    internal val charSet: String?,
    internal val rawData: String?,
    internal val path: String?,
)
