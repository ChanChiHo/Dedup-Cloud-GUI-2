import java.awt.CardLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.JProgressBar;
import java.awt.Font;
import javax.swing.SwingConstants;

public class GUI {

	private JFrame frame;
	private JTextField txtIpAddress;
	private Client client;
	private String address;
	private JFileChooser chooserFilePath;
	private JTextField txtMinChunk;
	private JTextField txtD;
	private JTextField txtAverageChunk;
	private JTextField txtMaxChunk;
	private JTable table;
	private JTextField filePathTextField;
	
	private Thread uploadThread;
	private volatile boolean kill = false;
	

	// Credit from :
	// https://www.codejava.net/java-se/swing/redirect-standard-output-streams-to-jtextarea
	public class CustomOutputStream extends OutputStream {
		private JTextArea textArea;

		public CustomOutputStream(JTextArea textArea) {
			this.textArea = textArea;
		}

		@Override
		public void write(int b) throws IOException {
			// redirects data to the text area
			textArea.append(String.valueOf((char) b));

			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	}

	// Credit from https://www.codejava.net/java-se/swing/jtable-popup-menu-example
	public class TableMouseListener extends MouseAdapter {

		private JTable table;

		public TableMouseListener(JTable table) {
			this.table = table;
		}

		@Override
		public void mousePressed(MouseEvent event) {
			// selects the row at which point the mouse is clicked
			Point point = event.getPoint();
			int currentRow = table.rowAtPoint(point);
			table.setRowSelectionInterval(currentRow, currentRow);
		}
	}

	// Credit from https://stackoverflow.com/a/30691451
	public static boolean validate(final String ip) {
		String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

		return ip.matches(PATTERN);
	}

	// Credit from https://stackoverflow.com/a/237204
	public static boolean isPositiveInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			return false;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

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

		// Setting JPanel : Main Page, Upload Page, Upload Progress Page,
		// Download Progress Page, List Page

		JPanel ipAddress = new JPanel();
		frame.getContentPane().add(ipAddress, "IP Address");
		ipAddress.setLayout(null);

		JLabel lblEnterIpAddress = new JLabel("Enter IP Address");
		lblEnterIpAddress.setBounds(170, 6, 109, 16);
		ipAddress.add(lblEnterIpAddress);

		txtIpAddress = new JTextField();
		txtIpAddress.setBounds(62, 111, 204, 26);
		ipAddress.add(txtIpAddress);
		txtIpAddress.setColumns(10);
		
		JLabel lblConnecting = new JLabel("Connecting...");
		lblConnecting.setBounds(156, 70, 110, 16);
		lblConnecting.setVisible(false);
		ipAddress.add(lblConnecting);

		JButton btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lblConnecting.setVisible(true);
				address = txtIpAddress.getText();
				if (!validate(address)) {
					JOptionPane.showMessageDialog(null, "IP Address not valid.");
					lblConnecting.setVisible(false);
					return;
				} else {
					
						(new Thread() {
							public void run() {
								try {
									client = new Client(address, 59090);
									Container cards = frame.getContentPane();
									CardLayout cl = (CardLayout) cards.getLayout();
									cl.show(cards, "Home");
									System.out.println("GUI - [Page] Home");
								} catch (ConnectException e3) {
									lblConnecting.setVisible(false);
									if (e3.getLocalizedMessage().equals("Connection refused (Connection refused)")) {
										JOptionPane.showMessageDialog(null, "Connection refused","ERROR", JOptionPane.ERROR_MESSAGE);
										System.out.println("GUI - ERROR - Connection refused.");
									
									} else {
										JOptionPane.showMessageDialog(null, "Connection timed out","ERROR", JOptionPane.ERROR_MESSAGE);
										System.out.println("GUI - ERROR - Connection timed out");
									}
									System.out.println("GUI - TEST - "+e3.getLocalizedMessage());
									e3.printStackTrace();
								}
								catch (IOException e1) {
									lblConnecting.setVisible(false);
									JOptionPane.showMessageDialog(null, "Connection Failed", "ERROR", JOptionPane.ERROR_MESSAGE);
									System.out.println("Connection Failed");
									e1.printStackTrace();
								} catch (Exception e2) {
									lblConnecting.setVisible(false);
									e2.printStackTrace();
								}
							}
						}).start();
						
				}
			}
		});
		btnSubmit.setBounds(278, 111, 117, 29);
		ipAddress.add(btnSubmit);

		JButton btnExit_1 = new JButton("Exit");
		btnExit_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("GUI - Exit");
				System.exit(0);
			}
		});
		btnExit_1.setBounds(156, 225, 117, 29);
		ipAddress.add(btnExit_1);

		JPanel home = new JPanel();
		frame.getContentPane().add(home, "Home");
		home.setLayout(null);

		JPanel uploadProgress = new JPanel();
		frame.getContentPane().add(uploadProgress, "Upload Progress");
		uploadProgress.setLayout(null);

		JPanel uploadPage = new JPanel();
		frame.getContentPane().add(uploadPage, "Upload");
		uploadPage.setLayout(null);

		JLabel lblUploadPage = new JLabel("Upload Page");
		lblUploadPage.setBounds(175, 16, 111, 16);
		uploadPage.add(lblUploadPage);

		JButton btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("GUI - Back Button Pressed.");
				try {
					System.out.println("GUI - Client start reconnect.");
					client.close();
					client.reconnect(address, 59090);
					System.out.println("GUI - Client finish reconnect.");
					
					Container cards = frame.getContentPane();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "Home");
					System.out.println("GUI - [Page] Home Page.");
					
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Reconnection Failed", "ERROR", JOptionPane.ERROR_MESSAGE);
					System.out.println("GUI - Reconnection Failed.");
					
					Container cards = frame.getContentPane();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "IP Address");
					e1.printStackTrace();
					System.out.println("GUI - [Page] IP Address Page.");
				}
			}
		});
		btnBack.setBounds(327, 243, 117, 29);
		uploadPage.add(btnBack);

		JLabel lblFilePath = new JLabel("File Name");
		lblFilePath.setBounds(41, 55, 78, 16);
		uploadPage.add(lblFilePath);

		JLabel lblMinChunk = new JLabel("Minimum Chunk Size");
		lblMinChunk.setBounds(40, 114, 140, 16);
		uploadPage.add(lblMinChunk);

		JLabel lblD = new JLabel("Multiplier");
		lblD.setBounds(40, 144, 97, 16);
		uploadPage.add(lblD);

		JLabel lblAverageChunk = new JLabel("Average Chunk Size");
		lblAverageChunk.setBounds(40, 174, 140, 16);
		uploadPage.add(lblAverageChunk);

		JLabel lblMaximumChunk = new JLabel("Maximum Chunk Size");
		lblMaximumChunk.setBounds(40, 204, 140, 16);
		uploadPage.add(lblMaximumChunk);

		JSeparator separator = new JSeparator();
		separator.setBounds(40, 100, 372, 12);
		uploadPage.add(separator);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(40, 129, 372, 12);
		uploadPage.add(separator_1);

		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(40, 159, 372, 12);
		uploadPage.add(separator_2);

		JSeparator separator_3 = new JSeparator();
		separator_3.setBounds(40, 189, 372, 12);
		uploadPage.add(separator_3);

		JSeparator separator_4 = new JSeparator();
		separator_4.setBounds(40, 219, 372, 12);
		uploadPage.add(separator_4);
		
		JProgressBar uploadProgressBar = new JProgressBar(0,100);
		uploadProgressBar.setBounds(6, 81, 438, 20);
		uploadProgressBar.setValue(0);
		uploadProgress.add(uploadProgressBar);

		JLabel estimateTimeLabel = new JLabel("0s");
		estimateTimeLabel.setBounds(112, 113, 189, 16);
		uploadProgress.add(estimateTimeLabel);
		
		JButton btnUpload_1 = new JButton("Upload");
		btnUpload_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("GUI - Upload Button Pressed.");
				
				File file = chooserFilePath.getSelectedFile();

				if (file == null) {
					JOptionPane.showMessageDialog(null, "No file is selected!", "ERROR", JOptionPane.ERROR_MESSAGE);
					System.out.println("GUI - ERROR - No file selected.");
					return;
				} else if (!file.exists()) {
					JOptionPane.showMessageDialog(null, "The file is not exist!", "ERROR", JOptionPane.ERROR_MESSAGE);
					System.out.println("GUI - ERROR - File not exist");
					return;
				}

				String filepath = chooserFilePath.getSelectedFile().getAbsolutePath();
				String minChunk = txtMinChunk.getText();
				String d = txtD.getText();
				String averageChunk = txtAverageChunk.getText();
				String maxChunk = txtMaxChunk.getText();

				// System.out.println(minChunk + " " + d + " " + averageChunk + " " + maxChunk);

				if (filepath.isEmpty() || minChunk.isEmpty() || d.isEmpty() || averageChunk.isEmpty()
						|| maxChunk.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Some field is empty!", "ERROR", JOptionPane.ERROR_MESSAGE);
					System.out.println("GUI - ERROR - Empty Field Detected.");
				} else if (!isPositiveInteger(minChunk) || !isPositiveInteger(d) || !isPositiveInteger(averageChunk)
						|| !isPositiveInteger(maxChunk)) {
					JOptionPane.showMessageDialog(null, "You must enter positive integer for the 2nd to 5th field.",
							"ERROR", JOptionPane.ERROR_MESSAGE);
					System.out.println("GUI - ERROR - Negative number / not integer detected.");
				} else {
					try {

						Container cards = frame.getContentPane();
						CardLayout cl = (CardLayout) cards.getLayout();
						cl.show(cards, "Upload Progress");
						System.out.println("GUI - [Page] Upload Progress");
						
						uploadThread = new Thread() {
							public void run() {
								System.out.println("GUI - Start Thread (Upload)...");
								try {
									int result = client.upload(filepath, Integer.parseInt(minChunk), Integer.parseInt(d),
											Integer.parseInt(averageChunk), Integer.parseInt(maxChunk),
											uploadProgressBar,estimateTimeLabel);
									if (result == Client.NO_ERROR) {
										JOptionPane.showMessageDialog(null, "The file is uploaded!", "Notification", JOptionPane.PLAIN_MESSAGE);
									} else if (result == Client.SAME_FILENAME_EXIST) {
										JOptionPane.showMessageDialog(null, "Filename already exist in server. No allow to upload","Filename exist", JOptionPane.ERROR_MESSAGE);
									}
								}catch (NumberFormatException nfe) {
									JOptionPane.showMessageDialog(null,
											"Possible Reason:\n1.You enter leading zero\n2.The number is too large",
											"NumberFormatException", JOptionPane.ERROR_MESSAGE);
								} catch (NoSuchAlgorithmException nsae) {
									JOptionPane.showMessageDialog(null,
											"Cryptographic algorithm (SHA-256) is requested but is not available in the environment",
											"NoSuchAlgorithmException", JOptionPane.ERROR_MESSAGE);
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} 
								System.out.println("GUI - End Thread (Upload).");
							}
						};
						
						uploadThread.start();
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null,
								"Possible Reason:\n1.You enter leading zero\n2.The number is too large",
								"NumberFormatException", JOptionPane.ERROR_MESSAGE);
					}
				}

			}
		});
		btnUpload_1.setBounds(155, 243, 117, 29);
		uploadPage.add(btnUpload_1);

		chooserFilePath = new JFileChooser();
		//chooserFilePath.setBounds(123, 67, 225, 39);
		//uploadPage.add(chooserFilePath);

		txtMinChunk = new JTextField();
		txtMinChunk.setBounds(218, 109, 130, 26);
		uploadPage.add(txtMinChunk);
		txtMinChunk.setColumns(10);

		txtD = new JTextField();
		txtD.setBounds(218, 139, 130, 26);
		uploadPage.add(txtD);
		txtD.setColumns(10);

		txtAverageChunk = new JTextField();
		txtAverageChunk.setBounds(218, 169, 130, 26);
		uploadPage.add(txtAverageChunk);
		txtAverageChunk.setColumns(10);

		txtMaxChunk = new JTextField();
		txtMaxChunk.setBounds(218, 199, 130, 26);
		uploadPage.add(txtMaxChunk);
		txtMaxChunk.setColumns(10);

		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = chooserFilePath.showDialog(null, "Attach");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					(new Thread() {
						public void run() {
							System.out.println("GUI - Start Thread (set Text)...");
							chooserFilePath.setSelectedFile(chooserFilePath.getSelectedFile());
							filePathTextField.setText(chooserFilePath.getSelectedFile().getName());
							System.out.println("GUI - End Thread (set Text).");
						}
					}).start();
				}
			}
		});
		btnOpen.setBounds(225, 75, 93, 29);
		uploadPage.add(btnOpen);

		JLabel lblEg = new JLabel("E.g. 4096");
		lblEg.setBounds(360, 114, 61, 16);
		uploadPage.add(lblEg);

		JLabel lblEg_1 = new JLabel("E.g. 257");
		lblEg_1.setBounds(360, 144, 61, 16);
		uploadPage.add(lblEg_1);

		JLabel lblEg_2 = new JLabel("E.g.10000");
		lblEg_2.setBounds(360, 174, 84, 16);
		uploadPage.add(lblEg_2);

		JLabel lblEg_3 = new JLabel("E.g. 40000");
		lblEg_3.setBounds(360, 204, 84, 16);
		uploadPage.add(lblEg_3);
		
		filePathTextField = new JTextField();
		filePathTextField.setText("No File Selected.");
		filePathTextField.setHorizontalAlignment(SwingConstants.LEFT);
		filePathTextField.setEditable(false);
		filePathTextField.setBounds(185, 50, 236, 26);
		uploadPage.add(filePathTextField);
		filePathTextField.setColumns(10);
		
		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("GUI - Reset Button Pressed.");
				chooserFilePath.setSelectedFile(null);
				filePathTextField.setText("No File Selected.");
			}
		});
		btnReset.setBounds(327, 75, 94, 29);
		uploadPage.add(btnReset);;

		JButton btnBack_1 = new JButton("Back");
		btnBack_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					/*if (uploadThread.isAlive()){
						System.out.println("UploadThread end.");
						uploadThread.interrupt();
					}*/
					System.out.println("GUI - Client start reconnect.");
					client.close();
					client.reconnect(address, 59090);
					System.out.println("GUI - Client finish reconnect.");
					
					Container cards = frame.getContentPane();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "Upload");
					System.out.println("GUI - [Page] Upload");
				} catch (IOException e1) {
					System.out.println("GUI - Reconnection Failed.");
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "Reconnection Failed", "ERROR", JOptionPane.ERROR_MESSAGE);
					
					Container cards = frame.getContentPane();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "IP Address");
					System.out.println("GUI - [Page] IP Address");
				}
			}
		});
		btnBack_1.setBounds(327, 243, 117, 29);
		uploadProgress.add(btnBack_1);

		JLabel lblUploading = new JLabel("Uploading...");
		lblUploading.setBounds(184, 6, 84, 16);
		uploadProgress.add(lblUploading);

		JLabel lblEstimateTime = new JLabel("Estimate Time :");
		lblEstimateTime.setBounds(6, 113, 108, 16);
		uploadProgress.add(lblEstimateTime);

		JPanel downloadProgress = new JPanel();
		frame.getContentPane().add(downloadProgress, "Download Progress");
		downloadProgress.setLayout(null);

		JLabel lblDownloading = new JLabel("Downloading...");
		lblDownloading.setBounds(166, 6, 117, 16);
		downloadProgress.add(lblDownloading);

		JButton btnBack_2 = new JButton("Back");
		btnBack_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					System.out.println("GUI - Client start reconnect.");
					client.close();
					client.reconnect(address, 59090);
					System.out.println("GUI - Client finish reconnect.");
					
					Container cards = frame.getContentPane();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "Home");
					System.out.println("GUI - [Page] Home");
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Reconnection Failed", "ERROR", JOptionPane.ERROR_MESSAGE);
					Container cards = frame.getContentPane();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "IP Address");
					e1.printStackTrace();
				}
			}
		});
		btnBack_2.setBounds(327, 243, 117, 29);
		downloadProgress.add(btnBack_2);
		
		JProgressBar downloadProgressBar = new JProgressBar();
		downloadProgressBar.setBounds(6, 96, 438, 20);
		downloadProgress.add(downloadProgressBar);
		
		JLabel lblEstimateTime_1 = new JLabel("Estimate Time :");
		lblEstimateTime_1.setBounds(6, 148, 103, 16);
		downloadProgress.add(lblEstimateTime_1);
		
		JLabel lblChunkDownloaded = new JLabel("Chunk downloaded : ");
		lblChunkDownloaded.setBounds(6, 120, 133, 16);
		downloadProgress.add(lblChunkDownloaded);
		
		JLabel currentChunk = new JLabel("???");
		currentChunk.setHorizontalAlignment(SwingConstants.RIGHT);
		currentChunk.setBounds(148, 120, 61, 16);
		downloadProgress.add(currentChunk);
		
		JLabel label_1 = new JLabel("/");
		label_1.setBounds(221, 120, 14, 16);
		downloadProgress.add(label_1);
		
		JLabel totalChunk = new JLabel("Total");
		totalChunk.setBounds(238, 120, 61, 16);
		downloadProgress.add(totalChunk);
		
		JLabel downloadTime = new JLabel("Estimating...");
		downloadTime.setBounds(114, 148, 133, 16);
		downloadProgress.add(downloadTime);

		JPanel listPage = new JPanel();
		frame.getContentPane().add(listPage, "List");
		listPage.setLayout(null);

		JLabel lblFileInThe = new JLabel("File in the Server");
		lblFileInThe.setBounds(162, 6, 131, 16);
		listPage.add(lblFileInThe);

		String[] columns = { "Filename", "Size" };
		@SuppressWarnings("serial")
		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JButton btnBack_4 = new JButton("Back");
		btnBack_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					System.out.println("GUI - Client start reconnect.");
					client.close();
					client.reconnect(address, 59090);
					System.out.println("GUI - Client finish reconnect.");
					
					model.setRowCount(0);
					
					Container cards = frame.getContentPane();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "Home");
					System.out.println("GUI - [Page] Home");
				} catch (IOException e1) {
					System.out.println("GUI - Reconnection Failed.");
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "Reconnection Failed", "ERROR", JOptionPane.ERROR_MESSAGE);
					
					Container cards = frame.getContentPane();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "IP Address");
					System.out.println("GUI - [Page] IP Address");
					
				}
			}
		});
		btnBack_4.setBounds(327, 243, 117, 29);
		listPage.add(btnBack_4);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(30, 30, 390, 200);
		listPage.add(scrollPane);

		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuItemDownload = new JMenuItem("Download");
		menuItemDownload.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					System.out.println("GUI - Client start reconnect.");
					client.close();
					client.reconnect(address, 59090);
					System.out.println("GUI - Client finish reconnect.");

					int selectedRow = table.getSelectedRow();
					String filename = (String) table.getValueAt(selectedRow, 0);

					Container cards = frame.getContentPane();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "Download Progress");
					System.out.println("GUI - [Page] Download Progress");
					
					(new Thread() {
						public void run() {
							System.out.println("GUI - Start Thread (download)... ");
							try {
								client.download(filename, filename, 
										currentChunk, totalChunk, downloadTime, downloadProgressBar);

							} catch (IOException e) {
								System.out.println("GUI - ERROR - Connection Failed.");
								e.printStackTrace();
								
								JOptionPane.showMessageDialog(null, "Connection Failed", "ERROR",
										JOptionPane.ERROR_MESSAGE);
								
								cl.show(cards, "IP Address");
								System.out.println("GUI - [Page] IP Address");
							}
							System.out.println("GUI - End Thread (download)... ");
						}
					}).start();
					model.setRowCount(0);

				} catch (IOException e1) {
					System.out.println("GUI - ERROR - Connection Failed.");
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "Connection Failed", "ERROR", JOptionPane.ERROR_MESSAGE);
					
					Container cards = frame.getContentPane();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "IP Address");
					System.out.println("GUI - [Page] IP Address");
				}
			}
		});

		JMenuItem menuItemDelete = new JMenuItem("Delete");
		menuItemDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					System.out.println("GUI - Client start reconnect.");
					client.close();
					client.reconnect(address, 59090);
					System.out.println("GUI - Client finish reconnect.");

					int selectedRow = table.getSelectedRow();
					String filename = (String) table.getValueAt(selectedRow, 0);
					System.err.println(filename);

					(new Thread() {
						public void run() {
							System.out.println("GUI - Start Thread (Delete)...");
							try {
								client.delete(filename);
								model.removeRow(selectedRow);
							} catch (IOException e) {
								System.out.println("GUI - ERROR - Connection Failed.");
								e.printStackTrace();
								JOptionPane.showMessageDialog(null, "Connection Failed", "ERROR",
										JOptionPane.ERROR_MESSAGE);
								
								
								Container cards = frame.getContentPane();
								CardLayout cl = (CardLayout) cards.getLayout();
								cl.show(cards, "IP Address");
								System.out.println("GUI - [Page] IP Address");
							}
							System.out.println("GUI - End Thread (Delete).");
						}
					}).start();

					// model.setRowCount(0);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		});

		popupMenu.add(menuItemDownload);
		popupMenu.add(menuItemDelete);

		table = new JTable(model);
		table.setComponentPopupMenu(popupMenu);
		table.addMouseListener(new TableMouseListener(table));
		scrollPane.setViewportView(table);

		JLabel lblClientProgramFor = new JLabel("Client Program for Deduplication Cloud");
		lblClientProgramFor.setBounds(100, 6, 248, 16);
		home.add(lblClientProgramFor);

		JButton btnUpload = new JButton("Upload");
		btnUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("GUI - Upload Button Pressed");
				
				Container cards = frame.getContentPane();
				CardLayout cl = (CardLayout) cards.getLayout();
				cl.show(cards, "Upload");
				System.out.println("GUI - [Page] Upload");
			}
		});
		btnUpload.setBounds(166, 98, 117, 29);
		home.add(btnUpload);

		JButton btnListFile = new JButton("List File");
		btnListFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				System.out.println("GUI - List File Button Pressed");
				Container cards = frame.getContentPane();
				CardLayout cl = (CardLayout) cards.getLayout();
				cl.show(cards, "List");
				System.out.println("GUI - [Page] List");

				(new Thread() {
					public void run() {
						try {
							int list_protocol = client.list();
							if (list_protocol == Client.LIST_RETREIVED){
								ArrayList<String[]> data = client.fileList;
								for (int i = 0; i < data.size(); i++) {
									System.err.print(data.get(i)[1]);
									
									if (data.get(i)[1] == "/" && data.get(i)[0] == "Error Occur.") {
										JOptionPane.showMessageDialog(null, "Unknown Error Occur", "ERROR",
												JOptionPane.ERROR_MESSAGE);
										cl.show(cards, "Home");
										Thread.currentThread().interrupt();
										break;
									} else if (data.get(i)[1] == "/" && data.get(i)[0] == "No file in the server.") {
										JOptionPane.showMessageDialog(null, "No file in the server.", "ERROR",
												JOptionPane.ERROR_MESSAGE);
										cl.show(cards, "Home");
										Thread.currentThread().interrupt();
										break;
									}

									model.addRow(data.get(i));
								}
							} else if (list_protocol == Client.LIST_NO_FILE) {
								JOptionPane.showMessageDialog(null, "No file in the server.", "ERROR",
										JOptionPane.ERROR_MESSAGE);
								cl.show(cards, "Home");
								System.out.println("GUI - [Page] Home");
								
								System.out.println("GUI - Client start reconnect.");
								client.close();
								client.reconnect(address, 59090);
								System.out.println("GUI - Client finish reconnect.");
								
								Thread.currentThread().interrupt();
								
							} else if (list_protocol == Client.LIST_RETREIVE_FAILED) {
								JOptionPane.showMessageDialog(null, "Unknown Error Occur", "ERROR",
										JOptionPane.ERROR_MESSAGE);
								cl.show(cards, "Home");
								System.out.println("GUI - [Page] Home");
								
								System.out.println("GUI - Client start reconnect.");
								client.close();
								client.reconnect(address, 59090);
								System.out.println("GUI - Client finish reconnect.");
								
								Thread.currentThread().interrupt();
							}
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}).start();

			}
		});
		btnListFile.setBounds(166, 139, 117, 29);
		home.add(btnListFile);

		JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("GUI - Exit");
				System.exit(0);
			}
		});
		btnExit.setBounds(324, 242, 117, 29);
		home.add(btnExit);
	}
}
