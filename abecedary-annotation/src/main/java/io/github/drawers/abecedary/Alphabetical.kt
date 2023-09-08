package io.github.drawers.abecedary

/**
 * Decorates a target where dictionary order needs to be enforced via static analysis.
 *
 * When applied to an **enum**, it indicates that enum entries should be declared in alphabetical order:
 *
 *     @Alphabetical
 *     enum class Fruit {
 *         APPLE,
 *         BANANA,
 *     }
 *
 * When applied to a **sealed class or interface**, it indicates the sealed subtypes should be declared in
 * alphabetical order.
 *
 *     @Alphabetical
 *     sealed class Fruit {
 *         object Apple: Fruit()
 *         object Banana: Fruit()
 *     }
 *
 * Note that this currently only covers the use case where sealed classes are being
 * used similar to enums with the subclasses declared nested in the sealed class.
 * It's not intended to venture into establishing a canonical order for all declarations in a file.
 *
 * It can also be **applied to a supertype like an interface** or abstract class. In this case, it
 * will mean that subtypes should also keep their entries in dictionary order.
 *
 *     @Alphabetical
 *     interface Edible {
 *         val calories: Int
 *     }
 *
 *     enum class Fruit(override val calories: Int): Edible {
 *         APPLE(50),
 *         BANANA(100),
 *     }
 *
 * Lastly, it's possible to enforce the order of **vararg arguments** by using the annotation
 * in expressions:
 *
 *     val fruits = @Alphabetical listOf("apple", "banana")
 *
 * This only works for function calls where the function has a single parameter marked as
 * `vararg`, like `listOf`, and `setOf`. In the case where there are multiple `vararg`
 * in a single expression, the first one is selected.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.EXPRESSION)
annotation class Alphabetical