package openblocks.asm.mixins.internals;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.FieldNode;

public final class Helper {

	private Helper() {}

	public static int addLoadSequence(MethodVisitor mv, Type[] args) {
		int localIndex = 0;
		mv.visitVarInsn(Opcodes.ALOAD, localIndex++);
		for (Type type : args) {
			mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), localIndex);
			localIndex += type.getSize();
		}

		return localIndex;
	}

	public static int calculateStackSize(Type[] args) {
		int localIndex = 1;
		for (Type type : args)
			localIndex += type.getSize();

		return localIndex;
	}

	public static FieldNode copyField(FieldNode node, String newName) {
		FieldNode result = new FieldNode(node.access, newName, node.desc, node.signature, node.value);

		result.invisibleAnnotations = node.invisibleAnnotations;
		result.visibleAnnotations = node.visibleAnnotations;
		result.attrs = node.attrs;
		return result;
	}

	public final static Method CONSTRUCTOR = new Method("<init>", "()V");
	public final static String SUB_CTOR_NAME = "$subinit";
}
