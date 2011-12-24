package com.jeverbox.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.jeverbox.EverboxAPI;
import com.jeverbox.EverboxClient;
import com.jeverbox.EverboxConfig;

/**
 *
 * @author wendal
 */
public class EverboxManager {
	
	private static final Log log = Logs.get();

	private JFrame frmEverbox;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JTextField proxyPortField;
	private JTextField proxyHostField;
	private JTextField rootpathField;
	private JTextField uploadMaxsizeField;
	private JTextField downloadLimitField;
	private JTextField uploadMinsizeField;
	private JTextField uploadIgnoreSuffixField;
	private JTextField downloadIgnoreSuffixField;
	private JTextField downloadMinsizeField;
	private JTextField uploadLimitField;
	private JTextField downloadMaxsizeField;
	private JTextField uploadIgnorePath;
	private JTextField downloadIgnorePath;
	
	private EverboxClient client;
	private Thread clientThread;
	private JButton startClientButton;
	private JButton stopClientButton;
	private JCheckBox uploadIgnoreHiddenBox;
	private JCheckBox uploadEnableBox;
	private JCheckBox downloadEnableBox;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EverboxManager window = new EverboxManager();
					window.client = new EverboxClient();
					window.clientThread = new Thread(window.client);
					window.frmEverbox.setVisible(true);
					window.client.run = false;
				} catch (Throwable e) {
					log.error("运行出错!!", e);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public EverboxManager() {
		initialize();
		resetConfig();
		frmEverbox.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(stopClientButton.isEnabled()) {
					JOptionPane.showConfirmDialog(frmEverbox, "客户端仍在运行,请先按停止按钮");
				} else {
					if(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(frmEverbox, "确定要退出?")){
						frmEverbox.dispose();
					}
				}
			}
		});
	}
	
	public void resetConfig() {
		try {
			EverboxConfig.reloadConfig();
		} catch (Throwable e) {
			log.warn("重新加载配置失败!!",e);
		}
		usernameField.setText(EverboxConfig.get("username"));
		passwordField.setText(EverboxConfig.get("password"));
		proxyPortField.setText(EverboxConfig.get("proxy.port"));
		proxyHostField.setText(EverboxConfig.get("proxy.host"));
		rootpathField.setText(EverboxConfig.get("rootpath"));
		
		uploadEnableBox.setSelected("true".equalsIgnoreCase(EverboxConfig.get("upload.enable")));
		uploadLimitField.setText(EverboxConfig.get("upload.speed.limit"));
		uploadMaxsizeField.setText(EverboxConfig.get("upload.ignore.maxsize"));
		uploadMinsizeField.setText(EverboxConfig.get("upload.ignore.minsize"));
		uploadIgnoreSuffixField.setText(EverboxConfig.get("upload.ignore.suffix"));
		uploadIgnoreHiddenBox.setSelected("true".equalsIgnoreCase(EverboxConfig.get("upload.ignore.hidden")));
		uploadIgnorePath.setText(EverboxConfig.get("upload.ignore.paths"));
		

		downloadEnableBox.setSelected("true".equalsIgnoreCase(EverboxConfig.get("download.enable")));
		downloadLimitField.setText(EverboxConfig.get("download.speed.limit"));
		downloadIgnoreSuffixField.setText(EverboxConfig.get("download.ignore.suffix"));
		downloadMinsizeField.setText(EverboxConfig.get("download.ignore.minsize"));
		downloadMaxsizeField.setText(EverboxConfig.get("download.ignore.maxsize"));
		downloadIgnorePath.setText(EverboxConfig.get("download.ignore.paths"));
	}
	
	public void saveConfig() {
		Properties p = EverboxConfig.getP();
		String username = usernameField.getText();
		String password = new String(passwordField.getPassword());
		File fv = new File("everbox.data");
		if(!fv.exists()) {
			if(Strings.isBlank(username) || Strings.isBlank(password)) {
				JOptionPane.showConfirmDialog(frmEverbox, 
						"必须填写用户名和密码!! 请修改后再保存!!","无效配置", JOptionPane.OK_OPTION);
				return;
			}
		}
		if(!p.getProperty("username", "").equalsIgnoreCase(username)) {
			p.put("username", username);
			//用户名改变,需要清理快速登录信息
			if(fv.exists())
				fv.delete();
		}
		p.put("password", password);
		
		p.put("proxy.host", proxyHostField.getText());
		p.put("proxy.port", proxyPortField.getText());
		String rootPath = rootpathField.getText();
		if(Strings.isBlank(rootPath)) {
			JOptionPane.showConfirmDialog(frmEverbox, 
					"必须设置本地文件夹!! 请修改后再保存!!","警告!!", JOptionPane.OK_OPTION);
			return;
		}
		if(!p.getProperty("rootpath","").equals(rootPath)) {
			JOptionPane.showConfirmDialog(frmEverbox, 
					"你设置了新的本地文件夹,请慎重! 除非这是你第一次配置!!","提醒", JOptionPane.OK_OPTION);
		}
		

		if(uploadEnableBox.isSelected() && downloadEnableBox.isSelected()) {
			JOptionPane.showConfirmDialog(frmEverbox, 
					"当前版本暂不支持同时开启上传和下载!! 请修改后再保存!!","提醒",JOptionPane.OK_OPTION);
			return;
		}
		
		p.put("rootpath", rootpathField.getText());
		
		p.put("upload.enable", ""+uploadEnableBox.isSelected());
		p.put("upload.speed.limit", uploadLimitField.getText());
		p.put("upload.ignore.maxsize", uploadMaxsizeField.getText());
		p.put("upload.ignore.minsize", uploadMinsizeField.getText());
		p.put("upload.ignore.suffix", uploadIgnoreSuffixField.getText());
		p.put("upload.ignore.hidden", ""+uploadIgnoreHiddenBox.isSelected());
		p.put("upload.ignore.paths", ""+uploadIgnorePath.getText());
		
		p.put("download.enable", ""+downloadEnableBox.isSelected());
		p.put("download.speed.limit", downloadLimitField.getText());
		p.put("download.ignore.suffix", downloadIgnoreSuffixField.getText());
		p.put("download.ignore.minsize", downloadMinsizeField.getText());
		p.put("download.ignore.maxsize", downloadMaxsizeField.getText());
		p.put("download.ignore.paths", downloadIgnorePath.getText());
		
		
		try {
			File f = new File("config.properties");
			f.createNewFile();
			p.store(new FileOutputStream(f), "Everbox4j Config");
			JOptionPane.showConfirmDialog(frmEverbox, 
					"保存成功!! 如果你改变了用户名,请先退出客户端,然后再启动!","提醒",JOptionPane.OK_OPTION);
		} catch (Throwable e) {
			log.error("保存出错!!", e);
			JOptionPane.showConfirmDialog(frmEverbox, "保存失败!!!","警告",JOptionPane.OK_OPTION);
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmEverbox = new JFrame();
		frmEverbox.setIconImage(Toolkit.getDefaultToolkit().getImage(EverboxManager.class.getResource("/com/everbox4j/ui/EverBox4J.gif")));
		frmEverbox.setResizable(false);
		frmEverbox.setTitle("Everbox \u7BA1\u7406\u5668");
		frmEverbox.setBounds(100, 100, 450, 600);
		frmEverbox.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frmEverbox.setJMenuBar(menuBar);
		
		JMenu FileMenu = new JMenu("File");
		menuBar.add(FileMenu);
		
		JMenuItem ExitMenuItem = new JMenuItem("\u9000\u51FA");
		ExitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.run = false;
				stopClientButton.setText("正在关闭,请等候");
				while(true) {
					if(clientThread.isAlive())
						try {
							Thread.sleep(1000);
						} catch (Throwable e1) {
						}
					break;
				}
				frmEverbox.dispose();
			}
		});
		FileMenu.add(ExitMenuItem);
		
		JMenu cleanMenu = new JMenu("\u6E05\u7406");
		menuBar.add(cleanMenu);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("\u6E05\u7406\u767B\u9646\u4FE1\u606F");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = new File("everbox.data");
				if(f.exists())
					f.delete();
			}
		});
		mntmNewMenuItem.setEnabled(false);
		cleanMenu.add(mntmNewMenuItem);
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("\u6E05\u7406\u65E5\u5FD7\u4FE1\u606F");
		mntmNewMenuItem_1.setEnabled(false);
		cleanMenu.add(mntmNewMenuItem_1);
		
		JMenuItem mntmNewMenuItem_3 = new JMenuItem("\u6E05\u7406\u4E0B\u8F7D\u7528\u5230\u7684\u4E34\u65F6\u6587\u4EF6");
		mntmNewMenuItem_3.setEnabled(false);
		cleanMenu.add(mntmNewMenuItem_3);
		
		JMenuItem mntmNewMenuItem_4 = new JMenuItem("\u6E05\u7406\u6587\u4EF6\u6570\u636E\u5E93");
		mntmNewMenuItem_4.setEnabled(false);
		cleanMenu.add(mntmNewMenuItem_4);
		
		JMenu helpMenu = new JMenu("\u5E2E\u52A9");
		menuBar.add(helpMenu);
		
		JMenuItem vistHomepageMenuItem = new JMenuItem("\u8BBF\u95EE\u9879\u76EE\u9996\u9875");
		vistHomepageMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URL("http://everbox4j.googlecode.com").toURI());
				} catch (Throwable e2) {
					log.warn("启动浏览器失败!!",e2);
					JOptionPane.showConfirmDialog(frmEverbox, "启动浏览器失败!! http://everbox4j.googlecode.com");
				}
			}
		});
		helpMenu.add(vistHomepageMenuItem);
		
		JMenuItem sendMailFeedback = new JMenuItem("\u610F\u89C1\u53CD\u9988(\u53D1\u9001\u90AE\u4EF6)");
		sendMailFeedback.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().mail(new URL("everbox4j@wendal.net").toURI());
				} catch (Throwable e2) {
					log.warn("启动邮件客户端失败!!",e2);
					JOptionPane.showConfirmDialog(frmEverbox, "启动邮件客户端失败!! 反馈邮箱: everbox4j@wendal.net");
				}
			}
		});
		helpMenu.add(sendMailFeedback);
		
		JMenuItem aboutMenuItem = new JMenuItem("\u5173\u4E8E...");
		aboutMenuItem.setEnabled(false);
		helpMenu.add(aboutMenuItem);
		
		JPanel clientContrlPanel = new JPanel();
		frmEverbox.getContentPane().add(clientContrlPanel, BorderLayout.NORTH);
		
		JLabel label = new JLabel("\u8FD0\u884C\u72B6\u6001");
		clientContrlPanel.add(label);
		
		startClientButton = new JButton("\u542F\u52A8");
		startClientButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(client.run || clientThread.isAlive()) {
					stopClientButton.setEnabled(true);
					startClientButton.setEnabled(false);
					return;
				} else {
					client.run = true;
					try{
						EverboxAPI.login();
						clientThread = new Thread(client);
						clientThread.start();
						startClientButton.setEnabled(false);
						stopClientButton.setEnabled(true);
						JOptionPane.showConfirmDialog(frmEverbox, "登陆成功!! 可以查看everbox.log文件观察本客户端的运行情况.");
					}catch (Throwable e2) {
						log.debug("登陆失败",e2);
						JOptionPane.showConfirmDialog(frmEverbox, "登陆失败!! 请确认你已经正确设置用户名/密码,并且已经保存!");
						client.run = false;
						return;
					}
				}
			}
		});
		startClientButton.setToolTipText("\u542F\u52A8\u5BA2\u6237\u7AEF");
		clientContrlPanel.add(startClientButton);
		
		stopClientButton = new JButton("\u505C\u6B62");
		stopClientButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if((!client.run) || (!clientThread.isAlive())) {
					stopClientButton.setEnabled(false);
					startClientButton.setEnabled(true);
				} else {
					client.run = false;
					stopClientButton.setText("正在关闭,请等候");
					while(true) {
						if(clientThread.isAlive())
							try {
								Thread.sleep(1000);
							} catch (Throwable e1) {
							}
						break;
					}
					stopClientButton.setText("停止");
					startClientButton.setEnabled(true);
					stopClientButton.setEnabled(false);
				}
			}
		});
		stopClientButton.setToolTipText("\u505C\u6B62\u5BA2\u6237\u7AEF");
		stopClientButton.setEnabled(false);
		clientContrlPanel.add(stopClientButton);
		
		JButton btnNewButton_1 = new JButton("\u7ACB\u5373\u505C\u6B62");
		btnNewButton_1.setEnabled(false);
		clientContrlPanel.add(btnNewButton_1);
		
		JPanel panel = new JPanel();
		frmEverbox.getContentPane().add(panel, BorderLayout.CENTER);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		JButton btnNewButton_2 = new JButton("\u4FDD\u5B58\u914D\u7F6E");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveConfig();
			}
		});
		panel.add(btnNewButton_2);
		
		JLabel lblNewLabel = new JLabel("\u7528\u6237\u540D");
		sl_panel.putConstraint(SpringLayout.NORTH, lblNewLabel, 45, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblNewLabel, 10, SpringLayout.WEST, panel);
		panel.add(lblNewLabel);
		
		usernameField = new JTextField();
		sl_panel.putConstraint(SpringLayout.WEST, usernameField, 18, SpringLayout.EAST, lblNewLabel);
		sl_panel.putConstraint(SpringLayout.SOUTH, btnNewButton_2, -6, SpringLayout.NORTH, usernameField);
		usernameField.setToolTipText("\u6682\u65F6\u53EA\u652F\u6301\u76DB\u5927\u5E10\u6237");
		sl_panel.putConstraint(SpringLayout.NORTH, usernameField, 42, SpringLayout.NORTH, panel);
		panel.add(usernameField);
		usernameField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("\u5BC6\u7801");
		sl_panel.putConstraint(SpringLayout.WEST, lblNewLabel_1, 207, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, usernameField, -30, SpringLayout.WEST, lblNewLabel_1);
		sl_panel.putConstraint(SpringLayout.EAST, btnNewButton_2, 0, SpringLayout.EAST, lblNewLabel_1);
		sl_panel.putConstraint(SpringLayout.NORTH, lblNewLabel_1, 45, SpringLayout.NORTH, panel);
		panel.add(lblNewLabel_1);
		
		passwordField = new JPasswordField();
		passwordField.setToolTipText("\u8BF7\u586B\u5165\u5BC6\u7801");
		sl_panel.putConstraint(SpringLayout.WEST, passwordField, 6, SpringLayout.EAST, lblNewLabel_1);
		sl_panel.putConstraint(SpringLayout.EAST, passwordField, 159, SpringLayout.EAST, lblNewLabel_1);
		panel.add(passwordField);
		
		JCheckBox checkBox = new JCheckBox("\u542F\u7528\u4EE3\u7406");
		sl_panel.putConstraint(SpringLayout.WEST, checkBox, 10, SpringLayout.WEST, panel);
		checkBox.setEnabled(false);
		panel.add(checkBox);
		
		JLabel label_1 = new JLabel("\u4EE3\u7406\u670D\u52A1\u5668");
		sl_panel.putConstraint(SpringLayout.NORTH, label_1, 4, SpringLayout.NORTH, checkBox);
		sl_panel.putConstraint(SpringLayout.WEST, label_1, 6, SpringLayout.EAST, checkBox);
		panel.add(label_1);
		
		JLabel label_2 = new JLabel("\u7AEF\u53E3");
		sl_panel.putConstraint(SpringLayout.NORTH, label_2, 4, SpringLayout.NORTH, checkBox);
		panel.add(label_2);
		
		proxyPortField = new JTextField();
		proxyPortField.setEditable(false);
		sl_panel.putConstraint(SpringLayout.NORTH, proxyPortField, 1, SpringLayout.NORTH, checkBox);
		sl_panel.putConstraint(SpringLayout.WEST, proxyPortField, 6, SpringLayout.EAST, label_2);
		proxyPortField.setText("8080");
		panel.add(proxyPortField);
		proxyPortField.setColumns(10);
		
		proxyHostField = new JTextField();
		proxyHostField.setEditable(false);
		sl_panel.putConstraint(SpringLayout.WEST, proxyHostField, 6, SpringLayout.EAST, label_1);
		sl_panel.putConstraint(SpringLayout.EAST, proxyHostField, -172, SpringLayout.EAST, panel);
		sl_panel.putConstraint(SpringLayout.WEST, label_2, 6, SpringLayout.EAST, proxyHostField);
		sl_panel.putConstraint(SpringLayout.NORTH, proxyHostField, 1, SpringLayout.NORTH, checkBox);
		panel.add(proxyHostField);
		proxyHostField.setColumns(10);
		
		JLabel label_3 = new JLabel("\u672C\u5730\u6587\u4EF6\u5939");
		sl_panel.putConstraint(SpringLayout.NORTH, label_3, 21, SpringLayout.SOUTH, lblNewLabel);
		sl_panel.putConstraint(SpringLayout.WEST, label_3, 10, SpringLayout.WEST, panel);
		panel.add(label_3);
		
		rootpathField = new JTextField();
		sl_panel.putConstraint(SpringLayout.NORTH, checkBox, 13, SpringLayout.SOUTH, rootpathField);
		sl_panel.putConstraint(SpringLayout.NORTH, rootpathField, -3, SpringLayout.NORTH, label_3);
		sl_panel.putConstraint(SpringLayout.WEST, rootpathField, 6, SpringLayout.EAST, label_3);
		sl_panel.putConstraint(SpringLayout.EAST, rootpathField, -91, SpringLayout.EAST, panel);
		rootpathField.setEditable(false);
		rootpathField.setEnabled(false);
		panel.add(rootpathField);
		rootpathField.setColumns(10);
		
		JButton selectRootPathButton = new JButton("\u9009\u62E9");
		selectRootPathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setName("请选择本地存放文件夹");
				jfc.setDialogTitle("请选择本地存放文件夹");
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int rc = jfc.showOpenDialog(frmEverbox);
				if(rc == JFileChooser.APPROVE_OPTION )
					rootpathField.setText(jfc.getSelectedFile().getAbsolutePath());
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, selectRootPathButton, -4, SpringLayout.NORTH, label_3);
		sl_panel.putConstraint(SpringLayout.WEST, selectRootPathButton, 6, SpringLayout.EAST, rootpathField);
		panel.add(selectRootPathButton);
		
		uploadEnableBox = new JCheckBox("\u542F\u7528\u4E0A\u4F20");
		sl_panel.putConstraint(SpringLayout.WEST, uploadEnableBox, 10, SpringLayout.WEST, panel);
		uploadEnableBox.setToolTipText("\u542F\u7528\u4E0A\u4F20\u6A21\u5F0F");
		uploadEnableBox.setSelected(true);
		sl_panel.putConstraint(SpringLayout.NORTH, uploadEnableBox, 6, SpringLayout.SOUTH, checkBox);
		panel.add(uploadEnableBox);
		
		downloadEnableBox = new JCheckBox("\u542F\u7528\u4E0B\u8F7D");
		downloadEnableBox.setToolTipText("\u542F\u7528\u4E0B\u8F7D\u6A21\u5F0F");
		sl_panel.putConstraint(SpringLayout.NORTH, downloadEnableBox, 0, SpringLayout.NORTH, uploadEnableBox);
		sl_panel.putConstraint(SpringLayout.WEST, downloadEnableBox, 0, SpringLayout.WEST, lblNewLabel_1);
		panel.add(downloadEnableBox);
		
		JLabel lblNewLabel_2 = new JLabel("\u5FFD\u7565\u6587\u4EF6\u6269\u5C55\u540D");
		sl_panel.putConstraint(SpringLayout.WEST, lblNewLabel_2, 10, SpringLayout.WEST, panel);
		panel.add(lblNewLabel_2);
		
		uploadMaxsizeField = new JTextField();
		uploadMaxsizeField.setToolTipText("\u8BBE\u7F6E\u4E0A\u4F20\u6587\u4EF6\u6700\u5927\u5C3A\u5BF8,\u4F8B\u5982100m");
		panel.add(uploadMaxsizeField);
		uploadMaxsizeField.setColumns(10);
		
		JLabel label_4 = new JLabel("\u5FFD\u7565\u6587\u4EF6\u6269\u5C55\u540D");
		sl_panel.putConstraint(SpringLayout.NORTH, label_4, 0, SpringLayout.NORTH, lblNewLabel_2);
		sl_panel.putConstraint(SpringLayout.WEST, label_4, 0, SpringLayout.WEST, lblNewLabel_1);
		sl_panel.putConstraint(SpringLayout.EAST, label_4, 0, SpringLayout.EAST, label_2);
		panel.add(label_4);
		
		downloadLimitField = new JTextField();
		downloadLimitField.setEditable(false);
		sl_panel.putConstraint(SpringLayout.EAST, downloadLimitField, 0, SpringLayout.EAST, proxyPortField);
		downloadLimitField.setColumns(10);
		panel.add(downloadLimitField);
		
		JLabel label_5 = new JLabel("\u6587\u4EF6\u6700\u5927\u5C3A\u5BF8");
		sl_panel.putConstraint(SpringLayout.WEST, uploadMaxsizeField, 17, SpringLayout.EAST, label_5);
		sl_panel.putConstraint(SpringLayout.WEST, label_5, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.NORTH, label_5, 45, SpringLayout.SOUTH, uploadEnableBox);
		sl_panel.putConstraint(SpringLayout.NORTH, uploadMaxsizeField, -3, SpringLayout.NORTH, label_5);
		panel.add(label_5);
		
		JLabel label_6 = new JLabel("\u6587\u4EF6\u6700\u5C0F\u5C3A\u5BF8");
		sl_panel.putConstraint(SpringLayout.WEST, label_6, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.NORTH, lblNewLabel_2, 20, SpringLayout.SOUTH, label_6);
		sl_panel.putConstraint(SpringLayout.NORTH, label_6, 16, SpringLayout.SOUTH, label_5);
		panel.add(label_6);
		
		JLabel label_7 = new JLabel("\u5FFD\u7565\u8DEF\u5F84");
		sl_panel.putConstraint(SpringLayout.WEST, label_7, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, lblNewLabel, 0, SpringLayout.EAST, label_7);
		sl_panel.putConstraint(SpringLayout.NORTH, label_7, 44, SpringLayout.SOUTH, lblNewLabel_2);
		panel.add(label_7);
		
		uploadIgnoreHiddenBox = new JCheckBox("\u5FFD\u7565\u9690\u85CF\u6587\u4EF6");
		sl_panel.putConstraint(SpringLayout.WEST, uploadIgnoreHiddenBox, 10, SpringLayout.WEST, panel);
		uploadIgnoreHiddenBox.setToolTipText("\u662F\u5426\u5FFD\u7565\u9690\u85CF\u6587\u4EF6");
		sl_panel.putConstraint(SpringLayout.NORTH, uploadIgnoreHiddenBox, 20, SpringLayout.SOUTH, label_7);
		uploadIgnoreHiddenBox.setSelected(true);
		panel.add(uploadIgnoreHiddenBox);
		
		JLabel label_8 = new JLabel("\u6587\u4EF6\u6700\u5927\u5C3A\u5BF8");
		sl_panel.putConstraint(SpringLayout.EAST, uploadMaxsizeField, -30, SpringLayout.WEST, label_8);
		sl_panel.putConstraint(SpringLayout.EAST, label_8, 0, SpringLayout.EAST, label_2);
		sl_panel.putConstraint(SpringLayout.NORTH, label_8, 3, SpringLayout.NORTH, uploadMaxsizeField);
		sl_panel.putConstraint(SpringLayout.WEST, label_8, 0, SpringLayout.WEST, lblNewLabel_1);
		panel.add(label_8);
		
		JLabel label_9 = new JLabel("\u6587\u4EF6\u6700\u5C0F\u5C3A\u5BF8");
		sl_panel.putConstraint(SpringLayout.NORTH, label_9, 0, SpringLayout.NORTH, label_6);
		sl_panel.putConstraint(SpringLayout.WEST, label_9, 0, SpringLayout.WEST, lblNewLabel_1);
		sl_panel.putConstraint(SpringLayout.EAST, label_9, 0, SpringLayout.EAST, label_2);
		panel.add(label_9);
		
		JLabel label_10 = new JLabel("\u5FFD\u7565\u8DEF\u5F84");
		sl_panel.putConstraint(SpringLayout.NORTH, label_10, 0, SpringLayout.NORTH, label_7);
		sl_panel.putConstraint(SpringLayout.WEST, label_10, 0, SpringLayout.WEST, lblNewLabel_1);
		panel.add(label_10);
		
		uploadMinsizeField = new JTextField();
		sl_panel.putConstraint(SpringLayout.WEST, uploadMinsizeField, 17, SpringLayout.EAST, label_6);
		sl_panel.putConstraint(SpringLayout.EAST, uploadMinsizeField, -30, SpringLayout.WEST, label_9);
		uploadMinsizeField.setToolTipText("\u8BBE\u7F6E\u4E0A\u4F20\u6587\u4EF6\u6700\u5C0F\u5C3A\u5BF8,\u4F8B\u5982100k");
		sl_panel.putConstraint(SpringLayout.NORTH, uploadMinsizeField, -3, SpringLayout.NORTH, label_6);
		panel.add(uploadMinsizeField);
		uploadMinsizeField.setColumns(10);
		
		uploadIgnoreSuffixField = new JTextField();
		sl_panel.putConstraint(SpringLayout.WEST, uploadIgnoreSuffixField, 6, SpringLayout.EAST, lblNewLabel_2);
		sl_panel.putConstraint(SpringLayout.EAST, uploadIgnoreSuffixField, -30, SpringLayout.WEST, label_4);
		uploadIgnoreSuffixField.setToolTipText("\u9700\u8981\u5FFD\u7565\u7684\u6587\u4EF6\u540D\u540E\u7F00,\u53EF\u586B\u5165\u591A\u4E2A,\u7528\u82F1\u6587\u9017\u53F7\u5206\u9694");
		sl_panel.putConstraint(SpringLayout.NORTH, uploadIgnoreSuffixField, -3, SpringLayout.NORTH, lblNewLabel_2);
		panel.add(uploadIgnoreSuffixField);
		uploadIgnoreSuffixField.setColumns(10);
		
		downloadIgnoreSuffixField = new JTextField();
		downloadIgnoreSuffixField.setToolTipText("\u9700\u8981\u5FFD\u7565\u7684\u6587\u4EF6\u540D\u540E\u7F00,\u53EF\u586B\u5165\u591A\u4E2A,\u7528\u82F1\u6587\u9017\u53F7\u5206\u9694");
		sl_panel.putConstraint(SpringLayout.NORTH, downloadIgnoreSuffixField, -3, SpringLayout.NORTH, lblNewLabel_2);
		sl_panel.putConstraint(SpringLayout.WEST, downloadIgnoreSuffixField, 0, SpringLayout.WEST, proxyPortField);
		panel.add(downloadIgnoreSuffixField);
		downloadIgnoreSuffixField.setColumns(10);
		
		downloadMinsizeField = new JTextField();
		downloadMinsizeField.setToolTipText("\u8BBE\u7F6E\u4E0B\u8F7D\u6587\u4EF6\u6700\u5C0F\u5C3A\u5BF8,\u4F8B\u59821m");
		sl_panel.putConstraint(SpringLayout.NORTH, downloadMinsizeField, -3, SpringLayout.NORTH, label_6);
		sl_panel.putConstraint(SpringLayout.WEST, downloadMinsizeField, 0, SpringLayout.WEST, proxyPortField);
		panel.add(downloadMinsizeField);
		downloadMinsizeField.setColumns(10);
		
		uploadLimitField = new JTextField();
		uploadLimitField.setText("\u8BBE\u7F6E\u6700\u5927\u4E0A\u4F20\u901F\u5EA6");
		sl_panel.putConstraint(SpringLayout.SOUTH, uploadLimitField, -6, SpringLayout.NORTH, uploadMaxsizeField);
		uploadLimitField.setEditable(false);
		panel.add(uploadLimitField);
		uploadLimitField.setColumns(10);
		
		downloadMaxsizeField = new JTextField();
		downloadMaxsizeField.setToolTipText("\u8BBE\u7F6E\u4E0B\u8F7D\u6587\u4EF6\u6700\u5927\u5C3A\u5BF8,\u4F8B\u59821g");
		sl_panel.putConstraint(SpringLayout.SOUTH, downloadLimitField, -6, SpringLayout.NORTH, downloadMaxsizeField);
		sl_panel.putConstraint(SpringLayout.NORTH, downloadMaxsizeField, 0, SpringLayout.NORTH, uploadMaxsizeField);
		sl_panel.putConstraint(SpringLayout.WEST, downloadMaxsizeField, 0, SpringLayout.WEST, proxyPortField);
		panel.add(downloadMaxsizeField);
		downloadMaxsizeField.setColumns(10);
		
		JCheckBox checkBox_4 = new JCheckBox("\u5220\u9664\u672C\u5730\u6587\u4EF6\u9700\u8981\u786E\u8BA4");
		sl_panel.putConstraint(SpringLayout.WEST, checkBox_4, 10, SpringLayout.WEST, panel);
		checkBox_4.setEnabled(false);
		sl_panel.putConstraint(SpringLayout.NORTH, checkBox_4, 70, SpringLayout.SOUTH, uploadIgnoreHiddenBox);
		panel.add(checkBox_4);
		
		JCheckBox checkBox_5 = new JCheckBox("\u8986\u76D6\u672C\u5730\u6587\u4EF6\u9700\u8981\u786E\u8BA4");
		checkBox_5.setEnabled(false);
		sl_panel.putConstraint(SpringLayout.NORTH, checkBox_5, 0, SpringLayout.NORTH, checkBox_4);
		sl_panel.putConstraint(SpringLayout.WEST, checkBox_5, 0, SpringLayout.WEST, lblNewLabel_1);
		panel.add(checkBox_5);
		
		JLabel label_11 = new JLabel("\u4E0A\u4F20\u9650\u901F");
		sl_panel.putConstraint(SpringLayout.WEST, uploadLimitField, 16, SpringLayout.EAST, label_11);
		sl_panel.putConstraint(SpringLayout.EAST, label_11, 0, SpringLayout.EAST, checkBox);
		sl_panel.putConstraint(SpringLayout.WEST, label_11, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, label_11, -12, SpringLayout.NORTH, label_5);
		panel.add(label_11);
		
		uploadIgnorePath = new JTextField();
		sl_panel.putConstraint(SpringLayout.WEST, uploadIgnorePath, 41, SpringLayout.EAST, label_7);
		sl_panel.putConstraint(SpringLayout.EAST, uploadIgnorePath, -30, SpringLayout.WEST, label_10);
		uploadIgnorePath.setToolTipText("\u9700\u8981\u5FFD\u7565\u7684\u76F8\u5BF9\u8DEF\u5F84,\u7528\u82F1\u6587\u9017\u53F7\u5206\u9694");
		sl_panel.putConstraint(SpringLayout.SOUTH, uploadIgnorePath, 0, SpringLayout.SOUTH, label_7);
		panel.add(uploadIgnorePath);
		uploadIgnorePath.setColumns(10);
		
		downloadIgnorePath = new JTextField();
		downloadIgnorePath.setToolTipText("\u9700\u8981\u5FFD\u7565\u7684\u76F8\u5BF9\u8DEF\u5F84,\u7528\u82F1\u6587\u9017\u53F7\u5206\u9694");
		sl_panel.putConstraint(SpringLayout.NORTH, downloadIgnorePath, -3, SpringLayout.NORTH, label_7);
		sl_panel.putConstraint(SpringLayout.WEST, downloadIgnorePath, 0, SpringLayout.WEST, proxyPortField);
		panel.add(downloadIgnorePath);
		downloadIgnorePath.setColumns(10);
		
		JLabel label_12 = new JLabel("\u4E0B\u8F7D\u9650\u901F");
		sl_panel.putConstraint(SpringLayout.EAST, uploadLimitField, -30, SpringLayout.WEST, label_12);
		sl_panel.putConstraint(SpringLayout.EAST, label_12, 0, SpringLayout.EAST, label_2);
		sl_panel.putConstraint(SpringLayout.NORTH, label_12, 3, SpringLayout.NORTH, downloadLimitField);
		sl_panel.putConstraint(SpringLayout.WEST, label_12, 0, SpringLayout.WEST, lblNewLabel_1);
		panel.add(label_12);
		
		JButton button = new JButton("\u64A4\u9500\u4FEE\u6539");
		sl_panel.putConstraint(SpringLayout.NORTH, passwordField, 6, SpringLayout.SOUTH, button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetConfig();
			}
		});
		sl_panel.putConstraint(SpringLayout.NORTH, button, 0, SpringLayout.NORTH, btnNewButton_2);
		sl_panel.putConstraint(SpringLayout.EAST, button, 0, SpringLayout.EAST, passwordField);
		panel.add(button);
	}
}
