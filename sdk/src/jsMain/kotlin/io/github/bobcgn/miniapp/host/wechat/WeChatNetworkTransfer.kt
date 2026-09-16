package io.github.bobcgn.miniapp.host.wechat

/**
 * What one upload asks the host for.
 *
 * The SDK does not define an upload protocol: it carries a URL, a local file, a form
 * field name, and the caller's own headers and form fields to the host and reports
 * what comes back. It does not read the file, does not retry, and does not add
 * authentication of its own.
 *
 * Headers and form fields are strings, and form fields the host accepts in other
 * shapes are not represented: a caller that needs to send a non-string form value
 * has to encode it itself, because the SDK will not hand the host a value it cannot
 * describe.
 *
 * @property url absolute HTTPS endpoint the host posts to
 * @property filePath path to the file inside the host's file sandbox
 * @property name name the host gives the file in the multipart body
 * @property headers request headers to send
 * @property formData additional multipart form fields
 * @property timeoutMillis host timeout in milliseconds, or `null` for the host default
 */
internal class WeChatUploadRequest(
    internal val url: String,
    internal val filePath: String,
    internal val name: String,
    internal val headers: Map<String, String> = emptyMap(),
    internal val formData: Map<String, String> = emptyMap(),
    internal val timeoutMillis: Int? = null,
) {
    init {
        require(url.isNotBlank()) {
            "uploadFile requires a url, because the host has nowhere to post without one"
        }
        require(filePath.isNotBlank()) {
            "uploadFile requires a filePath, because a blank path names no file"
        }
        require(name.isNotBlank()) {
            "uploadFile requires a form field name, because the host cannot name the part otherwise"
        }
        if (timeoutMillis != null) {
            require(timeoutMillis > 0) {
                "uploadFile timeoutMillis must be positive when supplied, but was $timeoutMillis"
            }
        }
    }
}

/**
 * One completed upload.
 *
 * A completed upload is a completed exchange: the host obtained an HTTP response, and
 * its status describes that response rather than whether the upload was useful. A
 * non-2xx status is therefore a result, exactly as it is for the HTTP transport
 * capability, and only a transport-level failure becomes an exception.
 *
 * @property statusCode HTTP status the host received
 * @property responseText the response body as text
 */
internal class WeChatUploadResult(
    internal val statusCode: Int,
    internal val responseText: String,
)

/**
 * What one download asks the host for.
 *
 * @property url absolute endpoint the host fetches from
 * @property headers request headers to send
 * @property timeoutMillis host timeout in milliseconds, or `null` for the host default
 * @property filePath where the host should put the file, or `null` to let the host
 *   use its own temporary location
 */
internal class WeChatDownloadRequest(
    internal val url: String,
    internal val headers: Map<String, String> = emptyMap(),
    internal val timeoutMillis: Int? = null,
    internal val filePath: String? = null,
) {
    init {
        require(url.isNotBlank()) {
            "downloadFile requires a url, because the host has nothing to fetch without one"
        }
        if (filePath != null) {
            require(filePath.isNotBlank()) {
                "downloadFile filePath must be non-blank when supplied, because a blank path names no file"
            }
        }
        if (timeoutMillis != null) {
            require(timeoutMillis > 0) {
                "downloadFile timeoutMillis must be positive when supplied, but was $timeoutMillis"
            }
        }
    }
}

/**
 * One completed download.
 *
 * The paths are host file references. The SDK does not read the file, move it, or
 * claim it is durable: what the host put where is the host's business, and further
 * file work belongs to the file-system capability the caller drives explicitly.
 *
 * @property statusCode HTTP status the host received
 * @property tempFilePath the host's temporary copy of the downloaded content
 * @property filePath the path the host reports when the caller asked for a target
 *   location, or `null` when the host reported none
 */
internal class WeChatDownloadResult(
    internal val statusCode: Int,
    internal val tempFilePath: String,
    internal val filePath: String?,
)

/**
 * Progress the host reported for an in-flight transfer.
 *
 * This is what the host last said, not an estimate the SDK computed, and it is not a
 * promise about the future: a transfer that never reports progress simply has no
 * value here, which is reported as `null` rather than as zero per cent.
 *
 * @property percent the host's own percentage, in `0..100`
 * @property bytesTransferred bytes the host reported transferring, when it reports
 *   that figure for this direction
 * @property bytesExpected bytes the host reported expecting, when it reports that
 *   figure for this direction
 */
internal class WeChatTransferProgress(
    internal val percent: Int,
    internal val bytesTransferred: Long?,
    internal val bytesExpected: Long?,
)
