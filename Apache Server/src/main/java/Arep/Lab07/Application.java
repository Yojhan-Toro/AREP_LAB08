package Arep.Lab07;

import Arep.Lab07.controller.MathController;

import java.io.IOException;
import java.net.URISyntaxException;

public class Application {

    public static void main(String[] args) throws IOException, URISyntaxException {

        HttpServer.staticfiles("/webroot/public");

        // Ruta raíz → redirige al login
        HttpServer.get("/", (req, res) -> {
            return "<script>window.location='/login.html'</script>";
        });

        // Apagado elegante
        HttpServer.get("/shutdown", (req, res) -> {
            new Thread(() -> {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                System.out.println("Shutdown requested via /shutdown endpoint.");
                System.exit(0);
            }, "shutdown-trigger-thread").start();
            return "<h2>Server shutting down gracefully... Goodbye!</h2>";
        });

        MicroSpringBoot.run(MathController.class, args);
    }
}
