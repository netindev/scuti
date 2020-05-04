package tk.netindev.scuti.gui;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tk.netindev.scuti.core.Scuti;
import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.configuration.option.obfuscation.ClassEncrypt;
import tk.netindev.scuti.core.configuration.option.obfuscation.MiscellaneousObfuscation;
import tk.netindev.scuti.core.configuration.option.obfuscation.NumberObfuscation;
import tk.netindev.scuti.core.configuration.option.obfuscation.RenameMembers;
import tk.netindev.scuti.core.configuration.option.obfuscation.StringEncryption;
import tk.netindev.scuti.core.configuration.option.shrinking.UnusedMembers;
import tk.netindev.scuti.core.transform.Transformer;

/**
 *
 * @author netindeva
 *
 */
public class Display extends Application {

    public static final double VERSION = 1.1D;

    private double x = 0, y = 0;

    @Override
    public void start(final Stage stage) throws Exception {
	final FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/FXMLDocument.fxml"));
	final Parent root = loader.load();
	stage.initStyle(StageStyle.UNDECORATED);

	root.setOnMousePressed(event -> {
	    Display.this.x = event.getSceneX();
	    Display.this.y = event.getSceneY();
	});

	root.setOnMouseDragged(event -> {
	    stage.setX(event.getScreenX() - Display.this.x);
	    stage.setY(event.getScreenY() - Display.this.y);
	});

	final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
	stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
	stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);

	final Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());

	stage.getIcons().add(new Image("/images/icon.png"));
	stage.setScene(scene);
	stage.show();
    }

    public static void main(final String[] args) throws Throwable {
	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	//if (loadCore()) {
	    launch(args);
	//} else {
	//    JOptionPane.showMessageDialog(null,
	//	    "Couldn't load core, please check if there is a \"scuti-core.jar\" file.");
	//}
    }

    private static boolean loadCore() {
	File file = new File("scuti-core.jar");
	if (!file.exists() || !file.canRead()) {
	    return false;
	}
	try {
	    URL url = file.toURI().toURL();
	    URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
	    method.setAccessible(true);
	    method.invoke(classLoader, url);
	    return true;
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }

}
