package openblocks.asm.mixins.internals;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import openblocks.Log;
import openblocks.asm.mixins.ExtensionPoint;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MixinClassInfo extends ClassVisitor {
	public final static Type EXTENSION_POINT = Type.getType(ExtensionPoint.class);

	private Map<FieldDesc, FieldNode> fields = Maps.newHashMap();
	private Map<Method, MethodNode> methods = Maps.newHashMap();
	private Type selfType;
	private Set<Method> privateMethods = Sets.newHashSet();
	private Set<Method> publicMethods = Sets.newHashSet();
	private Set<Method> abstractMethods = Sets.newHashSet();
	private Set<Method> extensionMethods = Sets.newHashSet();
	private Set<Type> interfaces = Sets.newHashSet();
	private Method constructor;

	public MixinClassInfo() {
		super(Opcodes.ASM4);
	}

	private class DefaultMethodVisitor extends MethodNode {

		public DefaultMethodVisitor(int access, String name, String desc, String signature, String[] exceptions) {
			super(access, name, desc, signature, exceptions);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			Type annotation = Type.getType(desc);
			if (annotation.equals(EXTENSION_POINT)) {
				extensionMethods.add(new Method(name, this.desc));
				if (Modifier.isPublic(access)) {
					Method m = new Method(name, this.desc);
					Log.warn("Extension method %s from class %s will be renamed, but is public", m, selfType.getClassName());
				}
			}

			return super.visitAnnotation(desc, visible);
		}

		@Override
		public void visitLineNumber(int line, Label start) {}
	}

	private class ConstructorMethodVisitor extends MethodNode {
		public ConstructorMethodVisitor(int access, String name, String desc, String signature, String[] exceptions) {
			super(access, name, desc, signature, exceptions);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) visitInsn(Opcodes.POP); // eat
																									// 'this'
																									// from
																									// stack
			else super.visitMethodInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitLineNumber(int line, Label start) {}
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaceNames) {
		selfType = Type.getObjectType(name);

		for (String intf : interfaceNames)
			interfaces.add(Type.getObjectType(intf));
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if (Modifier.isStatic(access)) return null;

		FieldNode node = new FieldNode(access, name, desc, signature, value);
		fields.put(new FieldDesc(name, desc), node);
		return node;
	}

	@Override
	public MethodNode visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		Method method = new Method(name, desc);

		if (Modifier.isStatic(access)) return null;

		if (Modifier.isAbstract(access)) abstractMethods.add(method);
		else if (Modifier.isPublic(access)) publicMethods.add(method);
		else privateMethods.add(method);

		MethodNode node;
		if (name.equals("<init>")) {
			Preconditions.checkArgument(constructor == null, "Only one constructor per mixin allowed");
			constructor = method;
			Preconditions.checkArgument(desc.equals("()V"), "Mixin can only have paramless constructor");
			node = new ConstructorMethodVisitor(access, name, desc, signature, exceptions);
		} else node = new DefaultMethodVisitor(access, name, desc, signature, exceptions);

		methods.put(method, node);
		return node;
	}

	public Appender createAppender(String newClassName, String postfix) {
		return new Appender(newClassName, postfix);
	}

	public class Appender {
		public final String newClassName;
		public final String postfix;
		private Map<Method, String> methodsToAdd = Maps.newHashMap();
		private Map<FieldDesc, String> fieldsToAdd = Maps.newHashMap();

		public Appender(String newClassName, String postfix) {
			this.newClassName = newClassName;
			this.postfix = postfix;

			for (Method m : publicMethods)
				methodsToAdd.put(m, null);

			for (Method m : privateMethods)
				methodsToAdd.put(m, m.getName() + "$" + postfix);

			for (Method m : extensionMethods)
				methodsToAdd.put(m, m.getName() + "$" + postfix);

			methodsToAdd.put(constructor, Helper.SUB_CTOR_NAME + "$" + postfix);

			for (FieldDesc d : fields.keySet())
				fieldsToAdd.put(d, d.name + "$" + postfix);
		}

		public void addToClass(ClassNode cls, Set<Method> allMethods, Set<Method> missingMethods) {
			String className = selfType.getInternalName();
			for (Map.Entry<Method, String> e : methodsToAdd.entrySet()) {
				Method method = e.getKey();
				String name = e.getValue();

				if (name == null) name = method.getName();

				Method newMethod = new Method(name, method.getDescriptor());

				if (allMethods.contains(newMethod)) continue;
				Preconditions.checkArgument(!allMethods.contains(newMethod), "Method conflict on '%s' while adding class '%s' to class '%s'", newMethod, className, cls.name);

				MethodNode original = methods.get(method);
				Preconditions.checkNotNull(original, "Selected wrong method to add: '%s', class: '%s'", method, className);

				MethodNode result = new RenameClassAdapter(name, original, className, newClassName, methodsToAdd, fieldsToAdd);

				original.instructions.resetLabels();
				original.accept(result);

				cls.methods.add(result);
				allMethods.add(method);
			}

			missingMethods.addAll(abstractMethods);

			for (Map.Entry<FieldDesc, String> e : fieldsToAdd.entrySet()) {
				FieldDesc field = e.getKey();
				String name = e.getValue();

				FieldNode original = fields.get(field);
				Preconditions.checkNotNull(original, "Selected wrong field to add: '%s', class: '%s'", field, className);

				cls.fields.add(Helper.copyField(original, name));
			}

			Set<String> clsInterfaces = Sets.newHashSet(cls.interfaces);

			for (Type t : interfaces) {
				String name = t.getInternalName();
				if (!clsInterfaces.contains(name)) {
					clsInterfaces.add(name);
					cls.interfaces.add(name);
				}
			}
		}

		public String getExtMethodName(Method m) {
			if (extensionMethods.contains(m) || m.equals(constructor)) return methodsToAdd.get(m);

			return null;
		}
	}
}
