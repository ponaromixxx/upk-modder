package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import model.modtree.ModReferenceLeaf;
import model.modtree.ModTree;
import model.upk.UpkFile;
import ui.BrowseActionListener;
import ui.Constants;
import ui.MainFrame;
import util.unrealhex.HexStringLibrary;
import util.unrealhex.ReferenceUpdate;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Dialog implementation listing distinct references of a modfile tree model.
 * <p>
 * Allows to look up references in specifiable UPK files and to apply changes to
 * the document linked to the tree model.
 * 
 * @author XMS, Amineri
 */
@SuppressWarnings("serial")
public class ReferenceUpdateDialog extends JDialog {
	
	/**
	 * The reference to the current modfile tree structure.
	 */
	private ModTree modTree;
	
	/**
	 * The reference to the reference table.
	 */
	private JTable refTbl;

	/** The index of the Source Reference column. */
	private final int SOURCE_REF_COLUMN = 0;
	/** The index of the Virtual Function Flag column. */
	private final int VF_FLAG_COLUMN = 1;
	/** The index of the Reference Name column. */
	private final int REF_NAME_COLUMN = 2;
	/** The index of the Destination Reference column. */
	private final int DEST_REF_COLUMN = 3;
	
	/** Error message string for missing reference name. */
	private final String NAME_NOT_FOUND = "name not found!";
	/** Error message string for missing reference. */
	private final String REF_NOT_FOUND = "ref not found!";
	
	/**
	 * Constructs a reference update dialog from the specified <code>ModTree</code> instance.<br>
	 * Contains a table listing all distinct references stored in the tree.
	 * @param modTree the modfile tree
	 */
	public ReferenceUpdateDialog(ModTree modTree) {
		super(MainFrame.getInstance(), "Update References", true);
		this.modTree = modTree;
		
		this.initComponents();
		
		// re-route default closing behavior to close() method
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				close();
			}
		});
		
		// adjust dialog size
		this.pack();
		this.setResizable(false);
		// center dialog in main frame
		this.setLocationRelativeTo(MainFrame.getInstance());
	}

	/**
	 * Initializes and lays out the dialog's components.
	 */
	private void initComponents() {
		Container contentPane = this.getContentPane();
		
		contentPane.setLayout(new FormLayout("5px, 600px:g, 5px", "5px, f:300px:g, 0px, f:p, 0px, b:p, 5px"));
		
		// create table containing 
		final DefaultTableModel refTblMdl = new DefaultTableModel() {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
					case 1:
						return Boolean.class;
					default:
						return super.getColumnClass(columnIndex);
				}
			}
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		refTblMdl.setColumnIdentifiers(new Object[] { "Source Hex", "VF", "Reference Name", "Dest. Hex" });
		
		// extract distinct references from tree
		Set<ModReferenceLeaf> refNodes = new HashSet<>(ReferenceUpdate.getReferences(this.modTree));
		// populate table model
		for (ModReferenceLeaf refNode : refNodes) {
			refTblMdl.addRow(new Object[] {
				refNode,
				refNode.isVirtualFunctionRef(),
				null,
				null
			});
		}
		
		refTbl = new JTable(refTblMdl);
		refTbl.setAutoCreateRowSorter(true);
		refTbl.getTableHeader().setReorderingAllowed(false);
		refTbl.getTableHeader().setResizingAllowed(false);
		
		TableColumnModel refColMdl = refTbl.getColumnModel();
		
		final TableCellRenderer delegate = refTbl.getDefaultRenderer(Boolean.class);
		DefaultTableCellRenderer booleanRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JComponent comp = (JComponent) delegate.getTableCellRendererComponent(
						table, value, isSelected, hasFocus, row, column);
				comp.setOpaque(true);
				if (hasFocus) {
					JPanel panel = new JPanel(new BorderLayout());
					panel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
					panel.add(comp, BorderLayout.CENTER);
					panel.setBackground(comp.getBackground());
					return panel;
				}
				return comp;
			}
		};
		refTbl.setDefaultRenderer(Boolean.class, booleanRenderer);
		
		DefaultTableCellRenderer monoRenderer = new DefaultTableCellRenderer() {
			/** Reference to monospaced font. */
			private Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				if (value instanceof ModReferenceLeaf) {
					int refValue = ((ModReferenceLeaf) value).getRefValue();
					if (refValue != 0) {
						value = HexStringLibrary.convertIntToHexString(
								refValue).trim();
					} else {
						value = REF_NOT_FOUND;
					}
				}
				Component comp = super.getTableCellRendererComponent(
						table, value, isSelected, hasFocus, row, column);
				if (REF_NOT_FOUND.equals(value)) {
					comp.setForeground((isSelected) ? Color.CYAN : Color.RED);
					comp.setFont(comp.getFont().deriveFont(Font.ITALIC));
				} else {
					comp.setForeground((isSelected) ? Color.WHITE : Color.BLACK);
					comp.setFont(this.monoFont);
				}
				return comp;
			}
		};
		
		DefaultTableCellRenderer errorRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				Component comp = super.getTableCellRendererComponent(
						table, value, isSelected, hasFocus, row, column);
				if (NAME_NOT_FOUND.equals(value)) {
					comp.setForeground((isSelected) ? Color.CYAN : Color.RED);
					comp.setFont(comp.getFont().deriveFont(Font.ITALIC));
				} else {
					comp.setForeground((isSelected) ? Color.WHITE : Color.BLACK);
				}
				return comp;
			}
		};
		
		refColMdl.getColumn(SOURCE_REF_COLUMN).setCellRenderer(monoRenderer);
		refColMdl.getColumn(SOURCE_REF_COLUMN).setMinWidth(88);
		refColMdl.getColumn(SOURCE_REF_COLUMN).setMaxWidth(88);
		refColMdl.getColumn(VF_FLAG_COLUMN).setMaxWidth(24);
		refColMdl.getColumn(REF_NAME_COLUMN).setCellRenderer(errorRenderer);
		refColMdl.getColumn(DEST_REF_COLUMN).setCellRenderer(monoRenderer);
		refColMdl.getColumn(DEST_REF_COLUMN).setMinWidth(94);
		refColMdl.getColumn(DEST_REF_COLUMN).setMaxWidth(94);
		
		JScrollPane refScpn = new JScrollPane(refTbl,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		JPanel refPnl = new JPanel(new BorderLayout(5, 5));
		refPnl.setBorder(BorderFactory.createTitledBorder("Reference Table"));
		refPnl.add(refScpn, BorderLayout.CENTER);

		// create panel containing update controls
		FormLayout controlLyt = new FormLayout("p:g, 5px, p:g", "f:p");
		controlLyt.setColumnGroups(new int[][] { { 1, 3 } });
		JPanel controlPnl = new JPanel(controlLyt);
		
		// create sub-panel containing source-specific controls
		JPanel sourcePnl = new JPanel(new FormLayout("p:g", "p, 5px, p, 2px"));
		sourcePnl.setBorder(BorderFactory.createTitledBorder("Source References"));
		
		JButton lookupSrcBtn = new JButton("Look Up Source Reference Names");
		final JButton hexToNamesBtn = new JButton("Convert Hex References to Names");
		hexToNamesBtn.setEnabled(false);
		// TODO: implement hex-to-names functionality
		
		sourcePnl.add(lookupSrcBtn, CC.xy(1, 1));
		sourcePnl.add(hexToNamesBtn, CC.xy(1, 3));
		
		// create sub-panel containing destination-specific controls
		JPanel destPnl = new JPanel(new FormLayout("p:g", "p, 5px, p"));
		destPnl.setBorder(BorderFactory.createTitledBorder("Destination References"));

		final JButton lookupDestBtn = new JButton("Look Up Destination Reference Values");
		lookupDestBtn.setEnabled(false);
		final JButton srcToDestBtn = new JButton("Convert Source Hex to Destination Hex");
		srcToDestBtn.setEnabled(false);
		// TODO: implement src hex-to-dest hex functionality

		destPnl.add(lookupDestBtn, CC.xy(1, 1));
		destPnl.add(srcToDestBtn, CC.xy(1, 3));

		// install action listeners
		lookupSrcBtn.addActionListener(new BrowseActionListener(this, Constants.UPK_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				boolean res = lookUpSourceNames(file);
				hexToNamesBtn.setEnabled(res);
				lookupDestBtn.setEnabled(res);
			}
		});
		
		lookupDestBtn.addActionListener(new BrowseActionListener(this, Constants.UPK_FILE_FILTER) {
			@Override
			protected void execute(File file) {
				boolean res = lookUpDestinationHex(file);
				srcToDestBtn.setEnabled(res);
			}
		});
		
		controlPnl.add(sourcePnl, CC.xy(1, 1));
		controlPnl.add(destPnl, CC.xy(3, 1));
		
		// create bottom panel containing 'OK' and 'Cancel' buttons
		JPanel buttonPnl = new JPanel(new FormLayout("0px:g, r:p", "p"));
		
		final JButton closeBtn = new JButton("Close");
		closeBtn.setEnabled(true);
		closeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				close();
			}
		});
		
		buttonPnl.add(closeBtn, CC.xy(2, 1));
		
		// add everything to content pane
		contentPane.add(refPnl, CC.xy(2, 2));
		contentPane.add(controlPnl, CC.xy(2, 4));
		contentPane.add(buttonPnl, CC.xy(2, 6));
	}

	/**
	 * Looks up reference names from the specified file and inserts them into
	 * the reference table.
	 * @param file the UPK file to parse
	 * @return <code>true</code> if reference lookup processed without errors,
	 *  <code>false</code> otherwise
	 */
	private boolean lookUpSourceNames(File file) {
		// init return value
		// TODO: maybe replace with error code of some sort
		boolean res = true;
		
		// parse UPK file
		// TODO: implement progress monitoring for upk parsing
		UpkFile upkFile = new UpkFile(file);
		// extract GUID
		byte[] guid = upkFile.getHeader().getGUID();
		// compare file GUID with tree GUID
		if (!this.modTree.getGuid().equals(HexStringLibrary.convertByteArrayToHexString(guid))) {
			// show warning message
			int option = JOptionPane.showConfirmDialog(this, "Mismatching GUIDs detected. Continue anyway?",
					"GUID MisMatch", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.NO_OPTION) {
				// abort
				return false;
			}
		}
		
		// iterate table rows
		for (int row = 0; row < refTbl.getRowCount(); row++) {
			// get reference from table
			ModReferenceLeaf refNode = (ModReferenceLeaf) refTbl.getValueAt(row, SOURCE_REF_COLUMN);
			// extract reference name from UPK file
			String refName = (refNode.isVirtualFunctionRef()) ?
					upkFile.getVFRefName(refNode.getRefValue()) :
					upkFile.getRefName(refNode.getRefValue());
			// check whether lookup failed
			if (refName.isEmpty()) {
				res = false;
				// replace name string with error message
				refName = NAME_NOT_FOUND;
			}
			// update table row
			refTbl.setValueAt(refName, row, REF_NAME_COLUMN);
		}
		
		return res;
	}
	
	/**
	 * Looks up reference values from the specified file and inserts them into
	 * the reference table.
	 * @param file the UPK file to parse
	 * @return <code>true</code> if reference lookup processed without errors,
	 *  <code>false</code> otherwise
	 */
	private boolean lookUpDestinationHex(File file) {
		// init return value
		boolean res = true;
		
		// parse UPK file
		// TODO: implement progress monitoring for upk parsing
		UpkFile upkFile = new UpkFile(file);
		
		// iterate table rows
		for (int row = 0; row < refTbl.getRowCount(); row++) {
			// get reference name from table
			String refName = (String) refTbl.getValueAt(row, REF_NAME_COLUMN);
			// get virtual function flag from table
			boolean vfRef = (Boolean) refTbl.getValueAt(row, VF_FLAG_COLUMN);
			// look up reference value in UPK file
			int refValue = (vfRef) ? upkFile.findVFRefName(refName) : upkFile.findRefName(refName);
			// check whether lookup failed
			// TODO: need proper error value for both lookups (e.g. Integer.MIN_VALUE), 0 is a proper value for virtual function refs
			String refStr = HexStringLibrary.convertIntToHexString(refValue);
			if (refValue == 0) {
				res = false;
				// replace ref string with error message
				refStr = REF_NOT_FOUND;
			}
			// update table row
			refTbl.setValueAt(refStr, row, DEST_REF_COLUMN);
		}
		
		return res;
	}
	
	/**
	 * Disposes the dialog.
	 */
	private void close() {
		// TODO: do some clean-up if necessary
		this.dispose();
	}

}
