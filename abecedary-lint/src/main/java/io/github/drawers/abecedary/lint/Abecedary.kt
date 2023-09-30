// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.lint

import com.intellij.psi.PsiClass
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.jetbrains.uast.UClass

object Annotation {
    const val FULLY_QUALIFIED_NAME = "io.github.drawers.abecedary.Alphabetical"
}

fun UClass.findAlphabeticalAnnotation(searchSuperTypes: Boolean): PsiClass? {
    // annotation explicit on class
    if (hasAnnotation(Annotation.FULLY_QUALIFIED_NAME)) {
        return this
    }

    if (!searchSuperTypes) {
        // we're only interested in types
        // explicitly annotated with @Alphabetical
        return null
    }

    // check for implicit annotation through supertypes
    val superTypes =
        uastSuperTypes.mapNotNull {
            it.type as? PsiClassReferenceType
        }.mapNotNull {
            it.resolve()
        }

    // BFS because we assume the annotation
    // will be found close to the root
    val deque = ArrayDeque(superTypes)
    while (deque.isNotEmpty()) {
        val head = deque.removeFirst()
        if (head.hasAnnotation(Annotation.FULLY_QUALIFIED_NAME)) {
            return head
        }
        deque.addAll(head.superTypes.mapNotNull { it.resolve() })
    }

    return null
}
