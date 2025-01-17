package net.minecraft.server;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NBTTagCompound implements NBTBase {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern c = Pattern.compile("[A-Za-z0-9._+-]+");
    public static final NBTTagType<NBTTagCompound> a = new NBTTagType<NBTTagCompound>() {
        @Override
        public NBTTagCompound b(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.a(384L);
            if (i > 512) {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            } else {
                it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<String, NBTBase> hashmap = new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>(8, 0.8f); // Paper - reduce memory footprint of NBTTagCompound

                byte b0;

                while ((b0 = NBTTagCompound.c(datainput, nbtreadlimiter)) != 0) {
                    String s = NBTTagCompound.d(datainput, nbtreadlimiter);

                    nbtreadlimiter.a((long) (224 + 16 * s.length()));
                    NBTBase nbtbase = NBTTagCompound.b(NBTTagTypes.a(b0), s, datainput, i + 1, nbtreadlimiter);

                    if (hashmap.put(s, nbtbase) != null) {
                        nbtreadlimiter.a(288L);
                    }
                }

                return new NBTTagCompound(hashmap);
            }
        }

        @Override
        public String a() {
            return "COMPOUND";
        }

        @Override
        public String b() {
            return "TAG_Compound";
        }
    };
    public final Map<String, NBTBase> map; // Paper

    private NBTTagCompound(Map<String, NBTBase> map) {
        this.map = map;
    }

    public NBTTagCompound() {
        this(new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>(8, 0.8f)); // Paper - reduce memory footprint of NBTTagCompound
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        Iterator iterator = this.map.keySet().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            NBTBase nbtbase = (NBTBase) this.map.get(s);

            a(s, nbtbase, dataoutput);
        }

        dataoutput.writeByte(0);
    }

    public Set<String> getKeys() {
        return this.map.keySet();
    }

    @Override
    public byte getTypeId() {
        return 10;
    }

    @Override
    public NBTTagType<NBTTagCompound> b() {
        return NBTTagCompound.a;
    }

    public int e() {
        return this.map.size();
    }

    @Nullable
    public NBTBase set(String s, NBTBase nbtbase) {
        return (NBTBase) this.map.put(s, nbtbase);
    }

    public void setByte(String s, byte b0) {
        this.map.put(s, NBTTagByte.a(b0));
    }

    public void setShort(String s, short short0) {
        this.map.put(s, NBTTagShort.a(short0));
    }

    public void setInt(String s, int i) {
        this.map.put(s, NBTTagInt.a(i));
    }

    public void setLong(String s, long i) {
        this.map.put(s, NBTTagLong.a(i));
    }

    public void setUUID(String prefix, UUID uuid) { a(prefix, uuid); } // Paper - OBFHELPER
    public void a(String s, UUID uuid) {
        this.setLong(s + "Most", uuid.getMostSignificantBits());
        this.setLong(s + "Least", uuid.getLeastSignificantBits());
    }


    @Nullable public UUID getUUID(String prefix) { return a(prefix); } // Paper - OBFHELPER
    @Nullable
    public UUID a(String s) {
        return new UUID(this.getLong(s + "Most"), this.getLong(s + "Least"));
    }

    public final boolean hasUUID(String s) { return this.b(s); } public boolean b(String s) { // Paper - OBFHELPER
        return this.hasKeyOfType(s + "Most", 99) && this.hasKeyOfType(s + "Least", 99);
    }

    public void c(String s) {
        this.remove(s + "Most");
        this.remove(s + "Least");
    }

    public void setFloat(String s, float f) {
        this.map.put(s, NBTTagFloat.a(f));
    }

    public void setDouble(String s, double d0) {
        this.map.put(s, NBTTagDouble.a(d0));
    }

    public void setString(String s, String s1) {
        this.map.put(s, NBTTagString.a(s1));
    }

    public void setByteArray(String s, byte[] abyte) {
        this.map.put(s, new NBTTagByteArray(abyte));
    }

    public void setIntArray(String s, int[] aint) {
        this.map.put(s, new NBTTagIntArray(aint));
    }

    public void b(String s, List<Integer> list) {
        this.map.put(s, new NBTTagIntArray(list));
    }

    public void a(String s, long[] along) {
        this.map.put(s, new NBTTagLongArray(along));
    }

    public void c(String s, List<Long> list) {
        this.map.put(s, new NBTTagLongArray(list));
    }

    public void setBoolean(String s, boolean flag) {
        this.map.put(s, NBTTagByte.a(flag));
    }

    @Nullable
    public NBTBase get(String s) {
        return (NBTBase) this.map.get(s);
    }

    public byte e(String s) {
        NBTBase nbtbase = (NBTBase) this.map.get(s);

        return nbtbase == null ? 0 : nbtbase.getTypeId();
    }

    public boolean hasKey(String s) {
        return this.map.containsKey(s);
    }

    public boolean hasKeyOfType(String s, int i) {
        byte b0 = this.e(s);

        return b0 == i ? true : (i != 99 ? false : b0 == 1 || b0 == 2 || b0 == 3 || b0 == 4 || b0 == 5 || b0 == 6);
    }

    public byte getByte(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).asByte();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return 0;
    }

    public short getShort(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).asShort();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return 0;
    }

    public int getInt(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).asInt();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return 0;
    }

    public long getLong(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).asLong();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return 0L;
    }

    public float getFloat(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).asFloat();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return 0.0F;
    }

    public double getDouble(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).asDouble();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return 0.0D;
    }

    public String getString(String s) {
        try {
            if (this.hasKeyOfType(s, 8)) {
                return ((NBTBase) this.map.get(s)).asString();
            }
        } catch (ClassCastException classcastexception) {
            ;
        }

        return "";
    }

    public byte[] getByteArray(String s) {
        try {
            if (this.hasKeyOfType(s, 7)) {
                return ((NBTTagByteArray) this.map.get(s)).getBytes();
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, NBTTagByteArray.a, classcastexception));
        }

        return new byte[0];
    }

    public int[] getIntArray(String s) {
        try {
            if (this.hasKeyOfType(s, 11)) {
                return ((NBTTagIntArray) this.map.get(s)).getInts();
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, NBTTagIntArray.a, classcastexception));
        }

        return new int[0];
    }

    public long[] getLongArray(String s) {
        try {
            if (this.hasKeyOfType(s, 12)) {
                return ((NBTTagLongArray) this.map.get(s)).getLongs();
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, NBTTagLongArray.a, classcastexception));
        }

        return new long[0];
    }

    public NBTTagCompound getCompound(String s) {
        try {
            if (this.hasKeyOfType(s, 10)) {
                return (NBTTagCompound) this.map.get(s);
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, NBTTagCompound.a, classcastexception));
        }

        return new NBTTagCompound();
    }

    public NBTTagList getList(String s, int i) {
        try {
            if (this.e(s) == 9) {
                NBTTagList nbttaglist = (NBTTagList) this.map.get(s);

                if (!nbttaglist.isEmpty() && nbttaglist.a_() != i) {
                    return new NBTTagList();
                }

                return nbttaglist;
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, NBTTagList.a, classcastexception));
        }

        return new NBTTagList();
    }

    public boolean getBoolean(String s) {
        return this.getByte(s) != 0;
    }

    public void remove(String s) {
        this.map.remove(s);
    }

    @Override
    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("{");
        Collection<String> collection = this.map.keySet();

        if (NBTTagCompound.LOGGER.isDebugEnabled()) {
            List<String> list = Lists.newArrayList(this.map.keySet());

            Collections.sort(list);
            collection = list;
        }

        String s;

        for (Iterator iterator = ((Collection) collection).iterator(); iterator.hasNext(); stringbuilder.append(t(s)).append(':').append(this.map.get(s))) {
            s = (String) iterator.next();
            if (stringbuilder.length() != 1) {
                stringbuilder.append(',');
            }
        }

        return stringbuilder.append('}').toString();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    private CrashReport a(String s, NBTTagType<?> nbttagtype, ClassCastException classcastexception) {
        CrashReport crashreport = CrashReport.a(classcastexception, "Reading NBT data");
        CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Corrupt NBT tag", 1);

        crashreportsystemdetails.a("Tag type found", () -> {
            return ((NBTBase) this.map.get(s)).b().a();
        });
        crashreportsystemdetails.a("Tag type expected", nbttagtype::a);
        crashreportsystemdetails.a("Tag name", (Object) s);
        return crashreport;
    }

    @Override
    public NBTTagCompound clone() {
        // Paper start - reduce memory footprint of NBTTagCompound
        it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<String, NBTBase> ret = new it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap<>(this.map.size(), 0.8f);

        Iterator<Map.Entry<String, NBTBase>> iterator = (this.map instanceof it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap) ? ((it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap)this.map).object2ObjectEntrySet().fastIterator() : this.map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, NBTBase> entry = iterator.next();
            ret.put(entry.getKey(), entry.getValue().clone());
        }

        return new NBTTagCompound(ret);
        // Paper end - reduce memory footprint of NBTTagCompound
    }

    public boolean equals(Object object) {
        return this == object ? true : object instanceof NBTTagCompound && Objects.equals(this.map, ((NBTTagCompound) object).map);
    }

    public int hashCode() {
        return this.map.hashCode();
    }

    private static void a(String s, NBTBase nbtbase, DataOutput dataoutput) throws IOException {
        dataoutput.writeByte(nbtbase.getTypeId());
        if (nbtbase.getTypeId() != 0) {
            dataoutput.writeUTF(s);
            nbtbase.write(dataoutput);
        }
    }

    private static byte c(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        return datainput.readByte();
    }

    private static String d(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        return datainput.readUTF();
    }

    private static NBTBase b(NBTTagType<?> nbttagtype, String s, DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) {
        try {
            return nbttagtype.b(datainput, i, nbtreadlimiter);
        } catch (IOException ioexception) {
            CrashReport crashreport = CrashReport.a(ioexception, "Loading NBT data");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("NBT Tag");

            crashreportsystemdetails.a("Tag name", (Object) s);
            crashreportsystemdetails.a("Tag type", (Object) nbttagtype.a());
            throw new ReportedException(crashreport);
        }
    }

    public NBTTagCompound a(NBTTagCompound nbttagcompound) {
        Iterator iterator = nbttagcompound.map.keySet().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            NBTBase nbtbase = (NBTBase) nbttagcompound.map.get(s);

            if (nbtbase.getTypeId() == 10) {
                if (this.hasKeyOfType(s, 10)) {
                    NBTTagCompound nbttagcompound1 = this.getCompound(s);

                    nbttagcompound1.a((NBTTagCompound) nbtbase);
                } else {
                    this.set(s, nbtbase.clone());
                }
            } else {
                this.set(s, nbtbase.clone());
            }
        }

        return this;
    }

    protected static String t(String s) {
        return NBTTagCompound.c.matcher(s).matches() ? s : NBTTagString.b(s);
    }

    protected static IChatBaseComponent u(String s) {
        if (NBTTagCompound.c.matcher(s).matches()) {
            return (new ChatComponentText(s)).a(NBTTagCompound.d);
        } else {
            String s1 = NBTTagString.b(s);
            String s2 = s1.substring(0, 1);
            IChatBaseComponent ichatbasecomponent = (new ChatComponentText(s1.substring(1, s1.length() - 1))).a(NBTTagCompound.d);

            return (new ChatComponentText(s2)).addSibling(ichatbasecomponent).a(s2);
        }
    }

    @Override
    public IChatBaseComponent a(String s, int i) {
        if (this.map.isEmpty()) {
            return new ChatComponentText("{}");
        } else {
            ChatComponentText chatcomponenttext = new ChatComponentText("{");
            Collection<String> collection = this.map.keySet();

            if (NBTTagCompound.LOGGER.isDebugEnabled()) {
                List<String> list = Lists.newArrayList(this.map.keySet());

                Collections.sort(list);
                collection = list;
            }

            if (!s.isEmpty()) {
                chatcomponenttext.a("\n");
            }

            IChatBaseComponent ichatbasecomponent;

            for (Iterator iterator = ((Collection) collection).iterator(); iterator.hasNext(); chatcomponenttext.addSibling(ichatbasecomponent)) {
                String s1 = (String) iterator.next();

                ichatbasecomponent = (new ChatComponentText(Strings.repeat(s, i + 1))).addSibling(u(s1)).a(String.valueOf(':')).a(" ").addSibling(((NBTBase) this.map.get(s1)).a(s, i + 1));
                if (iterator.hasNext()) {
                    ichatbasecomponent.a(String.valueOf(',')).a(s.isEmpty() ? " " : "\n");
                }
            }

            if (!s.isEmpty()) {
                chatcomponenttext.a("\n").a(Strings.repeat(s, i));
            }

            chatcomponenttext.a("}");
            return chatcomponenttext;
        }
    }
}
