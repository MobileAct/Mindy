# Mindy
Mindy is Minimum Dependency Injection library.

Feature:
- Resolve generics type parameter
- Resolve instance as singleton or else
- Create parameterized instance

## Usage
### Simple
```kotlin
val mindy = Mindy()

// register type: String
mindy.register{ "Example" }

val value = mindy.resolve<String>()
```

### Polymorphism and TypeParameter
```kotlin
interface IValue<T> {

    val value: T
}

abstract class BaseValue<T>(override val value: T) : IValue<T>

class Value<T>(value: T) : BaseValue<T>(value)

class StringValue(value: String) : BaseValue<String>(value)

```
```kotlin
val mindy = Mindy()

// regster type: IValue<Int>
mindy.register<IValue<Int>>{ Value(10) } 

// register type: IValue<String>
mindy.register<IValue<String>>{ StringValue("Example") }

// register type: IValue<IValue<String>>
// dependency resolve(): IValue<String>
mindy.register<IValue<IValue<String>>>{ Value(resolve()) }


val intValue = mindy.resolve<IValue<Int>>()
val stringValue = mindy.resolve<IValue<String>>()
val nestedStringValue = mindy.resolve<IValue<IValue<String>>>()
```

### Create instance every resolve
```kotlin
val mindy = Mindy()

mindy.register<IValue<String>> { StringValue("Example") }

val a = mindy.create<IValue<String>>()
val b = mindy.create<IValue<String>>()
// a and b is different instance
```

### Parameterized
```kotlin
val mindy = Mindy()

mindy.register<IValue<IValue<String>>> { Value(resolve()) }

val a = mindy.create<IValue<IValue<String>>> {
    register<IValue<String>> { StringValue("a") }
}
val b = mindy.create<IValue<IValue<String>>> {
    register<IValue<String>> { StringValue("b") }
}
// a and b is different value
```

### Named
```kotlin
val mindy = Mindy()

mindy.register<IValue<String>>(name = "a") { StringValue("a") }
mindy.register<IValue<String>>(name = "b") { StringValue("b") }

val a = mindy.resolve<IValue<String>>(name = "a")
val b = mindy.resolve<IValue<String>>(name = "b")
// a and b is different instance/value
```

## Notify
Now, Mindy is using `@ExperimentalStdlibApi` by `typeOf()`.

## License
This library is under MIT License

### Rule
- using [Kotlin Standard Library](https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib), published by [Apache License 2.0](https://github.com/JetBrains/kotlin/tree/master/license)
- using [JUnit5](https://github.com/junit-team/junit5), published by [Eclipse Public License 2.0](https://github.com/junit-team/junit5/blob/master/LICENSE.md)

### Sample
- using [Kotlin Standard Library](https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib), published by [Apache License 2.0](https://github.com/JetBrains/kotlin/tree/master/license)
- using [AndroidX](https://github.com/aosp-mirror/platform_frameworks_support), published by [Apache License 2.0](https://github.com/aosp-mirror/platform_frameworks_support/blob/androidx-master-dev/LICENSE.txt)
- using [ConstraintLayout](https://android.googlesource.com/platform/frameworks/opt/sherpa/+/refs/heads/studio-master-dev/constraintlayout/), published by [Apache License 2.0](https://android.googlesource.com/platform/frameworks/opt/sherpa/+/refs/heads/studio-master-dev/constraintlayout/src/main/java/android/support/constraint/ConstraintLayout.java)
- using [AndroidX Test](https://github.com/android/android-test), published by [Apache License 2.0](https://github.com/android/android-test/blob/master/LICENSE)
- using [JUnit4](https://github.com/junit-team/junit4), published by [Eclipse Public License 1.0](https://github.com/junit-team/junit4/blob/master/LICENSE-junit.txt)

## Contribute
ToDo: Write

## Other
Author: [@MeilCli](https://github.com/MeilCli)
