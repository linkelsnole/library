package com.example.library.rcp2.parts;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.example.library.rcp2.model.Reader;

public class AddReaderDialog extends Dialog {

    private Text fullNameText, genderText, ageText;
    private Reader reader;

    public AddReaderDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Add Reader");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        new Label(container, SWT.NONE).setText("Full Name:");
        fullNameText = new Text(container, SWT.BORDER);
        fullNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(container, SWT.NONE).setText("Gender (M/F):");
        genderText = new Text(container, SWT.BORDER);
        genderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(container, SWT.NONE).setText("Age:");
        ageText = new Text(container, SWT.BORDER);
        ageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return container;
    }

    @Override
    protected void okPressed() {
        String fullName = fullNameText.getText().trim();
        if (fullName.isEmpty()) {
            MessageDialog.openError(getShell(), "Validation", "Full name is required");
            return;
        }

        Integer age = null;
        String ageStr = ageText.getText().trim();
        if (!ageStr.isEmpty()) {
            try {
                age = Integer.parseInt(ageStr);
                if (age < 0) {
                    MessageDialog.openError(getShell(), "Validation", "Age must be non-negative");
                    return;
                }
            } catch (NumberFormatException e) {
                MessageDialog.openError(getShell(), "Validation", "Age must be a number");
                return;
            }
        }

        reader = new Reader();
        reader.setFullName(fullName);
        reader.setGender(genderText.getText().trim());
        reader.setAge(age);
        super.okPressed();
    }

    public Reader getReader() { return reader; }
}