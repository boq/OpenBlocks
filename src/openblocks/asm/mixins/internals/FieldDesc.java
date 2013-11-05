package openblocks.asm.mixins.internals;

import org.objectweb.asm.Type;

public class FieldDesc {

	public final String name;

	public final Type type;

	public FieldDesc(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public FieldDesc(String name, String desc) {
		this(name, Type.getType(desc));
	}

	@Override
	public String toString() {
		return name + ":" + type;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof FieldDesc) {
			FieldDesc other = (FieldDesc)o;
			return name.equals(other.name) && type.equals(other.type);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ type.hashCode();
	}

	@Override
	public FieldDesc clone() {
		return new FieldDesc(name, type);
	}
}
