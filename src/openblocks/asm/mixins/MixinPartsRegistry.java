package openblocks.asm.mixins;

import java.util.Map;

import openblocks.asm.mixins.internals.BaseClassInfo;
import openblocks.asm.mixins.internals.MixinClassInfo;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class MixinPartsRegistry {

	private MixinPartsRegistry() {}

	public final static Type MIXIN_ANNOTATION = Type.getType(Mixin.class);

	public final static MixinPartsRegistry instance = new MixinPartsRegistry();

	private static int builderCounter = 0;

	private Map<Type, MixinClassInfo> mixinCls = Maps.newHashMap();

	// public final static Type OBJECT_CLASS = Type.getType(Object.class);

	public <T> ExtClassBuilder<T> createBuilder(Class<T> baseClass) {
		BaseClassInfo info = new BaseClassInfo(baseClass);
		return new ExtClassBuilder<T>(builderCounter++, this, info);
	}

	public <T> Class<? extends T> createClass(Class<T> baseClass, Class<?>... mixins) {
		ExtClassBuilder<T> builder = createBuilder(baseClass);
		for (Class<?> mixin : mixins)
			builder.addImplementation(mixin);
		return builder.build(getClass().getClassLoader());
	}

	MixinClassInfo getExtensionInfo(Class<?> cls) {
		MixinClassInfo info = mixinCls.get(Type.getType(cls));

		if (info == null) info = tryFindMixin(cls);

		return info;
	}

	private MixinClassInfo tryFindMixin(Class<?> cls) {
		// Mixin marker = cls.getAnnotation(Mixin.class);
		// Preconditions.checkNotNull(marker, "Class is not valid mixin");
		Preconditions.checkArgument(cls.getSuperclass() == Object.class, "Mixin class '%s' supertype != Object", cls);

		byte[] bytes = ClassLoaderAccess.getTransformedBytes(cls.getCanonicalName());

		ClassReader cr = new ClassReader(bytes);
		ClassNode cn = new ClassNode(Opcodes.ASM4);
		cr.accept(cn, 0);

		MixinClassInfo info = new MixinClassInfo();
		cn.accept(info);
		mixinCls.put(Type.getObjectType(cn.name), info);
		return info;
	}
}
