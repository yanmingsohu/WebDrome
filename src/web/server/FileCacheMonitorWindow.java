package web.server;
// CatfoOD 2008.3.13

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/** ÎÄ¼þ»º´æ×´Ì¬´°¿Ú */
public class FileCacheMonitorWindow extends JFrame {
	IFileCacheList filelist;
	
	public FileCacheMonitorWindow(String title, Rectangle rect) {
		this.setTitle(title);
		this.setBounds(rect);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setResizable(true);
	}
	
	public void show(IFileCacheList fcl) {
		filelist = fcl;
		TableData td = new TableData();
		JTable table = new JTable(td);
		table.setBackground(Color.darkGray);
		table.setForeground(Color.YELLOW);
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.getViewport().setBackground(Color.black);
		JButton refersh = new JButton(Language.reBrushList);
		refersh.addActionListener(td);
		
		this.setLayout(new BorderLayout());
		this.add(scrollpane);
		this.add(refersh, BorderLayout.SOUTH);
		this.setVisible(true);
	}
	
	private class TableData extends AbstractTableModel implements ActionListener {
		private Object[] itemlist = new ICacheState[0]; 
		private final String[] columnName = 
			{
				Language.table_filename,
				Language.table_cachetime,
				Language.table_usecount,
				Language.table_refcount,
				Language.table_useMem,
				Language.table_state,
			};
		
		public String getColumnName(int column) {
			return columnName[column];
		}
		
		public int getColumnCount() {
			return columnName.length;
		}

		public int getRowCount() {
			return itemlist.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			ICacheState is = (ICacheState)itemlist[rowIndex];
			switch (columnIndex) {
			case 0:
				return is.getFilename();
			case 1:
				return is.cacheTime();
			case 2:
				return is.getUseCount();
			case 3:
				return is.referenceCount();
			case 4:
				return is.useMemory();
			case 5:
				return is.state();
			}
			return null;
		}
		
		public void actionPerformed(ActionEvent e) {
			itemlist = filelist.getFileList();
			if (itemlist==null) {
				itemlist = new ICacheState[0];
			}
			fireTableDataChanged();
		}
	}
}
