package tk.netindev.scuti.gui.display;

import java.text.NumberFormat;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.text.NumberFormatter;

public class Encrypt extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private JTextField loaderName = new JTextField();
    private JTextField mainClass = new JTextField();
    private JFormattedTextField stringKey;
    private JFormattedTextField classKey;
    private JButton button = new JButton("Save Settings");


    public Encrypt(String loaderName, String mainClass, int i, int j) {
	this.loaderName.setText(loaderName);
	this.mainClass.setText(mainClass);
	this.stringKey.setText(String.valueOf(i));
	this.classKey.setText(String.valueOf(j));
	this.init();
    }

    private void init() {
	setSize(262, 200);
	setTitle("Encrypt");
	setIconImage(new ImageIcon(this.getClass().getResource("/images/icon.png")).getImage());
	setLocationRelativeTo(null);
	JLabel loaderLabel = new JLabel("Loader Name"), mainLabel = new JLabel("Main Class"),
		stringLabel = new JLabel("String Key"), classLabel = new JLabel("Class Key");
	GroupLayout groupLayout = new GroupLayout(getContentPane());
	groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
		.addGroup(groupLayout.createSequentialGroup().addContainerGap()
			.addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(loaderLabel)
				.addComponent(mainLabel)
				.addGroup(groupLayout
					.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(stringKey, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
						.addComponent(stringLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
							GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(classKey, 0, 0, Short.MAX_VALUE).addComponent(classLabel,
							GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
							Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
					.addComponent(button))
				.addComponent(mainClass, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
				.addComponent(loaderName, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
			.addContainerGap()));
	groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
		.createSequentialGroup().addGap(12).addComponent(loaderLabel)
		.addPreferredGap(ComponentPlacement.RELATED)
		.addComponent(loaderName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
			GroupLayout.PREFERRED_SIZE)
		.addPreferredGap(ComponentPlacement.RELATED).addComponent(mainLabel).addGap(5)
		.addComponent(
			mainClass, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		.addPreferredGap(ComponentPlacement.RELATED)
		.addGroup(groupLayout
			.createParallelGroup(Alignment.BASELINE).addComponent(stringLabel).addComponent(classLabel))
		.addPreferredGap(ComponentPlacement.RELATED)
		.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
			.addComponent(stringKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
				GroupLayout.PREFERRED_SIZE)
			.addComponent(button).addComponent(classKey, GroupLayout.PREFERRED_SIZE,
				GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
		.addContainerGap(15, Short.MAX_VALUE)));
	getContentPane().setLayout(groupLayout);
	setResizable(false);
	setVisible(true);
        toFront();
        repaint();
    }
    
    public void setLoaderName(String str) {
	this.loaderName.setText(str);
    }
    
    public String getLoaderName() {
	return this.loaderName.getText();
    }
    
    public void setMainClass(String str) {
	this.mainClass.setText(str);
    }
    public String getMainClass() {
	return this.mainClass.getText();
    }
    
    public void setStringKey(String str) {
	this.stringKey.setText(str);
    }
    
    public String getStringKey() {
	return this.stringKey.getText();
    }
    
    public void setClassKey(String str) {
	this.classKey.setText(str);
    }
    public String getClassKey() {
	return this.classKey.getText();
    }
    
    public JButton getButton() {
	return button;
    }
    
    {
	NumberFormat format = NumberFormat.getInstance();
	NumberFormatter formatter = new NumberFormatter(format);
	formatter.setValueClass(Integer.class);
	formatter.setMinimum(0);
	formatter.setMaximum(Integer.MAX_VALUE);
	formatter.setAllowsInvalid(false);
	formatter.setCommitsOnValidEdit(true);

	stringKey = new JFormattedTextField(format);
	stringKey.setColumns(10);

	classKey = new JFormattedTextField(format);
	classKey.setColumns(10);
    }

}
