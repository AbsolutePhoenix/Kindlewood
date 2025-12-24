package com.absolutephoenix.kindlewood.game.ui;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final String title;
    private int width;
    private int height;

    private boolean vsync = true;
    private boolean resizable = true;

    private long handle;

    // Fullscreen tracking
    private boolean fullscreen = false;
    private int windowedX, windowedY, windowedW, windowedH;

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void setVsync(boolean vsync) { this.vsync = vsync; }
    public void setResizable(boolean resizable) { this.resizable = resizable; }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) throw new RuntimeException("Failed to create GLFW window");

        // Center the window
        GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vid != null) {
            glfwSetWindowPos(handle, (vid.width() - width) / 2, (vid.height() - height) / 2);
        }

        // Callbacks
        glfwSetFramebufferSizeCallback(handle, (win, w, h) -> {
            width = Math.max(1, w);
            height = Math.max(1, h);
            glViewport(0, 0, width, height);
        });

        glfwSetKeyCallback(handle, (win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_ESCAPE) {
                    glfwSetWindowShouldClose(handle, true);
                } else if (key == GLFW_KEY_F11) {
                    toggleFullscreen();
                } else if (key == GLFW_KEY_V) {
                    setVsync(!vsync);
                }
            }
        });

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(vsync ? 1 : 0);
        glfwShowWindow(handle);

        GL.createCapabilities();

        // Initial viewport
        glViewport(0, 0, width, height);

        // Basic GL state
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        IconLoader.setWindowIconFromResources(handle,
                "/assets/window_icons/kindlewood_icon_16.png",
                "/assets/window_icons/kindlewood_icon_24.png",
                "/assets/window_icons/kindlewood_icon_32.png",
                "/assets/window_icons/kindlewood_icon_48.png",
                "/assets/window_icons/kindlewood_icon_64.png",
                "/assets/window_icons/kindlewood_icon_128.png",
                "/assets/window_icons/kindlewood_icon_256.png",
                "/assets/window_icons/kindlewood_icon_512.png"
        );
    }

    public void beginFrame() {
        glClearColor(0.06f, 0.05f, 0.07f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endFrame() {
        // placeholder (UI, debug, etc.)
    }

    public void pollEvents() { glfwPollEvents(); }
    public void swapBuffers() { glfwSwapBuffers(handle); }
    public boolean shouldClose() { return glfwWindowShouldClose(handle); }
    public void markShouldClsoe(){glfwSetWindowShouldClose(handle, true);}
    public void setTitle(String title) {
        glfwSetWindowTitle(handle, title);
    }
    public boolean isVsync() {
        return vsync;
    }
    public boolean isMinimized() {
        if (handle == 0) return false;
        return glfwGetWindowAttrib(handle, GLFW_ICONIFIED) == GLFW_TRUE;
    }

    public void destroy() {
        glfwDestroyWindow(handle);
        glfwTerminate();
        GLFWErrorCallback cb = glfwSetErrorCallback(null);
        if (cb != null) cb.free();
    }

    private void toggleFullscreen() {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vid = glfwGetVideoMode(monitor);
        if (vid == null) return;

        if (!fullscreen) {
            // save windowed bounds
            try (MemoryStack stack = stackPush()) {
                IntBuffer px = stack.mallocInt(1);
                IntBuffer py = stack.mallocInt(1);
                IntBuffer pw = stack.mallocInt(1);
                IntBuffer ph = stack.mallocInt(1);
                glfwGetWindowPos(handle, px, py);
                glfwGetWindowSize(handle, pw, ph);
                windowedX = px.get(0);
                windowedY = py.get(0);
                windowedW = pw.get(0);
                windowedH = ph.get(0);
            }

            glfwSetWindowMonitor(handle, monitor, 0, 0, vid.width(), vid.height(), vid.refreshRate());
            fullscreen = true;
        } else {
            glfwSetWindowMonitor(handle, NULL, windowedX, windowedY, windowedW, windowedH, 0);
            fullscreen = false;
        }
    }
}
