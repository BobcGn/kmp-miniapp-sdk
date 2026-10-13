package consumer

/** Shared text generation; each host provides its own platform identity. */
public fun greeting(platform: String): String = "Hello, $platform!"
