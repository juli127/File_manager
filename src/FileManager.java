package ua.itea.homework.filemanager.src;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class FileManager extends JFrame {

	private JPanel FileListPanel;
	private JPanel DirListPanel;
	private JPanel mainPanel;
	private JLabel title;
	private JTextArea descr;
	private Font textFont;
	private Font titleFont;
	private File currentFile;
	private JMenuItem jmiCopyFile;
	private JMenuItem jmiCopyDir;
	private JMenuItem jmiPasteFile;
	private JMenuItem jmiPasteDir;
	private JMenuItem jmiExit;

	private File fileFrom;
	private File dirFrom;

	public static void main(String[] args) {
		new FileManager(new File("C:"));
	}

////////////////////////////////////////////////////////////////////
	FileManager(File file) {
		super("File manager");
		currentFile = file;

		// create directories list that are included in root directory 'file'
		// all of them have the same start nesting level == 1
		LinkedList<Node> dirList = createDirectoriesList(file, 1);

		textFont = new Font("Verdana", Font.PLAIN, 18);
		titleFont = new Font("Verdana", Font.BOLD, 22);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1500, 1500);
		setResizable(true);
		setLocationRelativeTo(null);

		String path = file.getPath();
		title = new JLabel(path);
		title.setFont(titleFont);
		title.setForeground(Color.BLUE);

		JPanel currPanel = new JPanel();
		add(title, BorderLayout.NORTH);
		add(currPanel, BorderLayout.CENTER);
		currPanel.setLayout(new GridBagLayout());

		JMenuBar menu = new JMenuBar();
		JMenu jmFile = new JMenu("File");
		JMenuItem jmiCopyFile = new JMenuItem("Copy File");
		JMenuItem jmiCopyDir = new JMenuItem("Copy Directory");
		JMenuItem jmiPasteFile = new JMenuItem("Paste File");
		JMenuItem jmiPasteDir = new JMenuItem("Paste Directory");
		JMenuItem jmiExit = new JMenuItem("Exit");
		jmFile.add(jmiCopyFile);
		jmFile.add(jmiCopyDir);
		jmFile.add(jmiPasteFile);
		jmFile.add(jmiPasteDir);
		jmFile.add(jmiExit);
		menu.add(jmFile);

		jmiCopyFile.setFont(textFont);
		jmiCopyDir.setFont(textFont);
		jmiExit.setFont(textFont);
		jmiPasteFile.setFont(textFont);
		jmiPasteDir.setFont(textFont);
		jmFile.setFont(textFont);
		setJMenuBar(menu);

		jmiCopyFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileFrom = currentFile;
			}
		});

		jmiPasteFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteFile(fileFrom, currentFile);
			}
		});

		jmiExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		jmiCopyDir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dirFrom = currentFile;
			}
		});

		jmiPasteDir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteDir(dirFrom, currentFile);
			}
		});

		// DirListPanel contains list of all directories from the root path
		// we can drop down / drop up every directory that has sub-directories
		DirListPanel = new JPanel();
		DirListPanel.setLayout(new GridBagLayout());
		JScrollPane scrDirLeftPanel = new JScrollPane(DirListPanel);

		// FileListPanel contains the list of all directories and files
		// that are located in the selected directory on DirListPanel
		FileListPanel = new JPanel();
		FileListPanel.setLayout(new GridBagLayout());
		JScrollPane scrFileCenterPanel = new JScrollPane(FileListPanel);

		// JTextArea contains the content of selected file from FileListPanel
		descr = new JTextArea("");
		descr.setFont(textFont);
		descr.setSize(300, 300);
		descr.setLineWrap(true);
		descr.setAutoscrolls(true);
		descr.setMaximumSize(new Dimension(300, 300));
		JScrollPane scrRightTextArea = new JScrollPane(descr);

		currPanel.add(scrDirLeftPanel, new GridBagConstraints(0, 0, 300, 0, 0.1, 0.1, GridBagConstraints.WEST,
				GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 5, 5));

		currPanel.add(scrFileCenterPanel, new GridBagConstraints(0, 1, 300, 0, 0.1, 0.1, GridBagConstraints.CENTER,
				GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 5, 5));

		currPanel.add(scrRightTextArea, new GridBagConstraints(0, 2, 300, 0, 0.1, 0.1, GridBagConstraints.LAST_LINE_END,
				GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 5, 5));

		repaintLeftDirPanel(dirList);
		setVisible(true);
	}

	///////////////////////////////////////
	
	private void pasteDir(File dirOrig, File dirTarget) {
		// at first, copy all folders
		recursivelyCopyDirs(dirOrig, dirTarget);
		
		// then, copy all included files into copied folders
		recursivelyCopyFiles(dirOrig, dirTarget);
	}

	private void recursivelyCopyDirs(File dirOrig, File dirTarget) {
		if (dirOrig != null) {
			String nameOrig = dirOrig.getName();
			String fullNameTarget = dirTarget.getPath() + "\\" + nameOrig.concat("_copy");
			File newTargetDir = new File(fullNameTarget);
			newTargetDir.mkdir();
			newTargetDir.setWritable(true);
			newTargetDir.setExecutable(true);

			for (File f : dirOrig.listFiles()) {
					if (f != null && f.isDirectory()) {
						newTargetDir = new File(fullNameTarget);
						recursivelyCopyDirs(f, newTargetDir);
					}
			}
		}
	}
	
	private void recursivelyCopyFiles(File dirOrig, File dirTarget) {
		
		if (dirOrig != null) {
			String nameDirOrig = dirOrig.getName();
			String fullNameDirTarget = dirTarget.getPath() + "\\" + nameDirOrig.concat("_copy");
			
			for (File f : dirOrig.listFiles()) {
				if (f != null) {
					File newTargetDir = new File(fullNameDirTarget);
					if (f.isFile()) {
						pasteFile(f, newTargetDir);
					}
					if (f.isDirectory()) {
						recursivelyCopyFiles(f, newTargetDir);
					}
				}
			}
		}
	}

	private void pasteFile(File fileOrig, File fileTarget) {
		
		Path filePathFrom = Paths.get(fileOrig.getPath());
		String nameOrig = fileOrig.getName();
		int pos = nameOrig.lastIndexOf('.');
		String nameTarget = fileTarget.getPath() + "\\"
				+ nameOrig.substring(0, pos).concat("_copy").concat(nameOrig.substring(pos));

		Path filePathTo = Paths.get(nameTarget);
		try {
			if (filePathFrom != null && filePathTo != null) {
				System.out.println("copy from " + filePathFrom + " to " + filePathTo);
				Files.copy(filePathFrom, filePathTo, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
////////////////////////////////////////////////////////////////////
	private void repaintLeftDirPanel(LinkedList<Node> dirList) {

		if (dirList != null) {

			DirListPanel.removeAll();

			for (int i = 0; i < dirList.size(); i++) {
				Node currNode = dirList.get(i);
				File currFile = currNode.getFile();
				int currLevel = currNode.getLevel();
				String currSign = currNode.getSign();

				StringBuilder tab = new StringBuilder("");
				for (int j = 0; j < currLevel; j++) {
					tab.append("-");
				}

				JButton btn = new JButton(tab.toString() + currFile.getName());
				btn.setHorizontalAlignment(SwingConstants.LEFT);
				btn.setFont(textFont);

				if (currFile.isFile()) {
					continue;
				}

				JButton signBtn = new JButton("");
				signBtn.setText(currNode.getSign());
				signBtn.setFont(textFont);

				DirListPanel.add(signBtn, new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.BASELINE,
						GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

				DirListPanel.add(btn, new GridBagConstraints(1, i, 1, 1, 0, 0, GridBagConstraints.BASELINE,
						GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

				signBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						File fileDrop = currFile;
						if (fileDrop.exists()) {
							title.setText(fileDrop.getPath());
							currentFile = fileDrop;
						}
						if (fileDrop != null && fileDrop.isDirectory()) {
							LinkedList<Node> newDirList = null;
							if (currSign.equals("v")) {
								currNode.setSign(">");
								newDirList = dropUpList(dirList, currNode);
							} else if (currSign.equals(">")) {
								currNode.setSign("v");
								newDirList = dropDownList(dirList, currNode);
							}
							repaintLeftDirPanel(newDirList);

							ArrayList<File> filesList = createFilesList(currFile);
							repaintCenterFilePanel(filesList);
						}
					}
				});
				
				btn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (currFile.exists()) {
							title.setText(currFile.getPath());
							currentFile = currFile;
							// repaint list of files (shown on FileListPanel) that are located in selected directory 'currFile'
							ArrayList<File> filesList = createFilesList(currFile);
							repaintCenterFilePanel(filesList);
						}
					}
				});
			}
			File byDefaultFile = dirList.get(0).getFile();
			ArrayList<File> filesList = createFilesList(byDefaultFile);
			repaintCenterFilePanel(filesList);
			descr.setText(" ");
			repaint();
		}
	}

////////////////////////////////////////////////////////////////////

	private LinkedList<Node> dropDownList(LinkedList<Node> list, Node currNode) {
		if (list.contains(currNode)) {
			int posInsert = list.indexOf(currNode);
			LinkedList<Node> listToInsert = createDirectoriesList(currNode.getFile(), currNode.getLevel() + 1);
			for (Node node : listToInsert) {
				list.add(++posInsert, node);
			}
		}
		return list;
	}

	private LinkedList<Node> dropUpList(LinkedList<Node> list, Node currNode) {
		if (list.contains(currNode)) {
			LinkedList<Node> listToRemove = createDirectoriesList(currNode.getFile(), currNode.getLevel() + 1);
			list.removeAll(listToRemove);
		}
		return list;
	}

	private boolean dirHasAnotherdirs(File dir) {
		File[] list = dir.listFiles();
		if (list != null) {
			for (File f : list) {
				if (f.isDirectory()) {
					return true;
				}
			}
		}
		return false;
	}

////////////////////////////////////////////////////////////////////
	private LinkedList<Node> createDirectoriesList(File root, int level) {

		LinkedList<Node> localList = new LinkedList<Node>();
		String rootPath = root.getPath();
		String path = "";
		File file = null;

		if (rootPath.equals("C:")) {
			path = rootPath + "/";
		} else {
			path = rootPath;
		}

		file = new File(path);
		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {

				if (f.isDirectory()) {

					String sign = " ";
					if (dirHasAnotherdirs(f)) {
						sign = ">";
					}
					Node newNode = new Node(f, level, sign);

					if (localList != null && !localList.contains((Node) newNode)) {
						localList.add(newNode);
					}
				}
			}
		}
		return localList;
	}

	private ArrayList<File> createFilesList(File selectedFile) {

		ArrayList<File> localList = new ArrayList<File>();
		String selectedPath = selectedFile.getPath();
		String path = selectedPath;
		File file;

		if (selectedPath.equals("C:")) {
			path = selectedPath + "/";
		}

		file = new File(path);
		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f != null && f.isDirectory()) {
					localList.add(f);
				}
			}
			for (File f : files) {
				if (f != null && f.isFile()) {
					localList.add(f);
				}
			}
		}
		return localList;
	}
////////////////////////////////////////////////////////////////////

	private void repaintCenterFilePanel(ArrayList<File> filesList) {

		if (filesList != null) {
			FileListPanel.removeAll();

			for (int i = 0; i < filesList.size(); i++) {
				File currFile = filesList.get(i);
				JButton btn = new JButton(currFile.getName());
				btn.setHorizontalAlignment(SwingConstants.LEFT);
				btn.setFont(textFont);
				descr.setText(" ");

				FileListPanel.add(btn, new GridBagConstraints(1, i, 1, 1, 0, 0, GridBagConstraints.BASELINE,
						GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

				btn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (currFile.exists()) {
							title.setText(currFile.getPath());
							currentFile = currFile;
							repaintRightTextArea(currFile);
						}
					}
				});
			}
			repaint();
		}
	}

	private void repaintRightTextArea(File fileSelected) {

		if (fileSelected.isFile()) {
			String fileName = fileSelected.getName();
			if (fileName.endsWith("txt") || fileName.endsWith("rtf") || fileName.endsWith("html")
					|| fileName.endsWith("xml") || fileName.endsWith("java") || fileName.endsWith("log")) {
				StringBuilder content = new StringBuilder();
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(fileSelected.getAbsolutePath()));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				String str = "";
				try {
					while ((str = br.readLine()) != null) {
						content.append(str).append("\n");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					br.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				descr.setText(content.toString());
			}
		} else {
			descr.setText(" ");
		}
		repaint();
	}
}
