package com.example.library.rcp2.parts;

import jakarta.annotation.PostConstruct;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import com.example.library.rcp2.model.Book;
import com.example.library.rcp2.service.ApiClient;

import java.util.List;
import java.util.function.Function;

public class BooksView {

    private TableViewer viewer;
    private Composite partRoot;
    private final ApiClient client = ApiClient.getInstance();

    @PostConstruct
    public void createControls(Composite parent) {
        this.partRoot = parent;
        parent.setLayout(new GridLayout(1, false));

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayout(new GridLayout(4, false));

        Button addBtn = new Button(buttons, SWT.PUSH);
        addBtn.setText("Add Book");

        Button editBtn = new Button(buttons, SWT.PUSH);
        editBtn.setText("Edit");

        Button deleteBtn = new Button(buttons, SWT.PUSH);
        deleteBtn.setText("Delete");

        Button refreshBtn = new Button(buttons, SWT.PUSH);
        refreshBtn.setText("Refresh");

        viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(ArrayContentProvider.getInstance());

        createColumn("ID", 50, b -> String.valueOf(b.getId()));
        createColumn("Title", 200, Book::getTitle);
        createColumn("Author", 150, Book::getAuthor);
        createColumn("Year", 70, b -> b.getYear() == null ? "" : String.valueOf(b.getYear()));
        createColumn("ISBN", 120, b -> b.getIsbn() == null ? "" : b.getIsbn());
        createColumn("Available", 80, b -> b.isAvailable() ? "Yes" : "No");

        refresh();

        addBtn.addListener(SWT.Selection, e -> {
            AddBookDialog dialog = new AddBookDialog(parent.getShell());
            if (dialog.open() == org.eclipse.jface.window.Window.OK) {
                Book book = dialog.getBook();
                runAsync(() -> client.createBook(book), parent);
            }
        });

        editBtn.addListener(SWT.Selection, e -> {
            IStructuredSelection sel = viewer.getStructuredSelection();
            if (sel.isEmpty()) return;
            Book selected = (Book) sel.getFirstElement();
            EditBookDialog dialog = new EditBookDialog(parent.getShell(), selected);
            if (dialog.open() == org.eclipse.jface.window.Window.OK) {
                Book updated = dialog.getBook();
                runAsync(() -> client.updateBook(selected.getId(), updated), parent);
            }
        });

        deleteBtn.addListener(SWT.Selection, e -> {
            IStructuredSelection sel = viewer.getStructuredSelection();
            if (sel.isEmpty()) return;
            Book book = (Book) sel.getFirstElement();
            runAsync(() -> client.deleteBook(book.getId()), parent);
        });

        refreshBtn.addListener(SWT.Selection, e -> refresh());
    }

    @Focus
    public void onFocus() {
        refresh();
    }

    private void runAsync(Runnable action, Composite parent) {
        UiBackgroundExecutor.execute(() -> {
            try {
                action.run();
                refresh();
            } catch (Exception ex) {
                showError(parent, ex);
            }
        });
    }

    private void showError(Composite parent, Exception ex) {
        Display.getDefault().asyncExec(() -> {
            if (parent.isDisposed()) return;
            MessageBox box = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
            box.setText("Error");
            box.setMessage(ex.getMessage());
            box.open();
        });
    }

    private void createColumn(String title, int width, Function<Book, String> fn) {
        TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
        col.getColumn().setText(title);
        col.getColumn().setWidth(width);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object el) {
                return fn.apply((Book) el);
            }
        });
    }

    private void refresh() {
        UiBackgroundExecutor.execute(() -> {
            try {
                List<Book> books = client.getBooks();
                Display.getDefault().asyncExec(() -> {
                    if (!viewer.getTable().isDisposed()) {
                        viewer.setInput(books);
                    }
                });
            } catch (Exception e) {
                showError(partRoot, e);
            }
        });
    }
}