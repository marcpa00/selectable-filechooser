package ca.marcpa

import griffon.core.GriffonApplication
import griffon.util.GriffonApplicationUtils
import griffon.swing.WindowManager
 
import java.awt.*
import javax.swing.*;

class SelectableFileChooser {

	GriffonApplication app
	boolean chooseDirOnly = false
	boolean useNativeDialog 
	def fileChooser
	Frame frame
	String filename
	String dirname
	File file
	Closure prepareOpen
	Closure afterReturn
	String openTitle = "Open"
	String saveTitle = "Save"

	def createFileChooser = { useNative ->
		if (useNative) {
			// defaults to open file dialog
			this.fileChooser = new FileDialog(frame, this.openTitle)
			this.prepareOpen = {}
			this.afterReturn = { result -> 
				if (this.fileChooser.getFile() != null) {
					this.filename = this.fileChooser.file
					this.dirname = this.fileChooser.directory
					this.file = new File("${this.dirname}/${this.filename}")
				}
			}
		} else {
			this.fileChooser = new JFileChooser()
			this.prepareOpen = {}
			this.afterReturn = { result ->
				this.app.log.debug "afterReturn closure called with result = ${result}"
				if (JFileChooser.APPROVE_OPTION == result) {
					this.app.log.debug "fileChooser.selectedFile is '${fileChooser.selectedFile}'"
					this.file = fileChooser.selectedFile
					this.filename = this.file.name
					this.dirname = this.file.canonicalFile.parent
				}
			}
		}
		this.fileChooser
	}
		
	public FileChooser(GriffonApplication app, JPanel panel) {
		this.app = app
		this.frame = SwingUtilities.getAncestorOfClass(Frame.class, panel)
		this.useNativeDialog = this.app.config.selectableFileChooser.useNative.file
		this.fileChooser = createFileChooser(this.useNativeDialog)
	}

	public FileChooser(GriffonApplication app, JPanel panel, boolean dirOnly) {
		this.app = app
		this.frame = SwingUtilities.getAncestorOfClass(Frame.class, panel)
		this.useNativeDialog = dirOnly ? this.app.config.selectableFileChooser.useNative.directory : this.app.config.selectableFileChooser.useNative.file
		this.fileChooser = createFileChooser(this.useNativeDialog)

		if (dirOnly) {
			this.chooseDirOnly = true
			def commonPrepareOpen = this.prepareOpen.clone()
			def commonAfterReturn = this.afterReturn.clone()
			if (this.useNativeDialog) {
				prepareOpen = { 
					if (GriffonApplicationUtils.isMacOSX()) {
						System.setProperty( "apple.awt.fileDialogForDirectories", "true" )
					}
					commonPrepareOpen()
				}
				afterReturn = { result ->
					commonAfterReturn(result)
					if (GriffonApplicationUtils.isMacOSX()) {
						System.setProperty( "apple.awt.fileDialogForDirectories", "false" )
					}
				}
			} else {
				prepareOpen = { 
					this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY) 
					commonPrepareOpen()
				}
				afterReturn = { result ->
					commonAfterReturn(result)
				}
			}
		}
	}
		
	public void setCurrentDirectory(String dir) {
		if (this.useNativeDialog) {
			fileChooser.setDirectory(dir)
		} else {
			fileChooser.setCurrentDirectory(new File(dir))
		}
	}
	
	public void setCurrentDirectory(File dir) {
		if (this.useNativeDialog) {
			fileChooser.setDirectory(dir.canonicalPath)
		} else {
			fileChooser.setCurrentDirectory(dir)
		}
	}

	public void chooseFileToOpen(startDir = null) {
		if (startDir) {
			this.setCurrentDirectory(startDir)
		}
		this.prepareOpen()
		def result
		if (this.useNativeDialog) {
			this.fileChooser.setMode(FileDialog.LOAD)
			this.fileChooser.setTitle(this.openTitle)			
			this.fileChooser.setVisible(true)
		} else {
			result = this.fileChooser.showOpenDialog(this.frame)
		}
		this.afterReturn(result)
	}
	
	public void chooseFileToSave(startDir = null) {
		if (startDir) {
			this.setCurrentDirectory(startDir)
		}
		this.prepareOpen()
		def result
		if (this.useNativeDialog) {
			this.fileChooser.setMode(FileDialog.SAVE)
			this.fileChooser.setTitle(this.saveTitle)
			this.fileChooser.setVisible(true)
		} else {
			result = this.fileChooser.showSaveDialog(this.frame)
		}
		this.afterReturn(result)
	}
}
