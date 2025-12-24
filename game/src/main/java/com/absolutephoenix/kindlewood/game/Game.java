package com.absolutephoenix.kindlewood.game;

import com.absolutephoenix.kindlewood.game.ui.Window;

public class Game {
    Window window;

    private int frames = 0;
    private double fpsTimer = 0.0;
    private int fps = 0;

    public void start() {
        window = new Window(1280, 720, "KindleWood");
        window.setVsync(false);
        window.setResizable(true);

        run();
    }

    private void run(){
        init();
        loop();
        cleanup();
    }

    private  void init(){
        window.init();
    }

    private void loop() {
        final double dt = 1.0 / 60.0;

        long lastTime = System.nanoTime();
        double accumulator = 0.0;

        int frames = 0;
        double fpsTimer = 0.0;

        while (!window.shouldClose()) {
            long now = System.nanoTime();
            double frameTime = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            frameTime = Math.min(frameTime, 0.25);

            accumulator += frameTime;

            window.pollEvents();

            while (accumulator >= dt) {
                update((float) dt);
                accumulator -= dt;
            }

            render();
            window.swapBuffers();

            frames++;
            fpsTimer += frameTime;
            if (fpsTimer >= 1.0) {
                window.setTitle("KindleWood | FPS: " + frames);
                frames = 0;
                fpsTimer -= 1.0;
            }

            // Only throttle when vsync is OFF or minimized
            if (!window.isVsync() || window.isMinimized()) {
                Thread.yield();
                try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    private void update(float delta){

    }

    private void render(){
        window.beginFrame();

        window.endFrame();
    }

    public void stop(){
        window.markShouldClsoe();
    }

    private void cleanup(){
        window.destroy();
    }
}
