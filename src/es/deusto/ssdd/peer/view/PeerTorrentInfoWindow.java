package es.deusto.ssdd.peer.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import es.deusto.ssdd.peer.controller.PeerTorrentInfoController;
import es.deusto.ssdd.peer.udp.messages.AnnounceResponse;
import es.deusto.ssdd.peer.udp.messages.PeerInfo;

public class PeerTorrentInfoWindow extends JPanel implements Observer {

	private static final long serialVersionUID = -4986665209181751538L;
	private JTable table;
	private MyButtonModel model;
	private Object[][] rows;
	JScrollPane scrollPane;
	public static final int numberRowsExample = 20;
	private PeerTorrentInfoController controller;

	public PeerTorrentInfoWindow(PeerTorrentInfoController controller) {
		this.controller = controller;
		controller.addObserver(this);
		setSize(600, 300);
		
		createTable();
	}

	public void createTable() {
		
		String[] columnNames = { "Info hash", "Leechers", "Seeders","Peers" };
		model = new MyButtonModel();
		model.setColumnIdentifiers(columnNames);
		model.setDataVector(rows, columnNames);
		table = new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(55);
		TableColumnModel colModel = table.getColumnModel();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.addMouseListener(new JTableButtonMouseListener());
		colModel.getColumn(3).setCellRenderer(new ButtonRenderer());
		scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(500, 250));
		this.add(scrollPane);
	}
	
	private synchronized void updateTable() {

		generateTorrentsData();
		model.setRowCount(0);
		for (int i = 0; i < rows.length; i++) {
			model.addRow(rows[i]);
		}
		model.fireTableDataChanged();
		configureSizesOfTable(table);

	}
	
	public void configureSizesOfTable(JTable table) {
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);
	}
	
	public void generateTorrentsData() {
		Map<String,AnnounceResponse> listInfoHashes = controller.getInfoHashesWithResponse();
		rows = new Object[listInfoHashes.size()][];
		Object[] rowData;
		AnnounceResponse announceResponse;
		int i = 0;
		for ( String infoHash: listInfoHashes.keySet() )
		{
			announceResponse = listInfoHashes.get(infoHash);
			if ( announceResponse != null )
			{
				rowData = new Object[4];
				rowData[0] = infoHash;
				rowData[1] = announceResponse.getLeechers();
				rowData[2] = announceResponse.getSeeders();
				rowData[3]=announceResponse.getPeers();
				rows[i] = rowData;
				i++;
			}
		}
	}
	
	public  List<PeerInfo> getPeers(int row){
		return (List<PeerInfo>) rows[row][3];
	}

	@Override
	public void update(Observable arg0, Object object ) {
		if ( object instanceof AnnounceResponse )
		{
			updateTable();
		}
		
	}
	class JTableButtonMouseListener implements MouseListener {

		private void forwardEventToButton(MouseEvent e) {
			TableColumnModel columnModel = table.getColumnModel();
			int column = columnModel.getColumnIndexAtX(e.getX());
			int row = e.getY() / table.getRowHeight();
			if (column == 3) {
				new PeersListView(getPeers(row));
			}

		}

		public JTableButtonMouseListener() {

		}

		public void mouseClicked(MouseEvent e) {
			forwardEventToButton(e);
		}

		public void mouseEntered(MouseEvent e) {

		}

		public void mouseExited(MouseEvent e) {

		}

		public void mousePressed(MouseEvent e) {

		}

		public void mouseReleased(MouseEvent e) {

		}
	}
	
}

class MyButtonModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	public boolean isCellEditable(int row, int column) {
		return false;
	}

}

class ButtonRenderer implements TableCellRenderer {
	JButton button = new JButton();

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		button.setText("Peers");
		return button;
	}
}
