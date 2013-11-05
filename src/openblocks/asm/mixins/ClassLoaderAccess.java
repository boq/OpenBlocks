package openblocks.asm.mixins;

import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

import com.google.common.base.Throwables;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.relauncher.IFMLCallHook;

public class ClassLoaderAccess {
	private static LaunchClassLoader loader;

	static byte[] getTransformedBytes(String className) {
		String rawName = FMLDeobfuscatingRemapper.INSTANCE.unmap(className);
		String transformedName = FMLDeobfuscatingRemapper.INSTANCE.map(className);
		try {
			byte[] bytes = loader.getClassBytes(rawName);

			for (IClassTransformer transformer : loader.getTransformers())
				bytes = transformer.transform(rawName, transformedName, bytes);

			return bytes;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public static class Setup implements IFMLCallHook {
		@Override
		public Void call() throws Exception {
			return null;
		}

		@Override
		public void injectData(Map<String, Object> data) {
			loader = (LaunchClassLoader)data.get("classLoader");
		}
	}
}
