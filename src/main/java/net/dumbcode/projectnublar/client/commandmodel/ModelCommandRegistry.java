package net.dumbcode.projectnublar.client.commandmodel;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ilexiconn.llibrary.client.util.Matrix;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.vecmath.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelCommandRegistry {
    private static final Map<String, Command> commandMap = Maps.newHashMap();

    public static void register(String commandName, Command command) {
        if(commandMap.containsKey(commandName)) {
            throw new IllegalArgumentException("Command " + commandName + " is already registered. Current: " + commandMap.get(commandName).getClass().getSimpleName() + " New:" + command.getClass().getSimpleName());
        }
        commandMap.put(commandName, command);
    }

    public static Command get(String commandName) {
        if(!commandMap.containsKey(commandName)) {
            throw new IllegalArgumentException("No command found: " + commandName);
        }
        return commandMap.get(commandName);
    }

    public interface Command {
        void applyChanges(IBakedModel model, String args);
    }

    static {

        //Move to function ?
        Function<IBakedModel, Iterable<BakedQuad>> modelToQuads = model -> {
            Set<BakedQuad> quadSet = Sets.newLinkedHashSet(model.getQuads(Blocks.STONE.getDefaultState(), null, 0L));
            for (EnumFacing facing : EnumFacing.values()) {
                quadSet.addAll(model.getQuads(Blocks.STONE.getDefaultState(), facing, 0L));
            }
            return quadSet;
        };

        Pattern norPat = Pattern.compile("(\\d*\\.?\\d*),(\\d*\\.?\\d*),(\\d*\\.?\\d*)@(\\d*\\.?\\d*),(\\d*\\.?\\d*),(\\d*\\.?\\d*)");

        register("rotate", (model, args) -> {
            Matcher matcher = norPat.matcher(args);
            if(!matcher.find()) {
                throw new IllegalArgumentException("Could not find match");
            }

            double[] ds = new double[6];
            for (int i = 0; i < 6; i++) {
                ds[i] = Double.valueOf(matcher.group(i+1));
            }

            Matrix4d mat = new Matrix4d();
            mat.setIdentity();

            Matrix4d m = new Matrix4d();

            m.setIdentity();
            m.rotZ(Math.toRadians(ds[2]));
            mat.mul(m);

            m.setIdentity();
            m.rotY(Math.toRadians(ds[1]));
            mat.mul(m);

            m.setIdentity();
            m.rotX(Math.toRadians(ds[0]));
            mat.mul(m);


            //TODO: i am assuming that POSITION comes first in the vertex format. maybe fix this?
            for (BakedQuad quad : modelToQuads.apply(model)) {
                int[] data = quad.getVertexData();
                float[][][] datum = null;
                if(quad instanceof UnpackedBakedQuad) {
                    datum = ObfuscationReflectionHelper.getPrivateValue(UnpackedBakedQuad.class, (UnpackedBakedQuad) quad, "unpackedData");
                }
                for (int v = 0; v < 4; v++) {
                    int o = data.length / 4 * v;
                    Point3d pos = new Point3d(Float.intBitsToFloat(data[o])-ds[3], Float.intBitsToFloat(data[o+1])-ds[4], Float.intBitsToFloat(data[o+2])-ds[5]);
                    mat.transform(pos);
                    data[o] =   Float.floatToRawIntBits((float) (pos.x+ds[3]));
                    data[o+1] = Float.floatToRawIntBits((float) (pos.y+ds[4]));
                    data[o+2] = Float.floatToRawIntBits((float) (pos.z+ds[5]));

                    if(datum != null) {
                        datum[v][0][0] = (float) (pos.x+ds[3]);
                        datum[v][0][1] = (float) (pos.y+ds[4]);
                        datum[v][0][2] = (float) (pos.z+ds[5]);
                    }
                }
            }


        });

        register("interpolate-scale", (model, args) -> {
            Matcher matcher = norPat.matcher(args);
            if(!matcher.find()) {
                throw new IllegalArgumentException("Could not find match");
            }
            double[] ds = new double[6];//((x-8)*s)+8
            for (int i = 0; i < 6; i++) {
                ds[i] = Double.valueOf(matcher.group(i+1));
            }
            for (BakedQuad quad : modelToQuads.apply(model)) {
                int[] data = quad.getVertexData();
                float[][][] datum = null;
                if(quad instanceof UnpackedBakedQuad) {
                    datum = ObfuscationReflectionHelper.getPrivateValue(UnpackedBakedQuad.class, (UnpackedBakedQuad) quad, "unpackedData");
                }
                for (int v = 0; v < 4; v++) {
                    int o = data.length / 4 * v;
                    float x = (float) ((Float.intBitsToFloat(data[o  ])-ds[3])*ds[0]+ds[3]);
                    float y = (float) ((Float.intBitsToFloat(data[o+1])-ds[4])*ds[1]+ds[4]);
                    float z = (float) ((Float.intBitsToFloat(data[o+2])-ds[5])*ds[2]+ds[5]);
                    data[o  ] = Float.floatToRawIntBits(x);
                    data[o+1] = Float.floatToRawIntBits(y);
                    data[o+2] = Float.floatToRawIntBits(z);

                    if(datum != null) {
                        datum[v][0][0] = x;
                        datum[v][0][1] = y;
                        datum[v][0][2] = z;
                    }
                }
            }

        });
    }

}
