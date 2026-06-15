package com.ordering.system.ui;

import javax.swing.table.DefaultTableModel;

// A table model whose cells cannot be edited, used by every table in the app
// so the tables are display-only. It extends DefaultTableModel and overrides
// one method, which is a simple example of inheritance.
public class ReadOnlyTableModel extends DefaultTableModel {

    public ReadOnlyTableModel(String[] columns) {
        super(columns, 0);   // start with these columns and no rows yet
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
