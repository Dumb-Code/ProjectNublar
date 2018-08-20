package net.dumbcode.projectnublar.client.files;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.util.Strings;

import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.util.List;
import java.util.Map;

public class SkeletalBuilderFileHandler {

    public static void serilize(SkeletalBuilderFileInfomation infomation, File file) {
        if(!file.exists() && Strings.isEmpty(FilenameUtils.getExtension(file.getName()))) {
            file = new File(file.getParentFile(), file.getName() + ".dpose");
        }
        List<String> lines = Lists.newArrayList(infomation.getDinosaurLocation().toString());
        infomation.getPoseData().forEach((s, v) -> lines.add(s + "\n" + v.x + " " + v.y + " " + v.z));
        try {
            FileUtils.writeLines(file, lines);
        } catch (IOException e) {
            ProjectNublar.getLogger().error("Unable to save file", e);
        }
    }

    public static SkeletalBuilderFileInfomation deserilize(File file) {
        Map<String, Vector3f> map = Maps.newHashMap();
        ResourceLocation fileLocation = null;
        try {
            int lineNum = 0;
            String prevName = "";
            for (String line : FileUtils.readLines(file, "UTF-8")) {
                line = line.trim(); //maybe not needed ?
                if(lineNum == 0) {
                    fileLocation = new ResourceLocation(line);
                } else {
                    if(lineNum % 2 == 1) {
                        prevName = line;
                    } else {
                        String[] astr = line.split(" ");
                        map.put(prevName, new Vector3f(Float.valueOf(astr[0]), Float.valueOf(astr[1]), Float.valueOf(astr[2])));

                    }
                }
                lineNum++;
            }
        } catch (IOException e) {
            ProjectNublar.getLogger().error("Unable to load file" + e);
        }
        return new SkeletalBuilderFileInfomation(fileLocation, map);
    }
}
