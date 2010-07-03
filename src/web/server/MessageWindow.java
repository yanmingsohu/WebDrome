package web.server;
// CatfoOD 2008.3.13

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;


public class MessageWindow extends JFrame {
	private JTextArea text;
	
	/** 缓存CommonInfo.maxMessageLine */
	private final static int removeLine = CommonInfo.maxMessageLine/10;
	private final static int maxLine = CommonInfo.maxMessageLine+removeLine;
	
	public MessageWindow(String title, Rectangle rect) {
		this.setTitle(title);
		this.setBounds(rect);
		this.setResizable(true);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		text = new JTextArea();
		text.setEditable(false);
		text.addComponentListener(new CA());
		text.setBackground(Color.black);
		text.setForeground(getColor());
		
		JScrollPane sp = new JScrollPane(text);
		this.add(sp);
		
		this.setVisible(true);
	}
	
	private final static Color getColor() {
		final Color[] color = {
				Color.green, Color.orange, Color.CYAN, Color.red,
				Color.LIGHT_GRAY, Color.PINK,};
		if (index>=color.length) index = 0;
		return color[index++];
	}
	private static int index = 0;
	
	/**
	 * 添加一条消息,已经在末尾添加换行
	 */
	public void append(Object o) {
		text.append(o.toString()+'\n');
		if (text.getLineCount()>maxLine) {
			try {
				text.replaceRange("", 0, text.getLineEndOffset(removeLine));
			} catch (BadLocationException e) {}
		}
	}
		
	private class CA extends ComponentAdapter {
		public void componentResized(ComponentEvent e) {
			text.setCaretPosition(text.getText().length());
		}
	}
}
