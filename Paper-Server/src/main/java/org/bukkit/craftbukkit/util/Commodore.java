package org.bukkit.craftbukkit.util;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.bukkit.Material;
import org.bukkit.plugin.AuthorNagException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;

/**
 * This file is imported from Commodore.
 *
 * @author md_5
 */
// CHECKSTYLE:OFF
public class Commodore
{

    private static final Set<String> EVIL = new HashSet<>( Arrays.asList(
            "org/bukkit/World (III)I getBlockTypeIdAt",
            "org/bukkit/World (Lorg/bukkit/Location;)I getBlockTypeIdAt",
            "org/bukkit/block/Block ()I getTypeId",
            "org/bukkit/block/Block (I)Z setTypeId",
            "org/bukkit/block/Block (IZ)Z setTypeId",
            "org/bukkit/block/Block (IBZ)Z setTypeIdAndData",
            "org/bukkit/block/Block (B)V setData",
            "org/bukkit/block/Block (BZ)V setData",
            "org/bukkit/inventory/ItemStack ()I getTypeId",
            "org/bukkit/inventory/ItemStack (I)V setTypeId"
    ) );

    // Paper start - Plugin rewrites
    private static final Map<String, String> SEARCH_AND_REMOVE = initReplacementsMap();
    private static Map<String, String> initReplacementsMap()
    {
        Map<String, String> getAndRemove = new HashMap<>();
        // Be wary of maven shade's relocations
        getAndRemove.put( "org/bukkit/".concat( "craftbukkit/libs/it/unimi/dsi/fastutil/" ), "org/bukkit/".concat( "craftbukkit/libs/" ) ); // Remap fastutil to our location

        if ( Boolean.getBoolean( "debug.rewriteForIde" ) )
        {
            // unversion incoming calls for pre-relocate debug work
            final String NMS_REVISION_PACKAGE = "v1_13_R2/";

            getAndRemove.put( "net/minecraft/".concat( "server/" + NMS_REVISION_PACKAGE ), NMS_REVISION_PACKAGE );
            getAndRemove.put( "org/bukkit/".concat( "craftbukkit/" + NMS_REVISION_PACKAGE ), NMS_REVISION_PACKAGE );
        }

        return getAndRemove;
    }

    @Nonnull
    private static String getOriginalOrRewrite(@Nonnull String original)
    {
        String rewrite = null;
        for ( Map.Entry<String, String> entry : SEARCH_AND_REMOVE.entrySet() )
        {
            if ( original.contains( entry.getKey() ) )
            {
                rewrite = original.replace( entry.getValue(), "" );
            }
        }

        return rewrite != null ? rewrite : original;
    }
    // Paper end

    public static void main(String[] args)
    {
        OptionParser parser = new OptionParser();
        OptionSpec<File> inputFlag = parser.acceptsAll( Arrays.asList( "i", "input" ) ).withRequiredArg().ofType( File.class ).required();
        OptionSpec<File> outputFlag = parser.acceptsAll( Arrays.asList( "o", "output" ) ).withRequiredArg().ofType( File.class ).required();

        OptionSet options = parser.parse( args );

        File input = options.valueOf( inputFlag );
        File output = options.valueOf( outputFlag );

        if ( input.isDirectory() )
        {
            if ( !output.isDirectory() )
            {
                System.err.println( "If input directory specified, output directory required too" );
                return;
            }

            for ( File in : input.listFiles() )
            {
                if ( in.getName().endsWith( ".jar" ) )
                {
                    convert( in, new File( output, in.getName() ) );
                }
            }
        } else
        {
            convert( input, output );
        }
    }

    private static void convert(File in, File out)
    {
        System.out.println( "Attempting to convert " + in + " to " + out );

        try
        {
            try ( JarFile inJar = new JarFile( in, false ) )
            {
                JarEntry entry = inJar.getJarEntry( ".commodore" );
                if ( entry != null )
                {
                    return;
                }

                try ( JarOutputStream outJar = new JarOutputStream( new FileOutputStream( out ) ) )
                {
                    for ( Enumeration<JarEntry> entries = inJar.entries(); entries.hasMoreElements(); )
                    {
                        entry = entries.nextElement();

                        try ( InputStream is = inJar.getInputStream( entry ) )
                        {
                            byte[] b = ByteStreams.toByteArray( is );

                            if ( entry.getName().endsWith( ".class" ) )
                            {
                                b = convert( b, false );
                                entry = new JarEntry( entry.getName() );
                            }

                            outJar.putNextEntry( entry );
                            outJar.write( b );
                        }
                    }

                    outJar.putNextEntry( new ZipEntry( ".commodore" ) );
                }
            }
        } catch ( Exception ex )
        {
            System.err.println( "Fatal error trying to convert " + in );
            ex.printStackTrace();
        }
    }

    public static byte[] convert(byte[] b, final boolean modern)
    {
        ClassReader cr = new ClassReader( b );
        ClassWriter cw = new ClassWriter( cr, 0 );

        cr.accept( new ClassVisitor( Opcodes.ASM8, cw) // Paper
        {
            // Paper start - Rewrite plugins
            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
            {
                desc = getOriginalOrRewrite( desc );
                if ( signature != null ) {
                    signature = getOriginalOrRewrite( signature );
                }

                return super.visitField( access, name, desc, signature, value) ;
            }
            // Paper end

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
            {
                return new MethodVisitor( api, super.visitMethod( access, name, desc, signature, exceptions ) )
                {
                    // Paper start - Plugin rewrites
                    @Override
                    public void visitInvokeDynamicInsn(String name, String desc, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments)
                    {
                        // Paper start - Rewrite plugins
                        name = getOriginalOrRewrite( name );
                        if ( desc != null )
                        {
                            desc = getOriginalOrRewrite( desc );
                        }
                        // Paper end

                        super.visitInvokeDynamicInsn( name, desc, bootstrapMethodHandle, bootstrapMethodArguments );
                    }

                    @Override
                    public void visitTypeInsn(int opcode, String type)
                    {
                        type = getOriginalOrRewrite( type );

                        super.visitTypeInsn( opcode, type );
                    }

                    @Override
                    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                        for ( int i = 0; i < local.length; i++ )
                        {
                            if ( !( local[i] instanceof String ) ) { continue; }

                            local[i] = getOriginalOrRewrite( (String) local[i] );
                        }

                        for ( int i = 0; i < stack.length; i++ )
                        {
                            if ( !( stack[i] instanceof String ) ) { continue; }

                            stack[i] = getOriginalOrRewrite( (String) stack[i] );
                        }

                        super.visitFrame( type, nLocal, local, nStack, stack );
                    }



                    @Override
                    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index)
                    {
                        descriptor = getOriginalOrRewrite( descriptor );

                        super.visitLocalVariable( name, descriptor, signature, start, end, index );
                    }
                    // Paper end



                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String desc)
                    {
                        // Paper start - Rewrite plugins
                        owner = getOriginalOrRewrite( owner );
                        if ( desc != null )
                        {
                            desc = getOriginalOrRewrite( desc );
                        }
                        // Paper end

                        if ( modern )
                        {
                            if ( owner.equals( "org/bukkit/Material" ) )
                            {
                                switch ( name )
                                {
                                    case "CACTUS_GREEN":
                                        name = "GREEN_DYE";
                                        break;
                                    case "DANDELION_YELLOW":
                                        name = "YELLOW_DYE";
                                        break;
                                    case "ROSE_RED":
                                        name = "RED_DYE";
                                        break;
                                    case "SIGN":
                                        name = "OAK_SIGN";
                                        break;
                                    case "WALL_SIGN":
                                        name = "OAK_WALL_SIGN";
                                        break;
                                }
                            }

                            super.visitFieldInsn( opcode, owner, name, desc );
                            return;
                        }

                        if ( owner.equals( "org/bukkit/Material" ) )
                        {
                            try
                            {
                                Material.valueOf( "LEGACY_" + name );
                            } catch ( IllegalArgumentException ex )
                            {
                                throw new AuthorNagException( "No legacy enum constant for " + name + ". Did you forget to define a modern (1.13+) api-version in your plugin.yml?" );
                            }

                            super.visitFieldInsn( opcode, owner, "LEGACY_" + name, desc );
                            return;
                        }

                        if ( owner.equals( "org/bukkit/Art" ) )
                        {
                            switch ( name )
                            {
                                case "BURNINGSKULL":
                                    super.visitFieldInsn( opcode, owner, "BURNING_SKULL", desc );
                                    return;
                                case "DONKEYKONG":
                                    super.visitFieldInsn( opcode, owner, "DONKEY_KONG", desc );
                                    return;
                            }
                        }

                        if ( owner.equals( "org/bukkit/DyeColor" ) )
                        {
                            switch ( name )
                            {
                                case "SILVER":
                                    super.visitFieldInsn( opcode, owner, "LIGHT_GRAY", desc );
                                    return;
                            }
                        }

                        if ( owner.equals( "org/bukkit/Particle" ) )
                        {
                            switch ( name )
                            {
                                case "BLOCK_CRACK":
                                case "BLOCK_DUST":
                                case "FALLING_DUST":
                                    super.visitFieldInsn( opcode, owner, "LEGACY_" + name, desc );
                                    return;
                            }
                        }

                        super.visitFieldInsn( opcode, owner, name, desc );
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf)
                    {
                        // SPIGOT-4496
                        if ( owner.equals( "org/bukkit/map/MapView" ) && name.equals( "getId" ) && desc.equals( "()S" ) )
                        {
                            // Should be same size on stack so just call other method
                            super.visitMethodInsn( opcode, owner, name, "()I", itf );
                            return;
                        }
                        // SPIGOT-4608
                        if ( (owner.equals( "org/bukkit/Bukkit" ) || owner.equals( "org/bukkit/Server" ) ) && name.equals( "getMap" ) && desc.equals( "(S)Lorg/bukkit/map/MapView;" ) )
                        {
                            // Should be same size on stack so just call other method
                            super.visitMethodInsn( opcode, owner, name, "(I)Lorg/bukkit/map/MapView;", itf );
                            return;
                        }

                        // Paper start - Rewrite plugins
                        owner = getOriginalOrRewrite( owner) ;
                        if (desc != null)
                        {
                            desc = getOriginalOrRewrite(desc);
                        }
                        // Paper end

                        if ( modern )
                        {
                            if ( owner.equals( "org/bukkit/Material" ) )
                            {
                                switch ( name )
                                {
                                    case "values":
                                        super.visitMethodInsn( opcode, "org/bukkit/craftbukkit/util/CraftLegacy", "modern_" + name, desc, itf );
                                        return;
                                    case "ordinal":
                                        super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/bukkit/craftbukkit/util/CraftLegacy", "modern_" + name, "(Lorg/bukkit/Material;)I", false );
                                        return;
                                }
                            }

                            super.visitMethodInsn( opcode, owner, name, desc, itf );
                            return;
                        }

                        if ( owner.equals( "org/bukkit/ChunkSnapshot" ) && name.equals( "getBlockData" ) && desc.equals( "(III)I" ) )
                        {
                            super.visitMethodInsn( opcode, owner, "getData", desc, itf );
                            return;
                        }

                        Type retType = Type.getReturnType( desc );

                        if ( EVIL.contains( owner + " " + desc + " " + name )
                                || ( owner.startsWith( "org/bukkit/block/" ) && ( desc + " " + name ).equals( "()I getTypeId" ) )
                                || ( owner.startsWith( "org/bukkit/block/" ) && ( desc + " " + name ).equals( "(I)Z setTypeId" ) )
                                || ( owner.startsWith( "org/bukkit/block/" ) && ( desc + " " + name ).equals( "()Lorg/bukkit/Material; getType" ) ) )
                        {
                            Type[] args = Type.getArgumentTypes( desc );
                            Type[] newArgs = new Type[ args.length + 1 ];
                            newArgs[0] = Type.getObjectType( owner );
                            System.arraycopy( args, 0, newArgs, 1, args.length );

                            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/bukkit/craftbukkit/legacy/CraftEvil", name, Type.getMethodDescriptor( retType, newArgs ), false );
                            return;
                        }

                        if ( owner.equals( "org/bukkit/DyeColor" ) )
                        {
                            if ( name.equals( "valueOf" ) && desc.equals( "(Ljava/lang/String;)Lorg/bukkit/DyeColor;" ) )
                            {
                                super.visitMethodInsn( opcode, owner, "legacyValueOf", desc, itf );
                                return;
                            }
                        }

                        if ( owner.equals( "org/bukkit/Material" ) )
                        {
                            if ( name.equals( "getMaterial" ) && desc.equals( "(I)Lorg/bukkit/Material;" ) )
                            {
                                super.visitMethodInsn( opcode, "org/bukkit/craftbukkit/legacy/CraftEvil", name, desc, itf );
                                return;
                            }

                            switch ( name )
                            {
                                case "values":
                                case "valueOf":
                                case "getMaterial":
                                case "matchMaterial":
                                    super.visitMethodInsn( opcode, "org/bukkit/craftbukkit/legacy/CraftLegacy", name, desc, itf );
                                    return;
                                case "ordinal":
                                    super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/bukkit/craftbukkit/legacy/CraftLegacy", "ordinal", "(Lorg/bukkit/Material;)I", false );
                                    return;
                                case "name":
                                case "toString":
                                    super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/bukkit/craftbukkit/legacy/CraftLegacy", name, "(Lorg/bukkit/Material;)Ljava/lang/String;", false );
                                    return;
                            }
                        }

                        if ( retType.getSort() == Type.OBJECT && retType.getInternalName().equals( "org/bukkit/Material" ) && owner.startsWith( "org/bukkit" ) )
                        {
                            super.visitMethodInsn( opcode, owner, name, desc, itf );
                            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/bukkit/craftbukkit/legacy/CraftLegacy", "toLegacy", "(Lorg/bukkit/Material;)Lorg/bukkit/Material;", false );
                            return;
                        }

                        super.visitMethodInsn( opcode, owner, name, desc, itf );
                    }
                };
            }
        }, 0 );

        return cw.toByteArray();
    }
}
