package tanks.effect;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tanks.Panel;

import java.util.HashMap;
import java.util.UUID;

public class AttributeModifier
{
	public enum Operation {add, multiply}

	/** A unique name for the modifier, to prevent double effects*/
	public String name = UUID.randomUUID().toString();
	
	/**Duration of the Attribute Modifier, leave at 0 for indefinite duration*/
	public double duration = 0;
	
	/**Age at which the Attribute starts to wear off*/
	public double deteriorationAge = 0;
	
	/**Age at which the Attribute is at full strength*/
	public double warmupAge = 0;
	public double value;
	public Operation operation;
	public double age;
	public boolean expired = false;
	public Type type;
	public static HashMap<String, Type> attributeModifierTypes = new HashMap<>();

	/** Changes the speed at which objects move */
	public static final Type velocity = new Type("velocity");
	/** Changes the speed at which status effects run out */
	public static final Type clock_speed = new Type("clock_speed");
	/** Affects ability of tanks to slow down */
	public static final Type friction = new Type("friction");
	/** Affects ability of tanks to speed up */
	public static final Type acceleration = new Type("acceleration");
	/** Changes the maximum speed a tank may move at */
	public static final Type max_speed = new Type("max_speed");
	/** When applied to a tank, its bullets will spawn boosted */
	public static final Type bullet_boost = new Type("bullet_boost");
	/** Changes glow of an object */
	public static final Type glow = new Type("glow");
	/** Causes objects to spawn a particle trail of embers */
	public static final Type ember_effect = new Type("effect");
	/** When applied to a tank, its bullets will have their speed modified */
	public static final Type bullet_speed = new Type("bullet_speed");
	/** When applied to a tank, its cooldown will change accordingly */
	public static final Type reload = new Type("reload");
	/** When applied to a tank, its bullet recoil will change accordingly */
	public static final Type recoil = new Type("recoil");
	/** When applied to a tank, its will show a green shield indicating health was added via a heal ray */
	public static final Type healray = new Type("healray");

	public static class Type
	{
		public String name;

		public Type(String name)
		{
			this.name = name;
			attributeModifierTypes.put(name, this);
		}

		public boolean equals(Type other)
		{
			return this.name.equals(other.name);
		}
	}
	
	public AttributeModifier(Type type, Operation op, double amount)
	{
		this.type = type;
		this.operation = op;
		this.value = amount;
	}
	
	public AttributeModifier(String name, Type type, Operation op, double amount)
	{
		this(type, op, amount);
		this.name = name;
	}
	
	public void update()
	{
		this.age += Panel.frameFrequency;
	
		if (this.duration > 0 && this.age >= this.duration)
			this.expired = true;
	}
	
	public double getValue(double in)
	{
		double val;
		
		if (this.expired)
			return in;

        if (this.age < this.warmupAge)
            val = this.value * this.age / this.warmupAge;
        else if (this.age < this.deteriorationAge || this.deteriorationAge <= 0)
            val = this.value;
        else
            val = this.value * (this.duration - this.age) / (this.duration - this.deteriorationAge);

        if (this.operation == Operation.add)
			return in + val;
		else if (this.operation == Operation.multiply)
			return in * (val + 1);
		else
			return in;
	}

	public static class Instance
	{
		public final AttributeModifier.Type type;
		/** All AttributeModifiers that modify 'type'. */
		public ObjectArrayList<AttributeModifier> attributeList = new ObjectArrayList<>();

		public Instance(AttributeModifier.Type type)
		{
			this.type = type;
		}

		/**
		 * Applies all attribute modifiers of this type to the given base value,
		 * in no guaranteed order.
		 */
		public double apply(double baseValue)
		{
			for (AttributeModifier m : attributeList)
			{
				if (!m.expired)
					baseValue = m.getValue(baseValue);
			}
			return baseValue;
		}

		public AttributeModifier last()
		{
			return attributeList.get(attributeList.size() - 1);
		}

		public double age()
		{
			return last().age;
		}

		public double deteriorationAge()
		{
			return last().deteriorationAge;
		}

		public double duration()
		{
			return last().duration;
		}

		public boolean isEmpty()
		{
			return attributeList.isEmpty();
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof AttributeModifier)
			return name.equals(((AttributeModifier) obj).name);
		return super.equals(obj);
	}
}
