package openblocks.asm.mixins.internals;

import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.ImmutableMap;

final class RenameClassAdapter extends MethodNode {
	private final Map<FieldDesc, String> fieldRenames;
	private final String oldClass;
	private final Map<Method, String> methodRenames;
	private final String newClass;

	private static String[] stupidJavaErasure(List<?> list) {
		@SuppressWarnings("unchecked")
		List<String> tmp = (List<String>)list;
		return tmp.toArray(new String[tmp.size()]);
	}

	public RenameClassAdapter(String newName, MethodNode node, String oldClass, String newClass, Map<Method, String> methodRenames,
			Map<FieldDesc, String> fieldRenames) {
		super(node.access, newName, node.desc, node.signature, stupidJavaErasure(node.exceptions));
		this.fieldRenames = fieldRenames;
		this.oldClass = oldClass;
		this.methodRenames = methodRenames;
		this.newClass = newClass;
	}

	public RenameClassAdapter(String newName, MethodNode node, String oldClass, String newClass) {
		this(newName, node, oldClass, newClass, ImmutableMap.<Method, String> of(), ImmutableMap.<FieldDesc, String> of());
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if ((opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKESPECIAL) && owner.equals(oldClass)) {
			Method method = new Method(name, desc);
			owner = newClass;

			String rename = methodRenames.get(method);
			if (rename != null) name = rename;
		}

		super.visitMethodInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		if ((opcode == Opcodes.GETFIELD || opcode == Opcodes.PUTFIELD) && owner.equals(oldClass)) {
			FieldDesc field = new FieldDesc(name, desc);
			owner = newClass;

			String rename = fieldRenames.get(field);
			if (rename != null) name = rename;
		}
		super.visitFieldInsn(opcode, owner, name, desc);
	}
}
