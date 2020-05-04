package tk.netindev.scuti.gui.display;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.sun.javafx.application.PlatformImpl;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import tk.netindev.scuti.core.Scuti;
import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.configuration.io.Parser;
import tk.netindev.scuti.core.configuration.io.Writer;
import tk.netindev.scuti.core.configuration.option.obfuscation.ClassEncrypt;
import tk.netindev.scuti.core.configuration.option.obfuscation.MiscellaneousObfuscation;
import tk.netindev.scuti.core.configuration.option.obfuscation.NumberObfuscation;
import tk.netindev.scuti.core.configuration.option.obfuscation.RenameMembers;
import tk.netindev.scuti.core.configuration.option.obfuscation.StringEncryption;
import tk.netindev.scuti.core.configuration.option.shrinking.UnusedMembers;
import tk.netindev.scuti.core.dictionary.Types;
import tk.netindev.scuti.core.transform.Transformer;
import tk.netindev.scuti.core.transform.Transformers.Obfuscation;
import tk.netindev.scuti.core.transform.Transformers.Optimization;
import tk.netindev.scuti.core.transform.Transformers.Shrinking;
import tk.netindev.scuti.gui.component.Chooser;

/**
 *
 * @author netindev1
 *
 */
public class Controller implements Initializable {

    @FXML
    private Label exitLabel, minimizeLabel, descLabel;

    @FXML
    private AnchorPane protectPane, startPane, inputPane, configurationPane;

    @FXML
    private JFXTextField inputField, outputField, classField, methodField, fieldField, customPackagesField,
	    customClassesField, customFieldsField, customMethodsField;

    @FXML
    private JFXComboBox<String> dictionary;

    @FXML
    private JFXToggleButton renameClasses, renameFields, renameMethods, dynamicCalls, controlFlow, encryptStrings,
	    encryptButton, hideButton, removePackages, nopField, deadCodeField, innerClassField, unusedField;

    @FXML
    private JFXButton encryptSettings, hideSettings, startButton;

    @FXML
    private JFXTextArea outputLog;

    @FXML
    private JFXSpinner spinner;

    private File input, output;
    private List<File> dependencies;
    private Set<Class<? extends Transformer>> transformers;

    private ClassEncrypt encrypt = new ClassEncrypt();
    private UnusedMembers unused = new UnusedMembers();
    private RenameMembers rename = new RenameMembers();

    private Viewer viewer;

    private PrintStream printStream;

    @FXML
    public void onClickExitLabel() {
	System.exit(0);
    }

    @FXML
    private void onClickMinimizeLabel() {
	Stage stage = (Stage) minimizeLabel.getScene().getWindow();
	stage.setIconified(true);
    }

    @FXML
    public void onClickFileImage() {
	if (this.viewer == Viewer.INPUT) {
	    return;
	}
	this.updatePane(this.inputPane);
	this.viewer = Viewer.INPUT;
	this.descLabel.setText("Input, output and dependencies");
    }

    @FXML
    public void onClickConfigurationImage() {
	if (this.viewer == Viewer.CONFIGURATION) {
	    return;
	}
	this.updatePane(this.configurationPane);
	this.viewer = Viewer.CONFIGURATION;
	this.descLabel.setText("Load and save your configurations");
    }

    @FXML
    public void onClickShieldImage() {
	if (this.viewer == Viewer.PROTECT) {
	    return;
	}
	this.updatePane(this.protectPane);
	this.viewer = Viewer.PROTECT;
	this.descLabel.setText("Obfuscation, shrinking and optimization");
    }

    @FXML
    public void onClickStartImage() {
	if (this.viewer == Viewer.START) {
	    return;
	}
	this.updatePane(this.startPane);
	this.viewer = Viewer.START;
	this.descLabel.setText("Output console");
    }

    Thread thread;

    @FXML
    public void onClickStart() {
	if ((this.renameClasses.isSelected() || this.renameFields.isSelected() || this.renameMethods.isSelected())
		&& !this.transformers.contains(Obfuscation.RENAME_MEMBERS_TRANSFORMER)) {
	    this.transformers.add(Obfuscation.RENAME_MEMBERS_TRANSFORMER);
	}
	for (final Types r : Types.values()) {
	    if (r.name().toLowerCase()
		    .equals(this.dictionary.getSelectionModel().getSelectedItem().toString().toLowerCase())) {
		this.rename.setRandomize(r);
	    }
	}
	if (this.input == null) {
	    System.out.println("Your input file can't be null.");
	    return;
	} else if (this.output == null) {
	    System.out.println("Your output file can't be null.");
	    return;
	} else if (this.transformers.isEmpty()) {
	    System.out.println("Please select at least one transformer to continue.");
	    return;
	}
	this.outputLog.clear();
	System.out.println("Starting scuti-core module");
	this.spinner.setVisible(true);
	this.startButton.setDisable(true);
	this.thread = new Thread(() -> {
	    try {
		new Scuti(Controller.this.buildConfiguration());
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	});
	this.thread.start();
    }

    @FXML
    public void onClickChooseInput() {
	FileChooser fileChooser = new FileChooser();
	fileChooser.setTitle("Input File");
	fileChooser.getExtensionFilters().add(new ExtensionFilter("Jar Files", "*.jar"));
	File selectedFile = fileChooser.showOpenDialog(minimizeLabel.getScene().getWindow());
	if (selectedFile == null) return;
	if (selectedFile.exists() && selectedFile.canRead()) {
	    if (this.output != null && selectedFile.getAbsolutePath().equals(this.output.getAbsolutePath())) {
		System.out.println("Your input and output file can't be the same.");
		return;
	    }
	    if (selectedFile.getName().endsWith(".jar")) {
		this.input = selectedFile;
		this.inputField.setText(selectedFile.getPath());
	    }
	}
    }

    @FXML
    public void onClickChooseOutput() {
	FileChooser fileChooser = new FileChooser();
	fileChooser.setTitle("Output File");
	fileChooser.getExtensionFilters().add(new ExtensionFilter("Jar Files", "*.jar"));
	File selectedFile = fileChooser.showOpenDialog(minimizeLabel.getScene().getWindow());
	if (selectedFile == null) return;
	if (selectedFile.exists() && selectedFile.canRead()) {
	    if (this.input != null && selectedFile.getAbsolutePath().equals(this.input.getAbsolutePath())) {
		System.out.println("Your input and output file can't be the same.");
		return;
	    }
	    if (selectedFile.getName().endsWith(".jar")) {
		this.output = selectedFile;
		this.outputField.setText(selectedFile.getPath());
	    }
	}
    }

    @FXML
    public void onClickAddDependency() {
	FileChooser fileChooser = new FileChooser();
	fileChooser.setTitle("Dependency File");
	fileChooser.getExtensionFilters().add(new ExtensionFilter("Jar Files", "*.jar"));
	File selectedFile = fileChooser.showOpenDialog(minimizeLabel.getScene().getWindow());
	if (selectedFile == null) return;
	if (selectedFile.exists() && selectedFile.canRead()) {
	    if (selectedFile.getName().endsWith(".jar")) {
		this.dependencies.add(selectedFile);
	    }
	}
    }

    @FXML
    public void onClickRemoveDependency() {

	final JFrame frame = new JFrame("Remove");
	frame.toFront();
	frame.repaint();
	frame.setContentPane(new JPanel() {
	    private static final long serialVersionUID = 1L;

	    private JList<File> fileList;
	    private DefaultListModel<File> listModel;

	    /* init */ {
		this.setLayout(new BorderLayout());
		this.listModel = new DefaultListModel<>();
		this.fileList = new JList<>(this.listModel);

		Controller.this.dependencies.forEach(file -> this.listModel.addElement(file));

		final JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(e -> {
		    if (this.fileList.getSelectedValue() == null) {
			return;
		    }
		    for (final File file : this.fileList.getSelectedValuesList()) {
			this.listModel.removeElement(file);
			Controller.this.dependencies.remove(file);
		    }
		});
		this.add(new JScrollPane(this.fileList), BorderLayout.NORTH);
		this.add(removeButton, BorderLayout.SOUTH);
	    }
	});
	frame.setIconImage(new ImageIcon(this.getClass().getResource("/images/icon.png")).getImage());
	frame.setSize(350, 180);
	frame.setResizable(false);
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
    }

    @FXML
    private void onClickLoadConfiguration() {
	final JFileChooser chooser = new Chooser();
	final FileNameExtensionFilter filter = new FileNameExtensionFilter(".json", "json");
	chooser.setMultiSelectionEnabled(false);
	chooser.setFileFilter(filter);
	final int value = chooser.showOpenDialog(new JLabel());
	if (value == JFileChooser.APPROVE_OPTION) {
	    final File file = chooser.getSelectedFile();
	    if (file.getName().endsWith(".json")) {
		System.out.println("Loading \"" + file.getName() + "\" configuration file.");
		try {
		    final Configuration configuration = new Parser(file);
		    this.input = configuration.getInput();
		    this.inputField.setText(this.input.getPath());
		    this.output = configuration.getOutput();
		    this.outputField.setText(this.output.getPath());
		    this.dependencies = configuration.getDependencies();
		    this.transformers = configuration.getTransformers();
		    this.rename = configuration.getRenameMembers();
		    this.customPackagesField.setText(this.rename.getPackagesDictionary().getPath());
		    this.customClassesField.setText(this.rename.getClassesDictionary().getPath());
		    this.customFieldsField.setText(this.rename.getFieldsDictionary().getPath());
		    this.customMethodsField.setText(this.rename.getMethodsDictionary().getPath());
		    this.renameClasses.setSelected(this.rename.isRenameClasses());
		    this.renameFields.setSelected(this.rename.isRenameFields());
		    this.renameMethods.setSelected(this.rename.isRenameClasses());
		    this.dynamicCalls.setSelected(
			    configuration.getTransformers().contains(Obfuscation.INVOKE_DYNAMIC_TRANSFORMER));
		    this.controlFlow
			    .setSelected(configuration.getTransformers().contains(Obfuscation.CONTROL_FLOW_TRANSFORMER));
		    this.encryptStrings
			    .setSelected(configuration.getTransformers().contains(Obfuscation.STRING_ENCRYPTION_TRANSFORMER));
		    this.encryptButton
			    .setSelected(configuration.getTransformers().contains(Obfuscation.CLASS_ENCRYPT_TRANSFORMER));
		    this.hideButton.setSelected(configuration.getTransformers().contains(Obfuscation.HIDE_CODE_TRANSFORMER));
		    this.nopField.setSelected(
			    configuration.getTransformers().contains(Optimization.NO_OPERATION_TRANSFORMER));
		    this.deadCodeField
			    .setSelected(configuration.getTransformers().contains(Optimization.DEAD_CODE_TRANSFORMER));
		    this.innerClassField
			    .setSelected(configuration.getTransformers().contains(Shrinking.INNER_CLASS_TRANSFORMER));
		    this.unusedField
			    .setSelected(configuration.getTransformers().contains(Shrinking.UNUSED_MEMBER_TRANSFORMER));
		    this.unused = configuration.getUnusedMembers();
		    this.encrypt = configuration.getClassEncrypt();
		    System.out.println("Configuration file loaded.");
		} catch (final Exception e) {
		    System.out.println("Couldn't parse configuration, exception: " + e.getMessage());
		}
	    }
	}
    }

    @FXML
    private void onClickSaveConfiguration() {
	final JFileChooser chooser = new Chooser();
	chooser.setMultiSelectionEnabled(false);
	final int value = chooser.showOpenDialog(new JLabel());
	if (value == JFileChooser.APPROVE_OPTION) {
	    try {
		final File file = chooser.getSelectedFile();
		if (file.getName().endsWith(".json")) {
		    new Writer(file, this.buildConfiguration());
		}
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	}
    }

    @FXML
    private void onClickRenameClasses() {
	this.rename.setRenameClasses(this.renameClasses.isSelected());
    }

    @FXML
    private void onClickRenameFields() {
	this.rename.setRenameFields(this.renameFields.isSelected());
    }

    @FXML
    private void onClickRenameMethods() {
	this.rename.setRenameMethods(this.renameMethods.isSelected());
    }

    @FXML
    private void onClickDynamicCalls() {
	if (this.dynamicCalls.isSelected()) {
	    this.transformers.add(Obfuscation.INVOKE_DYNAMIC_TRANSFORMER);
	} else {
	    this.transformers.remove(Obfuscation.INVOKE_DYNAMIC_TRANSFORMER);
	}
    }

    @FXML
    private void onClickControlFlow() {
	if (this.controlFlow.isSelected()) {
	    this.transformers.add(Obfuscation.CONTROL_FLOW_TRANSFORMER);
	} else {
	    this.transformers.remove(Obfuscation.CONTROL_FLOW_TRANSFORMER);
	}
    }

    @FXML
    private void onClickEncryptStrings() {
	if (this.encryptStrings.isSelected()) {
	    this.transformers.add(Obfuscation.STRING_ENCRYPTION_TRANSFORMER);
	} else {
	    this.transformers.remove(Obfuscation.STRING_ENCRYPTION_TRANSFORMER);
	}
    }

    @FXML
    private void onClickEncryptButton() {
	if (this.encryptButton.isSelected()) {
	    this.transformers.add(Obfuscation.CLASS_ENCRYPT_TRANSFORMER);
	} else {
	    this.transformers.remove(Obfuscation.CLASS_ENCRYPT_TRANSFORMER);
	}
    }

    @FXML
    private void onClickHideButton() {
	if (this.hideButton.isSelected()) {
	    this.transformers.add(Obfuscation.HIDE_CODE_TRANSFORMER);
	} else {
	    this.transformers.remove(Obfuscation.HIDE_CODE_TRANSFORMER);
	}
    }

    @FXML
    private void onClickEncryptSettings() {
	try {
	    tk.netindev.scuti.gui.display.Encrypt encrypt = new tk.netindev.scuti.gui.display.Encrypt(
		    this.encrypt.getLoaderName() == null ? "" : this.encrypt.getLoaderName(),
		    this.encrypt.getMainClass() == null ? "" : this.encrypt.getMainClass(),
		    (this.encrypt.getStringKey() == 0 ? 25 : this.encrypt.getStringKey()),
		    this.encrypt.getClassKey() == 0 ? 40 : this.encrypt.getClassKey());
	    encrypt.getButton().addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    Controller.this.encrypt.setMainClass(encrypt.getMainClass());
		    Controller.this.encrypt.setLoaderName(encrypt.getLoaderName());
		    Controller.this.encrypt.setStringKey(Integer.parseInt(encrypt.getStringKey().replaceAll(",", "")));
		    Controller.this.encrypt.setClassKey(Integer.parseInt(encrypt.getClassKey().replaceAll(",", "")));
		}
	    });
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    @FXML
    private void onClickHideSettings() {

    }

    @FXML
    private void onClickNopButton() {
	if (this.hideButton.isSelected()) {
	    this.transformers.add(Optimization.NO_OPERATION_TRANSFORMER);
	} else {
	    this.transformers.remove(Optimization.NO_OPERATION_TRANSFORMER);
	}
    }

    @FXML
    private void onClickDeadCodeButton() {
	if (this.deadCodeField.isSelected()) {
	    this.transformers.add(Optimization.DEAD_CODE_TRANSFORMER);
	} else {
	    this.transformers.remove(Optimization.DEAD_CODE_TRANSFORMER);
	}
    }

    @FXML
    private void onClickInnerClassButton() {
	if (this.innerClassField.isSelected()) {
	    this.transformers.add(Shrinking.INNER_CLASS_TRANSFORMER);
	} else {
	    this.transformers.remove(Shrinking.INNER_CLASS_TRANSFORMER);
	}
    }

    @FXML
    private void onClickUnusedButton() {
	if (this.unusedField.isSelected()) {
	    this.transformers.add(Shrinking.UNUSED_MEMBER_TRANSFORMER);
	} else {
	    this.transformers.remove(Shrinking.UNUSED_MEMBER_TRANSFORMER);
	}
    }

    @FXML
    private void onClickViewClasses() {
	final JFrame frame = new JFrame("Classes");
	frame.toFront();
	frame.repaint();
	frame.setContentPane(new JPanel() {
	    private static final long serialVersionUID = 1L;

	    private JList<String> fileList;
	    private DefaultListModel<String> listModel;

	    /* init */ {
		this.setLayout(new BorderLayout());
		this.listModel = new DefaultListModel<>();
		this.fileList = new JList<>(this.listModel);

		Controller.this.rename.getExcludeClasses().forEach(file -> this.listModel.addElement(file));

		final JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(e -> {
		    if (this.fileList.getSelectedValue() == null) {
			return;
		    }
		    for (final String file : this.fileList.getSelectedValuesList()) {
			this.listModel.removeElement(file);
			Controller.this.rename.getExcludeClasses().remove(file);
		    }
		});
		this.add(new JScrollPane(this.fileList), BorderLayout.NORTH);
		this.add(removeButton, BorderLayout.SOUTH);
	    }
	});
	frame.setIconImage(new ImageIcon(this.getClass().getResource("/images/icon.png")).getImage());
	frame.setSize(350, 180);
	frame.setResizable(false);
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
	frame.setFocusable(true);
    }

    @FXML
    private void onClickViewMethods() {
	final JFrame frame = new JFrame("Methods");
	frame.toFront();
	frame.repaint();
	frame.setContentPane(new JPanel() {
	    private static final long serialVersionUID = 1L;

	    private JList<String> fileList;
	    private DefaultListModel<String> listModel;

	    /* init */ {
		this.setLayout(new BorderLayout());
		this.listModel = new DefaultListModel<>();
		this.fileList = new JList<>(this.listModel);

		Controller.this.rename.getExcludeMethods().forEach(file -> this.listModel.addElement(file));

		final JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(e -> {
		    if (this.fileList.getSelectedValue() == null) {
			return;
		    }
		    for (final String string : this.fileList.getSelectedValuesList()) {
			this.listModel.removeElement(string);
			Controller.this.rename.getExcludeMethods().remove(string);
		    }
		});
		this.add(new JScrollPane(this.fileList), BorderLayout.NORTH);
		this.add(removeButton, BorderLayout.SOUTH);
	    }
	});
	frame.setIconImage(new ImageIcon(this.getClass().getResource("/images/icon.png")).getImage());
	frame.setSize(350, 180);
	frame.setResizable(false);
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
	frame.setFocusable(true);
    }

    @FXML
    private void onClickViewFields() {
	final JFrame frame = new JFrame("Fields");
	frame.toFront();
	frame.repaint();
	frame.setContentPane(new JPanel() {
	    private static final long serialVersionUID = 1L;

	    private JList<String> fileList;
	    private DefaultListModel<String> listModel;

	    /* init */ {
		this.setLayout(new BorderLayout());
		this.listModel = new DefaultListModel<>();
		this.fileList = new JList<>(this.listModel);

		Controller.this.rename.getExcludeFields().forEach(file -> this.listModel.addElement(file));

		final JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(e -> {
		    if (this.fileList.getSelectedValue() == null) {
			return;
		    }
		    for (final String file : this.fileList.getSelectedValuesList()) {
			this.listModel.removeElement(file);
			Controller.this.rename.getExcludeFields().remove(file);
		    }
		});
		this.add(new JScrollPane(this.fileList), BorderLayout.NORTH);
		this.add(removeButton, BorderLayout.SOUTH);
	    }
	});
	frame.setIconImage(new ImageIcon(this.getClass().getResource("/images/icon.png")).getImage());
	frame.setSize(350, 180);
	frame.setResizable(false);
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
	frame.setFocusable(true);
    }

    @FXML
    private void onClickSaveClasses() {
	if (!this.classField.getText().isEmpty()) {
	    this.rename.getExcludeClasses().add(this.classField.getText());
	}
    }

    @FXML
    private void onClickSaveFields() {
	if (!this.methodField.getText().isEmpty()) {
	    this.rename.getExcludeFields().add(this.methodField.getText());
	}
    }

    @FXML
    private void onClickSaveMethods() {
	if (!this.fieldField.getText().isEmpty()) {
	    this.rename.getExcludeMethods().add(this.fieldField.getText());
	}
    }

    @FXML
    private void onClickRemovePackages() {
	this.rename.setRemovePackages(this.removePackages.isSelected());
    }

    @FXML
    private void onClickCustomPackages() {
	final JFileChooser chooser = new Chooser();
	final FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt", "txt");
	chooser.setMultiSelectionEnabled(true);
	chooser.setFileFilter(filter);
	final int value = chooser.showOpenDialog(new JLabel());
	if (value == JFileChooser.APPROVE_OPTION) {
	    final File file = chooser.getSelectedFile();
	    if (file.getName().endsWith(".txt")) {
		this.rename.setPackagesDictionary(file);

		this.customPackagesField.setText(file.getPath());
	    }
	}
    }

    @FXML
    private void onClickCustomClasses() {
	final JFileChooser chooser = new Chooser();
	final FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt", "txt");
	chooser.setMultiSelectionEnabled(true);
	chooser.setFileFilter(filter);
	final int value = chooser.showOpenDialog(new JLabel());
	if (value == JFileChooser.APPROVE_OPTION) {
	    final File file = chooser.getSelectedFile();
	    if (file.getName().endsWith(".txt")) {
		this.rename.setClassesDictionary(file);

		this.customClassesField.setText(file.getPath());
	    }
	}
    }

    @FXML
    private void onClickCustomFields() {
	final JFileChooser chooser = new Chooser();
	final FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt", "txt");
	chooser.setMultiSelectionEnabled(true);
	chooser.setFileFilter(filter);
	final int value = chooser.showOpenDialog(new JLabel());
	if (value == JFileChooser.APPROVE_OPTION) {
	    final File file = chooser.getSelectedFile();
	    if (file.getName().endsWith(".txt")) {
		this.rename.setFieldsDictionary(file);
		this.customFieldsField.setText(file.getPath());
	    }
	}
    }

    @FXML
    private void onClickCustomMethods() {
	final JFileChooser chooser = new Chooser();
	final FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt", "txt");
	chooser.setMultiSelectionEnabled(true);
	chooser.setFileFilter(filter);
	final int value = chooser.showOpenDialog(new JLabel());
	if (value == JFileChooser.APPROVE_OPTION) {
	    final File file = chooser.getSelectedFile();
	    if (file.getName().endsWith(".txt")) {
		this.rename.setMethodsDictionary(file);
		this.customMethodsField.setText(file.getPath());
	    }
	}
    }

    private void updatePane(final AnchorPane anchorPane) {
	switch (this.viewer) {
	case CONFIGURATION:
	    this.configurationPane.setVisible(false);
	    this.configurationPane.setDisable(true);
	    break;
	case INPUT:
	    this.inputPane.setVisible(false);
	    this.inputPane.setDisable(true);
	    break;
	case PROTECT:
	    this.protectPane.setVisible(false);
	    this.protectPane.setDisable(true);
	    break;
	case START:
	    this.startPane.setVisible(false);
	    this.startPane.setDisable(true);
	    break;
	}
	anchorPane.setVisible(true);
	anchorPane.setDisable(false);
    }

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
	this.viewer = Viewer.INPUT;

	this.dictionary.getItems().add("Alphabet");
	this.dictionary.getItems().add("Custom");
	this.dictionary.getItems().add("Number");
	this.dictionary.getItems().add("Randomized");
	this.dictionary.getSelectionModel().select(0);

	this.spinner.setVisible(false);

	this.outputLog.setText(
		"Scuti, a java obfuscator written by netindev.\nPlease visit: \"scuti.netindev.tk\" for more information.\nThis GUI only works with scuti-core installed.\n\nAvailable threads: "
			+ Runtime.getRuntime().availableProcessors() + "\n\n");
    }

    private Configuration buildConfiguration() {
	return new Configuration() {

	    @Override
	    public UnusedMembers getUnusedMembers() {
		return Controller.this.unused;
	    }

	    @Override
	    public Set<Class<? extends Transformer>> getTransformers() {
		return Controller.this.transformers;
	    }

	    @Override
	    public RenameMembers getRenameMembers() {
		return Controller.this.rename;
	    }

	    @Override
	    public File getOutput() {
		return Controller.this.output;
	    }

	    @Override
	    public File getInput() {
		return Controller.this.input;
	    }

	    @Override
	    public ClassEncrypt getClassEncrypt() {
		return Controller.this.encrypt;
	    }

	    @Override
	    public List<File> getDependencies() {
		return Controller.this.dependencies;
	    }

	    @Override
	    public MiscellaneousObfuscation getMiscellaneousObfuscation() {
		return null;
	    }

	    @Override
	    public boolean corruptCRC32() {
		return false;
	    }

	    @Override
	    public NumberObfuscation getNumberObfuscation() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public StringEncryption getStringEncryption() {
		// TODO Auto-generated method stub
		return null;
	    }

		@Override
		public boolean corruptNames() {
			// TODO Auto-generated method stub
			return false;
		}
	};
    }

    {
	this.printStream = new PrintStream(new OutputStream() {

	    @Override
	    public void write(final int b) throws java.io.IOException {
		PlatformImpl.startup(() -> Controller.this.outputLog
			.setText(Controller.this.outputLog.getText() + new String(new char[] { (char) b })));
	    }

	    @Override
	    public void write(final byte b[], final int off, final int len) throws java.io.IOException {
		final String mess = new String(b, off, len);
		if (Controller.this.outputLog != null) {
		    PlatformImpl.startup(() -> {
			if (mess.contains("com.sun.javafx.css.parser.CSSParser")) {
			    return;
			}
			Controller.this.outputLog.setText(Controller.this.outputLog.getText() + mess);
			if (mess.contains("[INFO] Done in") || mess.contains("[ERROR]")
				|| mess.contains("java.lang.")) {
			    Controller.this.thread.interrupt();
			    Controller.this.startButton.setDisable(false);
			    Controller.this.spinner.setVisible(false);
			}
		    });
		}
	    }

	});
	System.setOut(this.printStream);
	System.setErr(this.printStream);
	this.dependencies = new ArrayList<>();
	this.transformers = new HashSet<>();
    }

}
