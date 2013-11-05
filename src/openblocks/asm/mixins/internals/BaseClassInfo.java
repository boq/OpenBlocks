package openblocks.asm.mixins.internals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;

import openblocks.asm.mixins.ExtensionPoint;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class BaseClassInfo {
	public final String superClass;
	public final Class<?> cls;
	public final Type selfType;

	public Set<Method> implementedMethods;
	public Set<Method> abstractMethods;
	public Set<Method> constructors = Sets.newHashSet();
	public final Set<Method> extensionMethods;

	public BaseClassInfo(Class<?> cls) {
		this.cls = cls;
		selfType = Type.getType(cls);
		superClass = Type.getType(cls.getSuperclass()).getInternalName();

		ImmutableSet.Builder<Method> absMethods = ImmutableSet.builder();
		ImmutableSet.Builder<Method> implMethods = ImmutableSet.builder();
		ImmutableSet.Builder<Method> extMethods = ImmutableSet.builder();

		for (java.lang.reflect.Method m : cls.getMethods()) {
			Method desc = Method.getMethod(m);
			if (Modifier.isAbstract(m.getModifiers())) absMethods.add(desc);
			else implMethods.add(desc);

			ExtensionPoint ann = m.getAnnotation(ExtensionPoint.class);

			if (ann != null) {
				Preconditions.checkArgument(m.getReturnType().equals(void.class), "Extension method '%s' from '%s' has non-void return type", m, cls);
				extMethods.add(desc);
			}
		}

		implementedMethods = implMethods.build();
		abstractMethods = absMethods.build();
		extensionMethods = extMethods.build();

		for (Constructor<?> ctr : cls.getConstructors())
			constructors.add(Method.getMethod(ctr));
	}

	public void addConstructors(ClassNode cls) {
		final String newName = cls.name;
		final String superName = selfType.getInternalName();
		for (Method ctor : constructors) {
			String desc = ctor.getDescriptor();
			MethodNode node = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", desc, null, null);
			Type[] args = ctor.getArgumentTypes();

			node.visitCode();
			int stackSize = Helper.addLoadSequence(node, args);
			node.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", desc);
			node.visitVarInsn(Opcodes.ALOAD, 0);
			node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, newName, Helper.SUB_CTOR_NAME, "()V");
			node.visitInsn(Opcodes.RETURN);
			node.visitMaxs(stackSize, stackSize);
			node.visitEnd();

			cls.methods.add(node);
		}
	}
}
