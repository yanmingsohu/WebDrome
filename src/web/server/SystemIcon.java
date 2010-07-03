package web.server;
// CatfoOD 2008.3.25

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *	°²¾²Ä£Ê½
 */
public class SystemIcon implements ActionListener, Runnable {
	private ServerMachine server;
	private PopupMenu pm;
	private TrayIcon ti;
	
	public SystemIcon(ServerMachine sm) {
		server = sm;
		pm = new PopupMenu();		
		pm.add(new MenuItem(Language.exitWebServer));
		pm.add(new MenuItem("-"));
		pm.add(new MenuItem(Language.about));
		pm.addActionListener(this);
				
		try {
			SystemTray st = SystemTray.getSystemTray();
			Dimension dim = st.getTrayIconSize();
			Image img = Toolkit.getDefaultToolkit().createImage( webIcon.getImage() ).
						getScaledInstance(dim.width, dim.height, 0);
			
			ti = new TrayIcon(img, VersionControl.programName+" "+
											VersionControl.version, pm);
			st.add(ti);
		}catch(Exception e){
			JOptionPane.showMessageDialog(
					null, 
					Language.systemIconinitError+","+Language.plaseUseTaskManageOnExit, 
					Language.error, 
					JOptionPane.ERROR_MESSAGE, 
					null);
			//stop();
		}
	}

	public void run() {
		int c = server.getLinkConut();
		window w = new window(c);
		server.stop();
		while (c!=0) {
			c = server.getLinkConut();
			w.setVisible(true);
			w.setCourse(c);
			try {
				Thread.sleep(90);
			} catch (InterruptedException e) {}
		}
		System.exit(0);
	}
	
	private void stop() {
		try {
			SystemTray st = SystemTray.getSystemTray();
			st.remove(ti);
		} catch(Exception e){}
		new Thread(this).start();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().startsWith(Language.exitWebServer)) {
			stop();
		} else {
			new AboutDialog(null);
		}
	}
}

class window extends Frame {
	private final int w = 300;
	private final int h = 130;
	private volatile int c;
	private final String m = Language.systemOnExit+"...";
	
	window(int cont) {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (d.width - w) /2;
		int y = (d.height- h) /2;
		this.setBounds(x, y, w, h);
		this.setAlwaysOnTop(true);
		this.setResizable(false);
		this.setTitle(VersionControl.programName);
		this.setBackground(new Color(212, 208, 200));
		c = cont;
	}
	public void paint(Graphics g) {
		g.drawString(m+" "+c, w/2-36, h/2+5);
	}
	public void setCourse(int c) {
		this.c =c;
		repaint();
	}
}
