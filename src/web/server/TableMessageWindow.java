package web.server;
// CatfoOD 2008.3.13

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/** 服务器状态窗口 */
public class TableMessageWindow extends JFrame {
	private ArrayList itemlist;
	
	public TableMessageWindow(String title, Rectangle rect) {
		this.setTitle(title);
		this.setBounds(rect);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setResizable(true);
		
		JTable table = new JTable(new TableData());
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.getViewport().setBackground(Color.black);
		table.setBackground(Color.darkGray);
		table.setForeground(Color.pink);
		itemlist = new ArrayList();

		this.add(scrollpane);
		this.setVisible(true);
	}
	
	/** 添加一个状态项目 */
	public void add(ITableItem i) {
		itemlist.add(i);
	}
	
	private class TableData extends AbstractTableModel implements Runnable {
		private final String[] columnName = {
				Language.tablecol_statename,
				Language.tablecol_state};
		
		private TableData() {
			Thread t = new Thread(this);
			t.setPriority(Thread.MIN_PRIORITY);
			t.setDaemon(true);
			t.start();
		}
		
		public String getColumnName(int column) {
			return columnName[column];
		}
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return itemlist.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			ITableItem o = (ITableItem)itemlist.get(rowIndex);
			if (columnIndex==0) {
				return o.getName();
			} else if (columnIndex==1) {
				return o.getVolume();
			}
			return null;
		}
		
		public void run() {
			while (true) {
				fireTableDataChanged();
				try {
					Thread.sleep(CommonInfo.refurbishSpace);
				} catch (InterruptedException e) {}
			}
		}
	}
}
