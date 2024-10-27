# Abecedary

![An abecedarian form from a Tibetan ritual text](/images/abecedary.png)

_An abecedarian form (ka rtsom) from a Tibetan ritual text. (Image
from [BDRC](http://purl.bdrc.io/resource/MW1NLM718_O1NLM718_011))_

## Introduction

For some enums, order is meaningful:

```kotlin
enum class Planet {
    MERCURY,
    VENUS,
    EARTH, // etc.
}

println("rock ${Planet.EARTH.ordinal + 1} from the sun") // rock 3 from the sun
println("${Planet.MERCURY < Planet.VENUS}") // true
```

In others, order is arbitrary and we just want to group a set of related constants:

```kotlin
enum class CatalogFeature(override val id: String) : Feature {
    LINK_TO_SEARCH("link-to-search"),
    HOME_CAROUSEL("carousel"),
    ACCELERATED_BUY_NOW("buy-now")
}
```

In the case where order is meaningless, it's tempting just to add new entries
as they arrive (chronological order). This can work in small codebases, but in
larger codebases it can cause problems:

* Appending to the end of the file can generate merge conflicts when two developers
  attempt to land their new entry at the end of the file at a similar time.
* There might be a team who has more of a vested interest in the enum through using it more
  frequently. In this case, it's much easier for them to read the file if it maintains
  some other kind of order.
* Post-hoc re-orderings after the file has reached some tipping point can generate a noisy diff.

Lexicographic order (alphabetical order) is the most natural choice where we want to locate
an entry within a long list. But we can't insist on it everywhere because of cases
like `enum class Planet` where the entry order is meaningful. It's especially dangerous
to reorder when enums are parsed from an ordinal sent over the wire.

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

@Alphabetical
interface Edible {
    val calories: Int
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

### Calls to functions with vararg parameters

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

### Ordering `.gradle` and `.gradle.kts` files

If you are interested in keeping build files tidy, the solution is in another castle:
https://github.com/square/gradle-dependencies-sorter

## Installation

Just add the annotation artifact as `compileOnly` and the lint artifact to the `lintChecks`
configuration.

```kotlin
dependencies {
    compileOnly("io.github.drawers.abecedary.abecedary-annotation:<VERSION>")
    lintChecks("io.github.drawers.abecedary:abecedary-lint:<VERSION>")
}
```

| Artifact             | Version                                                                                                                                                                          |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| abecedary-annotation | [![Maven Central](https://img.shields.io/maven-central/v/io.github.drawers/abecedary-annotation.svg)](https://mvnrepository.com/artifact/io.github.drawers/abecedary-annotation) |
| abecedary-lint       | [![Maven Central](https://img.shields.io/maven-central/v/io.github.drawers/abecedary-lint.svg)](https://mvnrepository.com/artifact/io.github.drawers/abecedary-lint)             |

Note for non-android projects, you must apply the `com.android.lint` Gradle plugin to
use `lintChecks`.

### Compatibility

| Abecedary version | Lint version  |
|-------------------|---------------|
| 0.5.0             | 31.7.1        |
| 0.4.0             | 31.7.0        |
| 0.3.0             | 31.5.0        |
| 0.2.0             | 31.2.0-beta01 |

Remember that lint versions are tied to Android Gradle Plugin (AGP) versions:

```kotlin
lintVersion = androidGradlePluginVersion + 23.0.0
```

But if you're on a lower version of AGP, you can still use a higher version of lint
by following the
instructions [here](https://googlesamples.github.io/android-custom-lint-rules/usage/newer-lint.md.html)

Problems with the Abecedary checks not showing in the IDE can sometimes be solved by using a newer
version of lint or by upgrading to a more recent version of Android Studio.

## Philosophy

### Easy to clone and fork

Abecedary classes should be easy to clone and fork:

https://twitter.com/JimSproch/status/1656143262804217860

There are many projects with their own custom lint rules.
It should be easy for these projects to copy/paste Abecedary code into their own rule sets in order
to avoid an extra dependency on a 3rd party library or incompatibility issues with Kotlin/lint.

This means that a design where Abecedary abstracts over lint was considered and discarded.

### Severity

The `@Alphabetical` annotation is for cases where it _really_ is important to keep
dictionary order. Serious enough that you'd expect a PR to address a comment about ordering before
merging.

If you would prefer Abecedary to give less strict advice, it is possible to configure the rules.
See
the [Configuring Issues and Severity](http://googlesamples.github.io/android-custom-lint-rules/user-guide.md.html#lintgradleplugindsl/configuringissuesandseverity)
section of the lint user guide.

## Inspiration

Much of the infrastructure and approach in this project is informed by the amazing
https://github.com/slackhq/slack-lints project.
