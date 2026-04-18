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
import com.example.library.rcp2.service.ApiClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ReportDialog extends Dialog {

    private Text readerIdText, fromText, toText;
    private final ApiClient client;

    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public ReportDialog(Shell parent, ApiClient client) {
        super(parent);
        this.client = client;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Report");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        new Label(container, SWT.NONE).setText("Reader ID:");
        readerIdText = new Text(container, SWT.BORDER);
        readerIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(container, SWT.NONE).setText("From (dd-MM-yyyy):");
        fromText = new Text(container, SWT.BORDER);
        fromText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(container, SWT.NONE).setText("To (dd-MM-yyyy):");
        toText = new Text(container, SWT.BORDER);
        toText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return container;
    }

    @Override
    protected void okPressed() {
        Long readerId;
        try {
            readerId = Long.parseLong(readerIdText.getText().trim());
        } catch (NumberFormatException e) {
            MessageDialog.openError(getShell(), "Validation", "Reader ID must be a number");
            return;
        }

        String fromStr = fromText.getText().trim();
        String toStr = toText.getText().trim();

        if (fromStr.isEmpty() || toStr.isEmpty()) {
            MessageDialog.openError(getShell(), "Validation", "From and To dates are required");
            return;
        }

        try {
            LocalDate fromDate = LocalDate.parse(fromStr, inputFormatter);
            LocalDate toDate = LocalDate.parse(toStr, inputFormatter);

            long count = client.getReport(readerId, fromDate.toString(), toDate.toString());

            MessageDialog.openInformation(getShell(), "Report", "Books issued: " + count);
        } catch (DateTimeParseException e) {
            MessageDialog.openError(getShell(), "Validation", "Invalid date format. Please use dd-MM-yyyy");
            return;
        } catch (Exception ex) {
            MessageDialog.openError(getShell(), "Error", ex.getMessage());
            return;
        }

        super.okPressed();
    }
}
