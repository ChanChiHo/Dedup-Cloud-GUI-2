import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.CardLayout;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUI {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new CardLayout(0, 0));
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, "name_59979820469839");
		panel.setLayout(null);
		
		JLabel lblClientProgramFor = new JLabel("Client Program for Deduplication Cloud");
		lblClientProgramFor.setBounds(94, 11, 248, 16);
		panel.add(lblClientProgramFor);
		
		JButton btnUpload = new JButton("Upload");
		btnUpload.setBounds(167, 42, 117, 29);
		panel.add(btnUpload);
		
		JButton btnDownload = new JButton("Download");
		btnDownload.setBounds(166, 81, 117, 29);
		panel.add(btnDownload);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.setBounds(165, 123, 117, 29);
		panel.add(btnDelete);
		
		JButton btnListFile = new JButton("List File");
		btnListFile.setBounds(166, 166, 117, 29);
		panel.add(btnListFile);
		
		JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		btnExit.setBounds(324, 242, 117, 29);
		panel.add(btnExit);
	}
}
