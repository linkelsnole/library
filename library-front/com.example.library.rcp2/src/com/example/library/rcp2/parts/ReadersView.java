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
import com.example.library.rcp2.model.Reader;
import com.example.library.rcp2.service.ApiClient;

import java.util.List;
import java.util.function.Function;

public class ReadersView {

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
        addBtn.setText("Add Reader");

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

        createColumn("ID", 50, r -> String.valueOf(r.getId()));
        createColumn("Full Name", 200, Reader::getFullName);
        createColumn("Gender", 80, r -> r.getGender() == null ? "" : r.getGender());
        createColumn("Age", 60, r -> r.getAge() == null ? "" : String.valueOf(r.getAge()));

        refresh();

        addBtn.addListener(SWT.Selection, e -> {
            AddReaderDialog dialog = new AddReaderDialog(parent.getShell());
            if (dialog.open() == org.eclipse.jface.window.Window.OK) {
                Reader reader = dialog.getReader();
                runAsync(() -> client.createReader(reader), parent);
            }
        });

        editBtn.addListener(SWT.Selection, e -> {
            IStructuredSelection sel = viewer.getStructuredSelection();
            if (sel.isEmpty()) return;
            Reader selected = (Reader) sel.getFirstElement();
            EditReaderDialog dialog = new EditReaderDialog(parent.getShell(), selected);
            if (dialog.open() == org.eclipse.jface.window.Window.OK) {
                Reader updated = dialog.getReader();
                runAsync(() -> client.updateReader(selected.getId(), updated), parent);
            }
        });

        deleteBtn.addListener(SWT.Selection, e -> {
            IStructuredSelection sel = viewer.getStructuredSelection();
            if (sel.isEmpty()) return;
            Reader reader = (Reader) sel.getFirstElement();
            runAsync(() -> client.deleteReader(reader.getId()), parent);
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

    private void createColumn(String title, int width, Function<Reader, String> fn) {
        TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
        col.getColumn().setText(title);
        col.getColumn().setWidth(width);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object el) {
                return fn.apply((Reader) el);
            }
        });
    }

    private void refresh() {
        UiBackgroundExecutor.execute(() -> {
            try {
                List<Reader> readers = client.getReaders();
                Display.getDefault().asyncExec(() -> {
                    if (!viewer.getTable().isDisposed()) {
                        viewer.setInput(readers);
                    }
                });
            } catch (Exception e) {
                showError(partRoot, e);
            }
        });
    }
}