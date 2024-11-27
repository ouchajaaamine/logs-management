package com.example;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import java.nio.file.Paths;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;
import com.teamdev.jxbrowser.view.javafx.BrowserView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class LogMonitoringApp extends Application {

    @Override
    public void start(Stage stage) {
        // Set up JxBrowser Engine with hardware acceleration
        EngineOptions options = EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                .licenseKey("OK6AEKNYF2JOKOYOR3NKEBSBKT2JTJI0XI3O7Z26JLXVGTSF5N9GQKVZ0VF0LWSMEEK4JWCN9N5KYRDKD23XMW8B6Q0P1IGPE0HOE4DQVEKPEQ9CV2RH8BHGK9U8V4Q1470XLIP711VIGED5Y")
                .build();
        Engine engine = Engine.newInstance(options);

        // Create the browser instance
        Browser browser = engine.newBrowser();

        // Create the BrowserView to embed in JavaFX scene
        BrowserView view = BrowserView.newInstance(browser);

        // Set up the layout and scene
        Scene scene = new Scene(new BorderPane(view), 1280, 700);

        // Start Jetty server to serve the HTML file
        Server server = new Server(8080);  // Jetty server on port 8080
        ResourceHandler handler = new ResourceHandler();
        try {
            handler.setBaseResource(Resource.newResource(Paths.get("src/main/resources").toUri()));
            server.setHandler(handler);
            server.start();
            System.out.println("Server started on http://localhost:8080");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Load the HTML file via the Jetty server URL
        browser.navigation().loadUrl("http://localhost:8080/index.html");

        // Set up the JavaFX window
        stage.setScene(scene);
        stage.setTitle("Log Monitoring Dashboard");
        stage.show();
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Wait for 5 seconds
                browser.navigation().reload(); // Reload the page
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        // Ensure the server shuts down when the application closes
        stage.setOnCloseRequest(event -> {
            browser.close();
            engine.close();
            try {
                server.stop();  // Stop the Jetty server on exit
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
