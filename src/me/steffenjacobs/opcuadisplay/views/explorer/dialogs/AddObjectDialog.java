package me.steffenjacobs.opcuadisplay.views.explorer.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import me.steffenjacobs.opcuadisplay.shared.util.SharedStorage;
import me.steffenjacobs.opcuadisplay.shared.util.SharedStorage.SharedField;

public class AddObjectDialog extends TitleAreaDialog {

	private Text txtName, txtNameSpace, txtNodeId;

	public AddObjectDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Create Object", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Create Object");
		super.setDialogHelpAvailable(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		createNameSpaceField(container);
		createNameField(container);
		createNodeIdField(container);
		createTypeSelector(container);

		return area;
	}

	private void createTypeSelector(Composite container) {
		final Button btn = new Button(container, SWT.PUSH);
		btn.setText("Select ObjectType");
		btn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				btn.setText("test");
				super.widgetSelected(e);
			}
		});
		btn.setSelection(true);
	}

	private void createNodeIdField(Composite container) {
		// add checkbox that toggles NodeId field
		final Button btn = new Button(container, SWT.CHECK);
		btn.setText("Generate numerical NodeId");
		btn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				txtNodeId.setEnabled(!btn.getSelection());
				if (btn.getSelection()) {
					txtNodeId.setText("" + SharedStorage.getInstance().getValue(SharedField.HighestNodeId));
				}
				super.widgetSelected(e);
			}
		});
		btn.setSelection(true);

		// spacer
		new Label(container, SWT.NONE);

		// add NodeId field
		Label label = new Label(container, SWT.NONE);
		label.setText("NodeId:");
		label.setToolTipText("NodeId of the new object. Has to be unique.");

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;

		txtNodeId = new Text(container, SWT.BORDER);
		txtNodeId.setLayoutData(gridData);
		txtNodeId.setText("" + SharedStorage.getInstance().getValue(SharedField.HighestNodeId));

		txtNodeId.setFocus();
		txtNodeId.setSelection(0, txtNodeId.getText().length());
		txtNodeId.setEnabled(false);
	}

	private void createNameSpaceField(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Namespace Index:");
		label.setToolTipText("Index associated to the namespace for the new object");

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;

		txtNameSpace = new Text(container, SWT.BORDER);
		txtNameSpace.setLayoutData(gridData);
		txtNameSpace
				.setText("" + SharedStorage.getInstance().getRoot().getChildren()[0].getNodeId().getNamespaceIndex());

		txtNameSpace.setFocus();
		txtNameSpace.setSelection(0, txtNameSpace.getText().length());
	}

	private void createNameField(Composite container) {
		Label lblName = new Label(container, SWT.NONE);
		lblName.setText("Name:");
		lblName.setToolTipText("Name of the new object");

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;

		txtName = new Text(container, SWT.BORDER);
		txtName.setLayoutData(gridData);
		txtName.setText("Name");

		txtName.setFocus();
		txtName.setSelection(0, txtName.getText().length());
	}

	@Override
	protected boolean isResizable() {
		return false;
	}

	@Override
	protected void okPressed() {
		// TODO: copy values from fields into string or do other stuff
		super.okPressed();
	}
}