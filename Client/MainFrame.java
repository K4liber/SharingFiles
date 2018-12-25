import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class MainFrame extends JFrame {
	
	private JTextField hostNameField;
	private JTextField portField;
	private JLabel hostNameLabel;
	private JLabel portLabel;
	private JLabel listLabel;
	private JButton logButton;
	private JButton downloadButton;
	private JButton uploadButton;
	private FileClient client = null;
	private JScrollPane listPanel = null;
	private JList<String> list = null;
	private DefaultListModel<String> listModel = null;
	
	public MainFrame() {
		super("Sharing files");
		setVisible(true);
		setSize(350, 420);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(null);
		
		hostNameField = new JTextField("localhost");
		portField = new JTextField("9000");
		hostNameField.setBounds(70, 20, 260, 25);
		portField.setBounds(70, 60, 260, 25);
		add(portField);
		add(hostNameField);
		
		hostNameLabel = new JLabel("Host:");
		listLabel = new JLabel("List of files:");
		portLabel = new JLabel("Port:");
		hostNameLabel.setBounds(20, 20, 130, 25);
		portLabel.setBounds(20, 60, 130, 25);
		listLabel.setBounds(20, 130, 130, 25);
		add(listLabel);
		add(hostNameLabel);
		add(portLabel);
		
		logButton = new JButton("Get the list of files");
		logButton.setBounds(20, 100, 310, 20);
		downloadButton = new JButton("Download");
		downloadButton.setBounds(20, 350, 150, 20);
		uploadButton = new JButton("Upload");
		uploadButton.setBounds(180, 350, 150, 20);
		add(logButton);
		add(downloadButton);
		add(uploadButton);

		listPanel = new JScrollPane();
		listPanel.setBounds(20,160,310,180);
		add(listPanel);

		repaint();
		
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.out.println("Goodbye!");
			}
		});
		
		logButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				String hostName = hostNameField.getText();
				int port = Integer.parseInt(portField.getText());
				if (client == null)
					client = new FileClient(hostName, port);

				client.connect();
				String fileList[] = client.getFileList();

				listModel = new DefaultListModel<String>();
				for(int i=0;i<fileList.length;i++)
					listModel.addElement(fileList[i]);

				list = new JList<String>(listModel);
				listPanel.setViewportView(list);

				System.out.println("List received successfully.");
				repaint();
			}
		});
		
		downloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (client == null)
					return;
				client.connect();
				int index = list.getSelectedIndex();
				String path = (String)listModel.getElementAt(index);
				try{
					client.receiveFileFromServer(path);
				} catch (Exception e1) {
					e1.printStackTrace();
				}			
			}
		});

		uploadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (client == null)
					return;
				client.connect();
				try{
					client.uploadFile();
				} catch (Exception e1) {
					e1.printStackTrace();
				}			
			}
		});
	}
	
	public static void main(String[] args) throws Exception {
		MainFrame frame = new MainFrame();
    }
}
