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
import com.example.library.rcp2.model.Book;

public class EditBookDialog extends Dialog {

    private Text titleText, authorText, yearText, isbnText;
    private final Book original;
    private Book updated;

    public EditBookDialog(Shell parent, Book book) {
        super(parent);
        this.original = book;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Edit Book");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        new Label(container, SWT.NONE).setText("Title:");
        titleText = new Text(container, SWT.BORDER);
        titleText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        titleText.setText(original.getTitle() == null ? "" : original.getTitle());

        new Label(container, SWT.NONE).setText("Author:");
        authorText = new Text(container, SWT.BORDER);
        authorText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        authorText.setText(original.getAuthor() == null ? "" : original.getAuthor());

        new Label(container, SWT.NONE).setText("Year:");
        yearText = new Text(container, SWT.BORDER);
        yearText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        yearText.setText(original.getYear() == null ? "" : String.valueOf(original.getYear()));

        new Label(container, SWT.NONE).setText("ISBN:");
        isbnText = new Text(container, SWT.BORDER);
        isbnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        isbnText.setText(original.getIsbn() == null ? "" : original.getIsbn());

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

        updated = new Book();
        updated.setTitle(title);
        updated.setAuthor(author);
        updated.setYear(year);
        updated.setIsbn(isbnText.getText().trim());
        updated.setAvailable(original.isAvailable());
        super.okPressed();
    }

    public Book getBook() { return updated; }
}