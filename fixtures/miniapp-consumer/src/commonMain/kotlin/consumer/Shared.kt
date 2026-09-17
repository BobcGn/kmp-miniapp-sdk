package consumer

/**
 * Shared client behaviour: the code a Compose client and a Mini App host would both use.
 *
 * It has no host API, no JavaScript, and nothing that knows which host will load it.
 */
public fun greeting(name: String): String = "Hello, $name, from commonMain"

public class Counter(private var value: Int = 0) {

    public fun increment(): Int {
        value += 1
        return value
    }

    public fun current(): Int = value
}
