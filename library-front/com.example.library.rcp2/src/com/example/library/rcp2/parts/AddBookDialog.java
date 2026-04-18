package com.example.library.rcp2.parts;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.example.library.rcp2.model.Book;

public class AddBookDialog extends Dialog {

    private Text titleText, authorText, yearText, isbnText;
    private Book book;

    public AddBookDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Add Book");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        new Label(container, SWT.NONE).setText("Title:");
        titleText = new Text(container, SWT.BORDER);
        titleText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(container, SWT.NONE).setText("Author:");
        authorText = new Text(container, SWT.BORDER);
        authorText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(container, SWT.NONE).setText("Year:");
        yearText = new Text(container, SWT.BORDER);
        yearText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(container, SWT.NONE).setText("ISBN:");
        isbnText = new Text(container, SWT.BORDER);
        isbnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return container;
    }

    @Override
    protected void okPressed() {
        String title = titleText.getText().trim();
        String author = authorText.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            MessageDialog.openError(getShell(), "Validation", "Title and author are required");
            return;
        }

        Integer year = null;
        String yearStr = yearText.getText().trim();
        if (!yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                MessageDialog.openError(getShell(), "Validation", "Year must be a number");
                return;
            }
        }

        book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setYear(year);
        book.setIsbn(isbnText.getText().trim());
        book.setAvailable(true);
        super.okPressed();
    }

    public Book getBook() { return book; }
}