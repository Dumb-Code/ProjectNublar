package net.dumbcode.projectnublar.client.commandmodel;

import com.google.common.collect.Maps;
import net.ilexiconn.llibrary.client.util.Matrix;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.Map;
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

        EnumFacing[] allNull = new EnumFacing[EnumFacing.values().length+1];
        System.arraycopy(EnumFacing.values(), 0, allNull, 0, EnumFacing.values().length);
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


            for (EnumFacing facing : allNull) {
                for (BakedQuad quad : model.getQuads(Blocks.STONE.getDefaultState(), facing, 0L)) {
                    int[] data = quad.getVertexData();
                    for (int v = 0; v < 4; v++) {
                        int o = data.length / 4 * v;
                        Vector3d pos = new Vector3d(Float.intBitsToFloat(data[o])-ds[3], Float.intBitsToFloat(data[o+1])-ds[4], Float.intBitsToFloat(data[o+2])-ds[5]);
                        mat.transform(pos);
                        data[o] = Float.floatToRawIntBits((float) (pos.x+ds[3]));
                        data[o+1] = Float.floatToRawIntBits((float) (pos.y+ds[4]));
                        data[o+2] = Float.floatToRawIntBits((float) (pos.z+ds[5]));
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

            for (EnumFacing facing : allNull) {
                for (BakedQuad quad : model.getQuads(Blocks.STONE.getDefaultState(), facing, 0L)) {
                    int[] data = quad.getVertexData();
                    for (int v = 0; v < 4; v++) {
                        int o = data.length / 4 * v;
                        data[o  ] = Float.floatToRawIntBits((float) ((Float.intBitsToFloat(data[o  ])-ds[3])*ds[0]+ds[3]));
                        data[o+1] = Float.floatToRawIntBits((float) ((Float.intBitsToFloat(data[o+1])-ds[4])*ds[1]+ds[4]));
                        data[o+2] = Float.floatToRawIntBits((float) ((Float.intBitsToFloat(data[o+2])-ds[5])*ds[2]+ds[5]));
                    }
                }
            }

        });

    }

}
