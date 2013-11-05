package openblocks.asm.mixins.internals;

import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Sets;

public class ExtMethodBuilder {
	public final Method method;
	private String methodName;
	private boolean skipSuper;
	private int flags;
	private Set<String> subMethods = Sets.newHashSet();

	public ExtMethodBuilder(Method method, String methodName, int flags, boolean skipSuper) {
		this.method = method;
		this.methodName = methodName;
		this.flags = flags;
		this.skipSuper = skipSuper;
	}

	public void addSubMethod(String method) {
		subMethods.add(method);
	}

	public void buildMergeMethod(ClassNode cls) {
		String desc = method.getDescriptor();
		Type args[] = method.getArgumentTypes();
		int stackSize = Helper.calculateStackSize(args);

		MethodNode newMethod = new MethodNode(flags, methodName, method.getDescriptor(), null, null);

		newMethod.visitCode();
		if (!skipSuper) {
			Helper.addLoadSequence(newMethod, args);
			newMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, cls.superName, method.getName(), desc);
		}

		for (String sub : subMethods) {
			Helper.addLoadSequence(newMethod, args);
			newMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cls.name, sub, desc);
		}

		newMethod.visitInsn(Opcodes.RETURN);
		newMethod.visitMaxs(stackSize, stackSize);
		newMethod.visitEnd();
		cls.methods.add(newMethod);
	}
}
