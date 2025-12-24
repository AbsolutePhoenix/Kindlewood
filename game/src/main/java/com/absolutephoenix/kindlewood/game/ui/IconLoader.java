package com.absolutephoenix.kindlewood.game.ui;

import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.*;

public class IconLoader {

    public static void setWindowIconFromResources(long windowHandle, String... resourcePaths) {
        ByteBuffer[] rawBuffers = new ByteBuffer[resourcePaths.length];
        ByteBuffer[] pixelBuffers = new ByteBuffer[resourcePaths.length];

        try (MemoryStack stack = MemoryStack.stackPush()) {
            GLFWImage.Buffer icons = GLFWImage.malloc(resourcePaths.length, stack);

            for (int i = 0; i < resourcePaths.length; i++) {
                String path = resourcePaths[i];

                byte[] bytes;
                try (InputStream is = IconLoader.class.getResourceAsStream(path)) {
                    if (is == null) {
                        System.err.println("Icon resource not found: " + path);
                        return;
                    }
                    bytes = is.readAllBytes();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                rawBuffers[i] = memAlloc(bytes.length);
                rawBuffers[i].put(bytes).flip();

                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);

                pixelBuffers[i] = stbi_load_from_memory(rawBuffers[i], w, h, comp, 4);
                if (pixelBuffers[i] == null) {
                    System.err.println("Failed to decode icon " + path + ": " + stbi_failure_reason());
                    return;
                }

                icons.position(i);
                icons.width(w.get(0));
                icons.height(h.get(0));
                icons.pixels(pixelBuffers[i]);
            }

            icons.position(0);
            glfwSetWindowIcon(windowHandle, icons);

        } finally {
            for (ByteBuffer p : pixelBuffers) if (p != null) stbi_image_free(p);
            for (ByteBuffer b : rawBuffers) if (b != null) memFree(b);
        }
    }
}
