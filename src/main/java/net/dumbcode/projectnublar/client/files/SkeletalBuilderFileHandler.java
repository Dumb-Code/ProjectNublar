package net.dumbcode.projectnublar.client.files;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Cleanup;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.util.Strings;

import javax.vecmath.Vector3f;
import java.io.*;
import java.net.FileNameMap;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SkeletalBuilderFileHandler {

    public static void serilize(SkeletalBuilderFileInfomation infomation, File file) {
        if(file == null) {
            return;
        }
        if(!file.exists() && Strings.isEmpty(FilenameUtils.getExtension(file.getName()))) {
            file = new File(file.getParentFile(), file.getName() + ".dpose");
        }
        StringBuilder outBuilder = new StringBuilder(infomation.getDinosaurLocation().toString() + "\n");
        infomation.getPoseData().forEach((s, v) -> {
            outBuilder.append(s);
            outBuilder.append("\n");
            outBuilder.append(v.x);
            outBuilder.append(" ");
            outBuilder.append(v.y);
            outBuilder.append(" ");
            outBuilder.append(v.z);
            outBuilder.append("\n");
        });
        String out = outBuilder.toString().trim();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ZipOutputStream zos = new ZipOutputStream(bos);

            try {
                zos.putNextEntry(new ZipEntry("pose"));
                zos.write(out.getBytes());
                zos.closeEntry();
            }
            finally {
                zos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static SkeletalBuilderFileInfomation deserilize(File file) {
        Map<String, Vector3f> map = Maps.newHashMap();
        ResourceLocation fileLocation = null;
        try {
            InputStream stream = null;
            @Cleanup FileInputStream fis = new FileInputStream(file);
            ZipInputStream zip = new ZipInputStream(fis);
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals("pose")) {
                    stream = zip;
                    break;
                }
            }
            if(stream == null) {
                throw new IOException("No pose file present");
            }
            int lineNum = 0;
            String prevName = "";
            for (String line : Lists.newArrayList(IOUtils.readLines(stream, "UTF-8"))) {
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
