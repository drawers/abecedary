# Abecedary

![An abecedarian form from a Tibetan ritual text](/images/abecedary.png)

An abecedarian form (ka rtsom) from a Tibetan ritual text.

_Image from [BDRC](http://purl.bdrc.io/resource/MW1NLM718_O1NLM718_011)_

## Introduction

In large codebases, files containing enums can become messy. Some enums have a clear
order that requires no explanation:

```kotlin
enum class Planet {
    MERCURY,
    VENUS,
    EARTH, // etc.
}
```

But others don't:

```kotlin
enum class CatalogFeature(override val id: String) : Feature {
    LINK_TO_SEARCH("link-to-search"),
    HOME_CAROUSEL("carousel"),
    ACCELERATED_BUY_NOW("buy-now")
}
```

It's often not clear for new devs where to put their new entry:

* Chronological can lead to more merge conflicts where two devs want to land their new entry
  at the end of the file at the same time.
* Re-orderings often make for a noisy diff and can create
  a burden for teams who have more stake in the code quality of the enum file.
* Insisting on alphabetical order everywhere can be dangerous when there are enums parsed
  from ordinals sent over the wire.

Abecedary is a static analysis tool that lets you choose which enums you want to maintain
alphabetical
order. It is based on discussions with [Zarah Dominguez](https://github.com/zmdominguez) and is a
clean room implementation of work started by Michael Ye.

## How to use

### Enums

Abecedary exposes an annotation called `@Alphabetical`. This is metadata you can use to decorate
an enum where you want the entries to maintain lexicographic order. You'll get the error in the IDE
if you are using Android Studio:

![An error on an enum where the entries are not in lexicographic order](/images/enum_with_error_in_IDE.png)

Apply this annotation directly to an enum:

```kotlin
@Alphabetical
enum class Fruit {
    APPLE,
    BANANA,
    CHERRY
}
```

Alternatively, you can apply the annotation to an interface.

```kotlin
@Alphabetical
interface Edible {
    val calories: Int
}
```

Now all enum classes that implement the interface will be scanned:

```kotlin
enum class Fruit(override val calories: Int) : Edible {
    APPLE(50),
    BANANA(100),
    CHERRY(200),
}

enum class Vegetable(override val calories: Int) : Edible {
    ASPARAGUS(20),
    BROCCOLI(10),
    CARROT(30),
}
```

This is especially useful for enums that implement a common interface from a base module
that a feature module would be expected to implement.

### Sealed classes and interfaces

Abecedary handles the case where sealed classes and interfaces are used like enums, with subclasses
declared in the class body:

```kotlin
@Alphabetical
sealed class Fruit {
    object Apple : Fruit()
    object Banana : Fruit()
    object Cherry : Fruit()
}

sealed interface Vegetable : Edible {
    object Asparagus : Vegetable {
        override val calories = 20
    }
    object Broccoli : Vegetable {
        override val calories = 10
    }
}
```

Note that we don't handle the case where sealed subclasses are declared outside the class body since
we want to keep things simple and don't want to enter into more general disputes about declaration
order of members
within a Kotlin file.

### EXPERIMENTAL - Calls to functions with vararg parameters

Thanks to a suggestion from [Nicola Corti](https://github.com/cortinico), it's possible to target a
call expression:

```kotlin
val fruits = @Alphabetical listOf("Apple", "Banana")
```

We only check alphabetical order for call expressions where the callee function has
a single parameter marked `vararg`. The intention here is to cover the most common use case,
`listOf`, `setOf` and so on, without adding too much complexity.

In a chain, the `@Alphabetical` annotation will target the first such expression:

```kotlin
class SpecialList(
    vararg element: String
) {
    fun addAll(vararg element: String)
}

fun foo() {
    @Alphabetical SpecialList("c", "b", "a").addAll(
        "f",
        "e",
        "d"
    ) // reports only "c", "b", "a" out of order
}
```

If you want a different target, you should be able to decompose the chain into local variables:

```kotlin
val specialList = SpecialList("a", "b", "c")
@Alphabetical specialList.addAll("f", "e", "d")
```

### Installation

Just add the dependency to the `lintChecks` configuration. Note for non-android projects, you must
apply the `com.android.lint` Gradle plugin to use this:

Latest version:
https://mvnrepository.com/artifact/io.github.drawers/abecedary-lint

```kotlin
dependencies {
    compileOnly("io.github.drawers.abecedary.abecedary-annotation:<VERSION>")
    lintChecks("io.github.drawers.abecedary:abecedary-lint:<VERSION>")
}
```

#### Compatibility

| Abecedary version | Lint version  | AGP version (Lint version - 23) |
|-------------------|---------------|---------------------------------|
| 0.2.0             | 31.2.0-beta01 | 8.2.0-alpha10                   |

#### Troubleshooting

Problems with the Abecedary checks not showing in the IDE can sometimes be solved by using a newer
version of lint:

https://googlesamples.github.io/android-custom-lint-rules/usage/newer-lint.md.html

Or a newer version of Android Studio.

This is because lint is integrated with the Android Gradle Plugin:

```kotlin
lintVersion = gradlePluginVersion + 23.0.0
```

See [the lint API guide](https://googlesamples.github.io/android-custom-lint-rules/api-guide.html#example:samplelintcheckgithubproject/lintversion?)
for more information.

Problems with Android projects that include a module for custom lint checks can often be solved by
running the `clean` and `assemble` tasks
for that module and then going to `File / Reload All from Disk`

## Philosophy

### Easy to clone and fork

Abecedary classes should be easy to clone and fork:

https://twitter.com/JimSproch/status/1656143262804217860

There are many projects with their own custom lint rules.
It should be easy for these projects to copy/paste Abecedary code into their own rule sets in order
to avoid an extra dependency on a 3rd party library.

This means that a design where Abecedary abstracts over lint was considered and discarded.

### Why Severity.ERROR ?

Android Lint lets you define different severities for violations of rules. Why did default to
`Severity.ERROR`?

The `@Alphabetical` annotation is for cases where it _really_ is important to keep
dictionary order. Serious enough that you'd expect a PR to address a comment about ordering before
merging.

If you would prefer Abecedary to give less strict advice, it is possible to configure the rules.
See
the [Configuring Issues and Severity](http://googlesamples.github.io/android-custom-lint-rules/user-guide.md.html#lintgradleplugindsl/configuringissuesandseverity)
section of the lint user guide.

### Detekt?

I have Detekt versions of these checks to share, but I don't have time to maintain a Detekt
extension.
If you are interested in maintaining a Detekt version of Abecedary, let me know!