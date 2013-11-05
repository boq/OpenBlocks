package openblocks.asm.mixins;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import openblocks.asm.mixins.internals.BaseClassInfo;
import openblocks.asm.mixins.internals.ExtMethodBuilder;
import openblocks.asm.mixins.internals.Helper;
import openblocks.asm.mixins.internals.MixinClassInfo;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ExtClassBuilder<T> {

	public static boolean DUMP_CODE = false;

	private static java.lang.reflect.Method defineClass;
	private final MixinPartsRegistry registry;
	private final BaseClassInfo baseClass;

	private final String newClassName;
	private Class<T> buildClass;

	private List<MixinClassInfo.Appender> implementations = Lists.newArrayList();
	private Set<String> postfixes = Sets.newHashSet();
	private boolean modified = true;

	ExtClassBuilder(int id, MixinPartsRegistry registry, BaseClassInfo baseClass) {
		this.baseClass = baseClass;
		this.registry = registry;

		newClassName = baseClass.selfType.getInternalName() + "$extended$" + id;
	}

	public ExtClassBuilder<T> addImplementation(Class<?> implementation) {
		MixinClassInfo info = registry.getExtensionInfo(implementation);

		String prefix = implementation.getName();
		prefix = prefix.substring(prefix.lastIndexOf('.') + 1);

		String postfix = prefix;

		int i = 0;
		while (postfixes.contains(postfix))
			postfix = prefix + "$" + i++;

		postfixes.add(postfix);

		implementations.add(info.createAppender(newClassName, postfix));
		modified = true;
		return this;
	}

	public Class<T> get(ClassLoader loader) {
		if (modified) return build(loader);

		return buildClass;
	}

	@SuppressWarnings("unchecked")
	public Class<T> build(ClassLoader loader) {
		Preconditions.checkState(buildClass == null, "Trying to redefine class!");
		if (implementations.isEmpty()) buildClass = (Class<T>)baseClass.cls;
		else {
			ClassNode node = new ClassNode();
			node.superName = baseClass.selfType.getInternalName();
			node.name = newClassName;
			node.access = Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL;
			node.version = Opcodes.V1_6;

			List<ExtMethodBuilder> extMethodsBuilders = Lists.newArrayList();

			for (Method ext : baseClass.extensionMethods)
				extMethodsBuilders.add(new ExtMethodBuilder(ext, ext.getName(), Opcodes.ACC_PUBLIC, false));

			extMethodsBuilders.add(new ExtMethodBuilder(Helper.CONSTRUCTOR, Helper.SUB_CTOR_NAME, Opcodes.ACC_PRIVATE, true));

			Set<Method> allMethods = Sets.newHashSet(baseClass.implementedMethods);
			Set<Method> abstractMethods = Sets.newHashSet(baseClass.abstractMethods);

			for (MixinClassInfo.Appender builder : implementations) {
				builder.addToClass(node, allMethods, abstractMethods);

				for (ExtMethodBuilder extBuilder : extMethodsBuilders) {
					String extMethod = builder.getExtMethodName(extBuilder.method);
					if (extMethod != null) extBuilder.addSubMethod(extMethod);
				}
			}

			for (ExtMethodBuilder extBuilder : extMethodsBuilders)
				extBuilder.buildMergeMethod(node);

			Set<Method> stillAbstract = Sets.difference(abstractMethods, allMethods);
			Preconditions.checkState(stillAbstract.isEmpty(), "Class %s still has abstract methods: %s", newClassName, stillAbstract);
			baseClass.addConstructors(node);

			node.access &= ~Opcodes.ACC_ABSTRACT;

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

			if (DUMP_CODE) {
				PrintWriter pw = new PrintWriter(System.out);
				TraceClassVisitor tcv = new TraceClassVisitor(null, pw);
				node.accept(tcv);
				pw.flush();
			}

			CheckClassAdapter ca = new CheckClassAdapter(writer, true);
			node.accept(ca);

			byte[] newBytes = writer.toByteArray();
			buildClass = defineClass(loader, newClassName.replace('/', '.'), newBytes);
		}

		modified = false;
		return buildClass;
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> defineClass(ClassLoader cl, String name, byte[] bytes) {
		try {
			if (defineClass == null) {
				defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
				defineClass.setAccessible(true);
			}
			return (Class<T>)defineClass.invoke(cl, name, bytes, 0, bytes.length);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
