package es.deusto.ssdd.peer.view;


import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import es.deusto.ssdd.peer.udp.messages.PeerInfo;

public class PeersListView extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4164593704177471081L;
	private JTable table;
	private MyBooleanModel model;
	private Object[][] rows;
	private List<PeerInfo> peers;

	public PeersListView(List<PeerInfo> peers) {
		this.peers=peers;
		createTable();
	}

	public void createTable() {
		populateRows();
		setLocationRelativeTo(null);
		setTitle("Peers List");
		String[] columnNames = { "Ip", "Port" };
		model = new MyBooleanModel();
		model.setColumnIdentifiers(columnNames);
		model.setDataVector(rows, columnNames);
		table = new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setPreferredWidth(65);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(425, 250));
		this.add(scrollPane);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				dispose();
			}
		});
		this.setVisible(true);
		setSize(400, 300);
	}
	public void populateRows(){
		rows = new Object[peers.size()][];
		Object[] rowData;
		int i=0;
		for (PeerInfo peer:peers){
			rowData = new Object[2];
			rowData[0]= PeerInfo.toStringIpAddress(peer.getIpAddress());
			rowData[1]=peer.getPort();
			rows[i]=rowData;
			i++;
		}
	}
}
class MyBooleanModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public Class<?> getColumnClass(int column) {
		switch (column) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		case 3:
			return Boolean.class;
		default:
			return String.class;
		}
	}

}
