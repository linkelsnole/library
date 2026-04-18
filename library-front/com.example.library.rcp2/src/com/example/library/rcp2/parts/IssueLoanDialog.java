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

public class IssueLoanDialog extends Dialog {

    private Text bookIdText, readerIdText;
    private Long bookId, readerId;

    public IssueLoanDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Issue Book");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        new Label(container, SWT.NONE).setText("Book ID:");
        bookIdText = new Text(container, SWT.BORDER);
        bookIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(container, SWT.NONE).setText("Reader ID:");
        readerIdText = new Text(container, SWT.BORDER);
        readerIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return container;
    }

    @Override
    protected void okPressed() {
        try {
            bookId = Long.parseLong(bookIdText.getText().trim());
            readerId = Long.parseLong(readerIdText.getText().trim());
        } catch (NumberFormatException e) {
            MessageDialog.openError(getShell(), "Validation", "Book ID and Reader ID must be numbers");
            return;
        }
        super.okPressed();
    }

    public Long getBookId() { return bookId; }
    public Long getReaderId() { return readerId; }
}