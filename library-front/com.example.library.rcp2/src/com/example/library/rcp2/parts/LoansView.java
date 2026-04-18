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
import com.example.library.rcp2.model.Loan;
import com.example.library.rcp2.service.ApiClient;

import java.util.List;
import java.util.function.Function;

public class LoansView {

    private TableViewer viewer;
    private Composite partRoot;
    private final ApiClient client = ApiClient.getInstance();

    @PostConstruct
    public void createControls(Composite parent) {
        this.partRoot = parent;
        parent.setLayout(new GridLayout(1, false));

        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayout(new GridLayout(4, false));

        Button issueBtn = new Button(buttons, SWT.PUSH);
        issueBtn.setText("Issue Book");

        Button returnBtn = new Button(buttons, SWT.PUSH);
        returnBtn.setText("Return Book");

        Button reportBtn = new Button(buttons, SWT.PUSH);
        reportBtn.setText("Report");

        Button refreshBtn = new Button(buttons, SWT.PUSH);
        refreshBtn.setText("Refresh");

        viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(ArrayContentProvider.getInstance());

        createColumn("ID", 50, l -> String.valueOf(l.getId()));
        createColumn("Book", 200, l -> l.getBook() == null ? "" : l.getBook().getTitle());
        createColumn("Reader", 150, l -> l.getReader() == null ? "" : l.getReader().getFullName());
        createColumn("Taken At", 100, l -> l.getTakenAt() == null ? "" : l.getTakenAt());
        createColumn("Returned At", 100, l -> l.getReturnedAt() == null ? "Active" : l.getReturnedAt());

        refresh();

        issueBtn.addListener(SWT.Selection, e -> {
            IssueLoanDialog dialog = new IssueLoanDialog(parent.getShell());
            if (dialog.open() == org.eclipse.jface.window.Window.OK) {
                Long bookId = dialog.getBookId();
                Long readerId = dialog.getReaderId();
                runAsync(() -> client.issueBook(bookId, readerId), parent);
            }
        });

        returnBtn.addListener(SWT.Selection, e -> {
            IStructuredSelection sel = viewer.getStructuredSelection();
            if (sel.isEmpty()) return;
            Loan loan = (Loan) sel.getFirstElement();
            if (loan.getReturnedAt() != null) {
                MessageBox box = new MessageBox(parent.getShell(), SWT.ICON_INFORMATION | SWT.OK);
                box.setText("Info");
                box.setMessage("Already returned");
                box.open();
                return;
            }
            runAsync(() -> client.returnBook(loan.getId()), parent);
        });

        reportBtn.addListener(SWT.Selection, e -> {
            ReportDialog dialog = new ReportDialog(parent.getShell(), client);
            dialog.open();
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

    private void createColumn(String title, int width, Function<Loan, String> fn) {
        TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
        col.getColumn().setText(title);
        col.getColumn().setWidth(width);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object el) {
                return fn.apply((Loan) el);
            }
        });
    }

    private void refresh() {
        UiBackgroundExecutor.execute(() -> {
            try {
                List<Loan> loans = client.getLoans();
                Display.getDefault().asyncExec(() -> {
                    if (!viewer.getTable().isDisposed()) {
                        viewer.setInput(loans);
                    }
                });
            } catch (Exception e) {
                showError(partRoot, e);
            }
        });
    }
}