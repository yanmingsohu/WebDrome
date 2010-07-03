import java.util.Calendar;

import javax.swing.JOptionPane;

import web.server.Language;
import web.server.ServerMachine;
import web.server.SystemIcon;

/*
 * Created on 2008-3-11
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author s1911
 */
public class mainc {

	public static void main(String[] args) {
		ServerMachine sm = new ServerMachine();
		boolean modecanset = true;
		for (int i=0; i<args.length; ++i) {
			if (modecanset && args[i].equalsIgnoreCase("-quiet")) {
				new SystemIcon(sm);
				modecanset = false;
			}
			else if (modecanset && args[i].equalsIgnoreCase("-GUI")) {
				sm.GUIMode();
				modecanset = false;
			} 
			else if (args[i].startsWith("-p:")) {
				try {
					int port = Integer.parseInt( args[i].substring(3) );
					sm.port(port);
				} catch(NumberFormatException e) {
					p(e);
					return;
				}
			}
			else {
				p(Language.parameterError+".");
				JOptionPane.showMessageDialog(	null, 
												Language.parameterError+".", 
												Language.error, 
												JOptionPane.ERROR_MESSAGE, 
												null);
				return;
			}
		}
		sm.start();
	}
	
	public static void p(Object o) {
		System.out.println(o);
	}
}
