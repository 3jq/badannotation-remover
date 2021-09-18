package wtf.fuckyou.badannotationremover;

import com.google.common.io.ByteStreams;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalStateException("YOOO WHAT THE FUCK IS GOING ON NOT ENOUGH ARGUMENTS!!!!!!!!!!!");
        }

        String input = args[0];
        String output = args[1];

        if (!new File(input).exists()) {
            throw new IllegalStateException("YOOOOOOOOOO DUDE WHAT THA FUCK IS GOING ON INPUT FILE DOESN'T EXIST SHEEEEEEEESH!!!!");
        }

        final Map<ClassNode, String> classes = new HashMap<>();
        final Map<String, byte[]> resources = new HashMap<>();

        final ZipInputStream zis = new ZipInputStream(new FileInputStream(input));
        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {
            String name = entry.getName();
            byte[] bytes = ByteStreams.toByteArray(zis);

            if (!name.endsWith(".class")) {
                System.out.println("LOL GO FUCK YOURSELF");
                resources.put(name, bytes);
                continue;
            }

            ClassNode node = new ClassNode();
            new ClassReader(bytes).accept(node, 0);

            classes.put(node, name);
        }

        classes.forEach((clazz, name) -> {
            for (FieldNode field : clazz.fields) {
                if (field.invisibleAnnotations == null) continue;

                for (AnnotationNode annotation : field.invisibleAnnotations) {
                    field.invisibleAnnotations.remove(annotation);
                    System.out.println("FUCKED ANNOTATION " + annotation.desc + " IN FIELD " + field.name);
                }

            }

            for (MethodNode method : clazz.methods) {
                if (method.invisibleAnnotations == null) continue;

                for (AnnotationNode annotation : method.invisibleAnnotations) {
                    method.invisibleAnnotations.remove(annotation);
                    System.out.println("FUCKED ANNOTATION " + annotation.desc + " IN METHOD " + method.name);
                }

            }

            System.out.println("EZ-PZ REMOVED ALL THE INVISIBLE ANNOTATIONS FROM CLASS " + clazz.name);
        });

        System.out.println("WRITING DA FUCKING JAR");

        Files.deleteIfExists(Paths.get(output));

        ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(Paths.get(output), StandardOpenOption.CREATE, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));

        classes.forEach((node, name) -> {
            try {
                zipOutputStream.putNextEntry(new ZipEntry(name));

                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(writer);
                zipOutputStream.write(writer.toByteArray());

                zipOutputStream.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        resources.forEach((name, bytes) -> {
            try {
                zipOutputStream.putNextEntry(new ZipEntry(name));
                zipOutputStream.write(bytes);
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        zipOutputStream.close();
    }
}
