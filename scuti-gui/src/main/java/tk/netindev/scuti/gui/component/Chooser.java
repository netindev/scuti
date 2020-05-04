package tk.netindev.scuti.gui.component;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

/**
 *
 * @author netindev
 *
 */
public class Chooser extends JFileChooser {

    private static final long serialVersionUID = 1L;

    @Override
    protected JDialog createDialog(final Component parent) throws HeadlessException {
	final JDialog dialog = super.createDialog(parent);
	final Image image = new ImageIcon(this.getClass().getResource("/images/icon.png")).getImage();
	dialog.setIconImage(image);
	dialog.toFront();
	return dialog;
    }

}
