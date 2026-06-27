package app.template.patches.trainwise

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.builder.BuilderTryBlock
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

fun MutableMethod.ensureRegisters(needed: Int) {
    val impl = implementation ?: return
    if (impl.registerCount >= needed) return

    val field = registerCountField
        ?: throw PatchException("MutableMethodImplementation has no int registerCount field")
    field.setInt(impl, needed)
}

fun MutableMethod.clearBody() {
    val impl = implementation ?: return
    val field = tryBlocksField
        ?: throw PatchException("MutableMethodImplementation has no try-block list field")

    @Suppress("UNCHECKED_CAST")
    (field.get(impl) as MutableList<BuilderTryBlock>).clear()

    val count = impl.instructions.toList().size
    repeat(count) { impl.removeInstruction(0) }
}

private val registerCountField: Field? =
    MutableMethodImplementation::class.java.declaredFields
        .firstOrNull { it.type == Int::class.javaPrimitiveType }
        ?.apply { isAccessible = true }

private val tryBlocksField: Field? =
    MutableMethodImplementation::class.java.declaredFields
        .firstOrNull { field ->
            if (!MutableList::class.java.isAssignableFrom(field.type) &&
                !List::class.java.isAssignableFrom(field.type)
            ) return@firstOrNull false

            val generic = field.genericType as? ParameterizedType ?: return@firstOrNull false
            val arg = generic.actualTypeArguments.firstOrNull() ?: return@firstOrNull false
            arg.typeName == BuilderTryBlock::class.java.name ||
                arg.typeName.startsWith("${BuilderTryBlock::class.java.name}<")
        }
        ?.apply { isAccessible = true }
