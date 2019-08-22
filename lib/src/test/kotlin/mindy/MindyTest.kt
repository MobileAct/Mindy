package mindy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


@ExperimentalStdlibApi
class MindyTest {

    interface IValue<T> {

        val value: T
    }

    abstract class BaseValue<T>(override val value: T) : IValue<T>

    class Value<T>(value: T) : BaseValue<T>(value)

    class StringValue(value: String) : BaseValue<String>(value)

    @Test
    fun testResolve_simple() {
        val mindy = Mindy()

        mindy.register { "Test" }
        mindy.register { 10 }
        mindy.register { 3.0 }

        assertEquals("Test", mindy.resolve<String>())
        assertEquals(10, mindy.resolve<Int>())
        assertEquals(3.0, mindy.resolve<Double>())

        assertThrows<IllegalStateException> {
            mindy.resolve<Float>()
        }
    }

    @Test
    fun testResolve_interface() {
        val mindy = Mindy()

        mindy.register<IValue<Int>> { Value(10) }
        mindy.register<IValue<String>> { StringValue("Test") }

        val intValue = mindy.resolve<IValue<Int>>()
        val stringValue = mindy.resolve<IValue<String>>()

        assertEquals(10, intValue.value)
        assertEquals("Test", stringValue.value)

        assertThrows<IllegalStateException> {
            mindy.resolve<IValue<Float>>()
        }
    }

    @Test
    fun testResolve_abstract() {
        val mindy = Mindy()

        mindy.register<BaseValue<Int>> { Value(10) }
        mindy.register<BaseValue<String>> { StringValue("Test") }

        val intValue = mindy.resolve<BaseValue<Int>>()
        val stringValue = mindy.resolve<BaseValue<String>>()

        assertEquals(10, intValue.value)
        assertEquals("Test", stringValue.value)

        assertThrows<IllegalStateException> {
            mindy.resolve<BaseValue<Float>>()
        }
    }

    @Test
    fun testResolve_dependency() {
        val mindy = Mindy()

        mindy.register<IValue<IValue<String>>> { Value(resolve()) }
        mindy.register<IValue<String>> { StringValue("Test") }

        val nestedStringValue = mindy.resolve<IValue<IValue<String>>>()

        assertEquals("Test", nestedStringValue.value.value)
    }

    @Test
    fun testResolve_dependency_notRegistered() {
        val mindy = Mindy()

        mindy.register<IValue<IValue<String>>> { Value(resolve()) }

        assertThrows<IllegalStateException> {
            mindy.resolve<IValue<IValue<String>>>()
        }
    }

    @Test
    fun testResolve_sameInstance() {
        val mindy = Mindy()

        mindy.register<IValue<String>> { StringValue("Test") }

        val a = mindy.resolve<IValue<String>>()
        val b = mindy.resolve<IValue<String>>()

        assertEquals(a, b)
    }

    @Test
    fun testResolve_parameter() {
        val mindy = Mindy()

        mindy.register<IValue<IValue<String>>> { Value(resolve()) }

        val a = mindy.resolve<IValue<IValue<String>>> {
            register<IValue<String>> { StringValue("a") }
        }
        val b = mindy.resolve<IValue<IValue<String>>> {
            register<IValue<String>> { StringValue("b") }
        }

        assertEquals(0, mindy.transactionEntryCount)

        assertEquals("a", a.value.value)
        assertEquals("a", b.value.value) // because resolve() is singleton
    }

    @Test
    fun testResolve_parameter_fail() {
        val mindy = Mindy()

        mindy.register<IValue<IValue<String>>> { Value(resolve()) }
        mindy.register<IValue<IValue<Int>>> { Value(resolve()) }

        val a = mindy.resolve<IValue<IValue<String>>> {
            register<IValue<String>> { StringValue("a") }
            register<IValue<Int>> { Value(1) }
        }

        assertEquals(0, mindy.transactionEntryCount)

        assertEquals("a", a.value.value)

        assertThrows<IllegalStateException> {
            mindy.resolve<IValue<IValue<Int>>>()
        }
    }

    @Test
    fun testResolve_parameter_nestedDependency() {
        val mindy = Mindy()
        mindy.register<Pair<String, Pair<Int, Boolean>>> { Pair(resolve(), resolve()) }
        mindy.register<Pair<Int, Boolean>> { Pair(resolve(), resolve()) }

        val a = mindy.resolve<Pair<String, Pair<Int, Boolean>>> {
            register { "a" }
            register { 1 }
            register { true }
        }
        val b = mindy.resolve<Pair<String, Pair<Int, Boolean>>>() {
            register { "b" }
            register { 2 }
            register { false }
        }

        assertEquals(0, mindy.transactionEntryCount)

        assertEquals("a", a.first)
        assertEquals(1, a.second.first)
        assertEquals(true, a.second.second)

        // because resolve() is singleton
        assertEquals("a", b.first)
        assertEquals(1, b.second.first)
        assertEquals(true, b.second.second)
    }

    @Test
    fun testCreate_simple() {
        val mindy = Mindy()

        mindy.register { "Test" }
        mindy.register { 10 }
        mindy.register { 3.0 }

        assertEquals("Test", mindy.create<String>())
        assertEquals(10, mindy.create<Int>())
        assertEquals(3.0, mindy.create<Double>())

        assertThrows<IllegalStateException> {
            mindy.create<Float>()
        }
    }

    @Test
    fun testCreate_interface() {
        val mindy = Mindy()

        mindy.register<IValue<Int>> { Value(10) }
        mindy.register<IValue<String>> { StringValue("Test") }

        val intValue = mindy.create<IValue<Int>>()
        val stringValue = mindy.create<IValue<String>>()

        assertEquals(10, intValue.value)
        assertEquals("Test", stringValue.value)

        assertThrows<IllegalStateException> {
            mindy.create<IValue<Float>>()
        }
    }

    @Test
    fun testCreate_abstract() {
        val mindy = Mindy()

        mindy.register<BaseValue<Int>> { Value(10) }
        mindy.register<BaseValue<String>> { StringValue("Test") }

        val intValue = mindy.create<BaseValue<Int>>()
        val stringValue = mindy.create<BaseValue<String>>()

        assertEquals(10, intValue.value)
        assertEquals("Test", stringValue.value)

        assertThrows<IllegalStateException> {
            mindy.create<BaseValue<Float>>()
        }
    }

    @Test
    fun testCreate_dependency() {
        val mindy = Mindy()

        mindy.register<IValue<IValue<String>>> { Value(create()) }
        mindy.register<IValue<String>> { StringValue("Test") }

        val nestedStringValue = mindy.create<IValue<IValue<String>>>()

        assertEquals("Test", nestedStringValue.value.value)
    }

    @Test
    fun testCreate_dependency_notRegistered() {
        val mindy = Mindy()

        mindy.register<IValue<IValue<String>>> { Value(create()) }

        assertThrows<IllegalStateException> {
            mindy.create<IValue<IValue<String>>>()
        }
    }

    @Test
    fun testCreate_notSameInstance() {
        val mindy = Mindy()

        mindy.register<IValue<String>> { StringValue("Test") }

        val a = mindy.create<IValue<String>>()
        val b = mindy.create<IValue<String>>()

        assertNotEquals(a, b)
    }

    @Test
    fun testCreate_parameter() {
        val mindy = Mindy()

        mindy.register<IValue<IValue<String>>> { Value(resolve()) }

        val a = mindy.create<IValue<IValue<String>>> {
            register<IValue<String>> { StringValue("a") }
        }
        val b = mindy.create<IValue<IValue<String>>> {
            register<IValue<String>> { StringValue("b") }
        }

        assertEquals(0, mindy.transactionEntryCount)

        assertEquals("a", a.value.value)
        assertEquals("b", b.value.value)
    }

    @Test
    fun testCreate_parameter_fail() {
        val mindy = Mindy()

        mindy.register<IValue<IValue<String>>> { Value(resolve()) }
        mindy.register<IValue<IValue<Int>>> { Value(resolve()) }

        val a = mindy.create<IValue<IValue<String>>> {
            register<IValue<String>> { StringValue("a") }
            register<IValue<Int>> { Value(1) }
        }

        assertEquals(0, mindy.transactionEntryCount)

        assertEquals("a", a.value.value)

        assertThrows<IllegalStateException> {
            mindy.create<IValue<IValue<Int>>>()
        }
    }

    @Test
    fun testCreate_parameter_nestedDependency() {
        val mindy = Mindy()
        mindy.register<Pair<String, Pair<Int, Boolean>>> { Pair(resolve(), create()) }
        mindy.register<Pair<Int, Boolean>> { Pair(resolve(), resolve()) }

        val a = mindy.create<Pair<String, Pair<Int, Boolean>>> {
            register { "a" }
            register { 1 }
            register { true }
        }

        assertEquals(0, mindy.transactionEntryCount)

        val b = mindy.create<Pair<String, Pair<Int, Boolean>>>() {
            register { "b" }
            register { 2 }
            register { false }
        }

        assertEquals(0, mindy.transactionEntryCount)

        assertEquals("a", a.first)
        assertEquals(1, a.second.first)
        assertEquals(true, a.second.second)

        assertEquals("b", b.first)
        assertEquals(2, b.second.first)
        assertEquals(false, b.second.second)
    }

    @Test
    fun testCreate_resolveSingleton() {
        val mindy = Mindy()

        mindy.register<IValue<String>> { StringValue("Test") }
        mindy.register<IValue<IValue<String>>> { Value(resolve()) }

        val a = mindy.create<IValue<IValue<String>>>()
        val b = mindy.create<IValue<IValue<String>>>()

        assertEquals("Test", a.value.value)
        assertEquals("Test", b.value.value)
        assertEquals(a.value, b.value)
        assertNotEquals(a, b)
    }

    @Test
    fun testCreate_resolvedValue() {
        val mindy = Mindy()

        mindy.register<IValue<String>> { StringValue("Test") }

        val a = mindy.resolve<IValue<String>>()
        val b = mindy.resolve<IValue<String>>()
        val c = mindy.create<IValue<String>>()

        assertEquals(a, b)
        assertNotEquals(a, c)
        assertNotEquals(b, c)
    }

    @Test
    fun testCreate_failSingletonOnly() {
        val mindy = Mindy()

        mindy.register<IValue<String>>(isSingletonOnly = true) { StringValue("Test") }

        assertThrows<IllegalStateException> {
            mindy.create<IValue<String>>()
        }
    }

    @Test
    fun testTransaction_commit() {
        val mindy = Mindy()

        mindy.beginTransaction()
        mindy.register { "" }
        mindy.commitTransaction()

        assertEquals("", mindy.resolve<String>())
        assertEquals(0, mindy.transactionEntryCount)
    }

    @Test
    fun testTransaction_rollback() {
        val mindy = Mindy()

        mindy.beginTransaction()
        mindy.register { "" }
        mindy.rollbackTransaction()

        assertEquals(0, mindy.transactionEntryCount)
    }

    @Test
    fun testTransaction_autoBeginToClose(){
        val mindy = Mindy()

        mindy.register{ "" }
        mindy.resolve<String>()
        mindy.register{ 1 }
        assertEquals(0, mindy.transactionEntryCount)
    }

    @Test
    fun testNamed_resolve() {
        val mindy = Mindy()

        mindy.register<IValue<String>>(name = "a") { StringValue("a") }
        mindy.register<IValue<String>>(name = "b") { StringValue("b") }

        val a = mindy.resolve<IValue<String>>(name = "a")
        val b = mindy.resolve<IValue<String>>(name = "b")

        assertEquals("a", a.value)
        assertEquals("b", b.value)
    }

    @Test
    fun testNamed_create() {
        val mindy = Mindy()

        mindy.register<IValue<String>>(name = "a") { StringValue("a") }
        mindy.register<IValue<String>>(name = "b") { StringValue("b") }

        val a = mindy.create<IValue<String>>(name = "a")
        val b = mindy.create<IValue<String>>(name = "b")

        assertEquals("a", a.value)
        assertEquals("b", b.value)
    }
}