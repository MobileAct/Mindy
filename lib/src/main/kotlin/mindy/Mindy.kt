package mindy

import kotlin.reflect.typeOf

@ExperimentalStdlibApi
interface IReadOnlyMindy {

    @Suppress("UNCHECKED_CAST")
    fun <T> resolve(typeIdentifier: String, name: String, additionalResolver: (Mindy.() -> Unit)?): T

    @Suppress("UNCHECKED_CAST")
    fun <T> create(typeIdentifier: String, name: String, additionalResolver: (Mindy.() -> Unit)?): T
}

@ExperimentalStdlibApi
class Mindy : IReadOnlyMindy {

    private class Entry<out T>(
        private val instanceCreator: Mindy.() -> T,
        private val isSingletonOnly: Boolean
    ) {

        private var instance: T? = null

        private fun createInstance(mindy: Mindy): T {
            val instance = instanceCreator.invoke(mindy)
            this.instance = instance
            return instance
        }

        fun create(mindy: Mindy): T {
            if (isSingletonOnly) {
                throw IllegalStateException("This instance is only singleton")
            }
            return createInstance(mindy)
        }

        fun resolve(mindy: Mindy): T {
            return instance ?: createInstance(mindy)
        }
    }

    private val entries = mutableMapOf<String, Entry<*>>()
    private val transactionEntries = mutableMapOf<String, Entry<*>>()
    internal val transactionEntryCount get() = transactionEntries.size
    private var isTransaction = false

    fun beginTransaction() {
        isTransaction = true
    }

    fun commitTransaction() {
        isTransaction = false
        entries.putAll(transactionEntries)
        transactionEntries.clear()
    }

    fun rollbackTransaction() {
        isTransaction = false
        transactionEntries.clear()
    }

    private fun identifier(typeIdentifier: String, name: String) = "$typeIdentifier#$name"

    fun <T> register(
        typeIdentifier: String,
        name: String = "",
        isSingletonOnly: Boolean = false,
        instanceCreator: Mindy.() -> T
    ) {
        val entry = Entry(instanceCreator, isSingletonOnly)

        if (isTransaction) {
            transactionEntries[identifier(typeIdentifier, name)] = entry
        } else {
            entries[identifier(typeIdentifier, name)] = entry
        }
    }

    private fun findEntry(typeIdentifier: String, name: String = ""): Entry<*> {
        val identifier = identifier(typeIdentifier, name)
        return transactionEntries[identifier]
            ?: entries[identifier]
            ?: throw IllegalStateException("$identifier is not registered")
    }

    private inline fun <T> transaction(crossinline function: () -> T): T {
        beginTransaction()
        val result = function()
        rollbackTransaction()
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> resolve(
        typeIdentifier: String,
        name: String,
        additionalResolver: (Mindy.() -> Unit)?
    ): T {
        if (additionalResolver != null) {
            return transaction {
                additionalResolver.invoke(this)
                findEntry(typeIdentifier, name).resolve(this) as T
            }
        }
        return findEntry(typeIdentifier, name).resolve(this) as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> create(
        typeIdentifier: String,
        name: String,
        additionalResolver: (Mindy.() -> Unit)?
    ): T {
        if (additionalResolver != null) {
            return transaction {
                additionalResolver.invoke(this)
                findEntry(typeIdentifier, name).create(this) as T
            }
        }
        return findEntry(typeIdentifier, name).create(this) as T
    }
}

@ExperimentalStdlibApi
inline fun <reified T> Mindy.register(
    name: String = "",
    isSingletonOnly: Boolean = false,
    noinline instanceCreator: Mindy.() -> T
) {
    val typeIdentifier = typeOf<T>().toString()
    register(typeIdentifier, name, isSingletonOnly, instanceCreator)
}

@ExperimentalStdlibApi
inline fun <reified T> IReadOnlyMindy.resolve(
    name: String = "",
    noinline additionalResolver: (Mindy.() -> Unit)? = null
): T {
    val typeIdentifier = typeOf<T>().toString()
    return resolve(typeIdentifier, name, additionalResolver)
}

@ExperimentalStdlibApi
inline fun <reified T> IReadOnlyMindy.create(
    name: String = "",
    noinline additionalResolver: (Mindy.() -> Unit)? = null
): T {
    val typeIdentifier = typeOf<T>().toString()
    return create(typeIdentifier, name, additionalResolver)
}