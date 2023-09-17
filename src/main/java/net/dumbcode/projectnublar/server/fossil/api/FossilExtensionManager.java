package net.dumbcode.projectnublar.server.fossil.api;

import com.google.common.base.Stopwatch;
import net.dumbcode.projectnublar.server.fossil.api.context.FossilRegistrationContext;
import net.dumbcode.projectnublar.server.fossil.api.context.StoneTypeRegistrationContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

public class FossilExtensionManager {
    public static final List<IFossilExtension> EXTENSIONS = new ArrayList<>();
    public static final Logger LOGGER = getLogger("projectnublar-extensions");

    public static void initialize() {
        LOGGER.info("Initializing Fossil Extensions");
        Stopwatch stopwatch = Stopwatch.createStarted();
        registerAnnotatedExtensions();
        onFossilRegister();
        onStoneTypeRegister();
        LOGGER.info("Initialized Fossil Extensions, \033[0;31mTook {}\033[0;0m", stopwatch);
    }

    public static void onFossilRegister() {
        LOGGER.info("Registering Fossils");
        Stopwatch stopwatch = Stopwatch.createStarted();
        EXTENSIONS.forEach((extension -> {
            LOGGER.info("Starting Registration Of Fossils using Extension {}", extension.getName());
            Stopwatch stopwatch2 = Stopwatch.createStarted();
            extension.registerFossils(new FossilRegistrationContext());
            LOGGER.info("Finished Registration Of Fossils using Extension {}, \033[0;31mTook {}\033[0;0m", extension.getName(), stopwatch2);
        }));
        LOGGER.info("Finished Registering Fossils, \033[0;31mTook {}\033[0;0m", stopwatch);
    }

    /**
     * <b><h2><i>INTERNAL USE ONLY! DO NOT CALL</i></h2></b>
     */
    public static void onStoneTypeRegister() {
        LOGGER.info("Registering Stone Types");
        Stopwatch stopwatch = Stopwatch.createStarted();
        EXTENSIONS.forEach((extension -> {
            LOGGER.info("Starting Registration Of Stone Types using Extension {}", extension.getName());
            Stopwatch stopwatch2 = Stopwatch.createStarted();
            extension.registerStoneTypes(new StoneTypeRegistrationContext());
            LOGGER.info("Finished Registration Of Stone Types using Extension {}, \033[0;31mTook {}\033[0;0m", extension.getName(), stopwatch2);
        }));
        LOGGER.info("Finished Registering Stone Types, \033[0;31mTook {}\033[0;0m", stopwatch);
    }

    /**
     * Utilizes {@link #findInstances} in order to find all fossil extension classes
     * @return The list of fossil extension classes
     */
    public static List<IFossilExtension> findAllAnnotatedFossilExtensions() {
        return findInstances(Extension.class, IFossilExtension.class);
    }
    
    /**
     * Registers an extension manually
     * Not recommended
     * @param extension the extension to register
     */
    public static <T extends IFossilExtension> void registerExtension(T extension) {
        EXTENSIONS.add(extension);
    }

    /**
     * Finds all classes with a certain annotation that extend a certain class
     * @param annotationClass the class that something has to be annotated with in order to be added
     * @param instanceClass the class that something has to extend to be added (if you want none, use {@link Object})
     * @return All classes that match these parameters
     */
    @SuppressWarnings("SameParameterValue")
    private static <T> List<T> findInstances(Class<?> annotationClass, Class<T> instanceClass) {
        Type annotationType = Type.getType(annotationClass);
        List<ModFileScanData> allScanData = ModList.get().getAllScanData();
        Set<String> extensionClassNames = new LinkedHashSet<>();
        LOGGER.info("Finding Extension Classes By Annotation");
        Stopwatch stopwatch2 = Stopwatch.createStarted();
        for (ModFileScanData scanData : allScanData) {
            Iterable<ModFileScanData.AnnotationData> annotations = scanData.getAnnotations();
            for (ModFileScanData.AnnotationData a : annotations) {
                if (Objects.equals(a.getAnnotationType(), annotationType)) {
                    String memberName = a.getMemberName();
                    extensionClassNames.add(memberName);
                    String[] splitName = memberName.split("\\.");
                    LOGGER.info("Found Extension {}", splitName[splitName.length - 1]);
                }
            }
        }
        LOGGER.info("Found Extension Classes By Annotation, \033[0;31mTook {}\033[0;0m", stopwatch2);
        List<T> instances = new ArrayList<>();
        LOGGER.info("Adding All Found Extensions");
        Stopwatch stopwatch3 = Stopwatch.createStarted();
        for (String className : extensionClassNames) {
            try {
                String[] splitName = className.split("\\.");
                Stopwatch stopwatch = Stopwatch.createStarted();
                Class<?> asmClass = Class.forName(className);
                Stopwatch stopwatch1 = Stopwatch.createStarted();
                LOGGER.info("Constructing Extension {}", splitName[splitName.length - 1]);
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                Constructor<? extends T> constructor = asmInstanceClass.getDeclaredConstructor();
                T instance = constructor.newInstance();
                LOGGER.info("Finished Constructing Extension {}, \033[0;31mTook {}\033[0;0m", splitName[splitName.length - 1], stopwatch1);
                instances.add(instance);
                LOGGER.info("Found & Added Extension {}, \033[0;31mTook {}\033[0;0m", splitName[splitName.length - 1], stopwatch);
            } catch (ReflectiveOperationException | LinkageError e) {
                LOGGER.error("Failed to load: {}", className, e);
            }
        }
        LOGGER.info("Finished Adding All Found Extensions, \033[0;31mTook {}\033[0;0m", stopwatch3);
        return instances;
    }

    /**
     * Registers all classes with {@link Extension @Extension} annotation that implement {@link IFossilExtension}
     * Also prints some debug log stuff
     */
    public static void registerAnnotatedExtensions() {
        LOGGER.info("Automatically Registering Extensions");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stopwatch findStopwatch = Stopwatch.createStarted();
        LOGGER.info("Finding Extensions");
        List<IFossilExtension> extensions = findAllAnnotatedFossilExtensions();
        LOGGER.info("Found All Extensions, \033[0;31mTook {}\033[0;0m", findStopwatch);
        LOGGER.info("Registering Found Extensions");
        Stopwatch registerStopwatch = Stopwatch.createStarted();
        extensions.forEach(extension -> {
            LOGGER.info("Registering Extension {}", extension.getName());
            Stopwatch stopwatch2 = Stopwatch.createStarted();
            registerExtension(extension);
            LOGGER.info("Finished Registering Extension {}, \033[0;31mTook {}\033[0;0m", extension.getName(), stopwatch2);
            LOGGER.info("Executing On Register Tasks For Extension {}", extension.getName());
            Stopwatch stopwatch3 = Stopwatch.createStarted();
            LOGGER.info("Finished On Register Tasks Extension {}, \033[0;31mTook {}\033[0;0m", extension.getName(), stopwatch3);
        });
        LOGGER.info("Finished Registering Found Extensions, \033[0;31mTook {}\033[0;0m", registerStopwatch);
        LOGGER.info("Finished Automatically Registering Extensions, \033[0;31mTotal Time Taken: {}\033[0;0m", stopwatch);
    }
}
