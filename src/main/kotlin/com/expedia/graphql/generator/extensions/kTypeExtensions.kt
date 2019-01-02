package com.expedia.graphql.generator.extensions

import com.expedia.graphql.exceptions.InvalidListTypeException
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

private val primitiveArrayTypes = mapOf(
    IntArray::class to Int::class,
    LongArray::class to Long::class,
    ShortArray::class to Short::class,
    FloatArray::class to Float::class,
    DoubleArray::class to Double::class,
    CharArray::class to Char::class,
    BooleanArray::class to Boolean::class
)

internal fun KType.getKClass() = this.jvmErasure

@Throws(InvalidListTypeException::class)
internal fun KType.getTypeOfFirstArgument(): KType =
    this.arguments.firstOrNull()?.type ?: throw InvalidListTypeException(this)

internal fun KType.getWrappedType(): KType {
    val primitiveClass = primitiveArrayTypes[this.getKClass()]
    return when {
        primitiveClass != null -> primitiveClass.createType()
        else -> this.getTypeOfFirstArgument()
    }
}

internal fun KType.getWrappedName(): String {
    val isPrimitiveArray = primitiveArrayTypes.containsKey(this.getKClass())
    return when {
        isPrimitiveArray -> this.getSimpleName()
        this.getKClass().isList() -> "List<${this.getWrappedType().getSimpleName()}>"
        this.getKClass().isArray() -> "Array<${this.getWrappedType().getSimpleName()}>"
        else -> this.getSimpleName()
    }
}

internal fun KType.getSimpleName(): String = this.getKClass().getSimpleName()

internal val KType.qualifiedName: String
    get() = this.getKClass().getQualifiedName()
