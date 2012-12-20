package ca.marcpa

import griffon.core.GriffonApplication
import griffon.util.GriffonApplicationUtils
import griffon.util.ApplicationHolder
import griffon.swing.WindowManager

import java.awt.*
import javax.swing.*
import javax.swing.filechooser.FileSystemView;

/**
 * A SelectableFileChooser is somewhat similar to a Swing JFileChooser API-wise but can possibly be implemented with
 * a native-backed AWT FileDialog widget.  Selection between native and Swing is through griffon's app.config object
 * of running application.
 * The config of plugin have default values for selection of native vs Swing according to the operating platform.
 *
 * Basically, where one used a JFileChooser constructor before, one can use a ca.marcpa.SelectableFileChooser constructor.
 *
 */
public class SelectableFileChooser {

    GriffonApplication app
    boolean chooseDirOnly = false
    boolean useNativeDialog
    def fileChooser
    Frame frame
    String filename
    String dirname
    File selectedFile
    int previousFileSelectionMode = JFileChooser.FILES_ONLY
    Closure prepareOpen
    Closure afterReturn
    String openTitle = "Open"
    String saveTitle = "Save"
    String openDirectoryTitle = "Select Directory"

    //
    // JFileChooser facade constructors
    //

    public SelectableFileChooser() {
        this.app = ApplicationHolder.application
        this.useNativeDialog = this.app.config.selectableFileChooser.useNative.file
        this.fileChooser = createFileChooser()
    }

    public SelectableFileChooser(File currentDirectory) {
        this.app = ApplicationHolder.application
        this.useNativeDialog = this.app.config.selectableFileChooser.useNative.file
        this.fileChooser = createFileChooser(currentDirectory: currentDirectory)
    }

    public SelectableFileChooser(File currentDirectory, FileSystemView fsv) {
        this.app = ApplicationHolder.application
        this.useNativeDialog = this.app.config.selectableFileChooser.useNative.file
        this.fileChooser = createFileChooser(currentDirectory: currentDirectory, fileSystemView: fsv)
    }

    public SelectableFileChooser(String currentDirectoryPath) {
        this.app = ApplicationHolder.application
        this.useNativeDialog = this.app.config.selectableFileChooser.useNative.file
        this.fileChooser = createFileChooser(currentDirectoryPath: currentDirectoryPath)
    }

    public SelectableFileChooser(String currentDirectoryPath, FileSystemView fsv) {
        this.app = ApplicationHolder.application
        this.useNativeDialog = this.app.config.selectableFileChooser.useNative.file
        this.fileChooser = createFileChooser(currentDirectoryPath: currentDirectoryPath, fileSystemView: fsv)
    }

    //
    // Non-facade constructors
    //

    public SelectableFileChooser(JFrame frame) {
        this.app = ApplicationHolder.application
        this.frame = frame
        this.useNativeDialog = this.app.config.selectableFileChooser.useNative.file
        this.fileChooser = createFileChooser(this.useNativeDialog)
    }


    public SelectableFileChooser(JPanel panel) {
        this.app = ApplicationHolder.application
        this.frame = SwingUtilities.getAncestorOfClass(Frame.class, panel)
        this.useNativeDialog = this.app.config.selectableFileChooser.useNative.file
        this.fileChooser = createFileChooser(this.useNativeDialog)
    }

    /**
     * Create a SelectableFileChooser, native of pure (Swing) according to plateform.  If dirOnly is true, the file chooser is configured
     * to select directories only.
     *
     * @limitation Depending on the underlying implementation of the actual component, directory selection may not be supported.
     *
     * @param dirOnly
     */
    public SelectableFileChooser(boolean dirOnly) {
        this.app = ApplicationHolder.application
        this.useNativeDialog = dirOnly ? this.app.config.selectableFileChooser.useNative.directory : this.app.config.selectableFileChooser.useNative.file
        this.fileChooser = createFileChooser(dirOnly: dirOnly)
    }

    //
    // Façade to JFileChooser
    //

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

    //
    // SelectableFileChooser internals
    //

    Closure nativeFilePrepareOpen = {}
    Closure nativeFileAfterReturn = { result ->
        if (this.fileChooser.file != null) {
            result = JFileChooser.APPROVE_OPTION
            this.dirname = this.fileChooser.directory
            this.filename = this.fileChooser.file
            this.selectedFile = new File("${this.dirname}/${this.filename}")
        } else {
            result = JFileChooser.CANCEL_OPTION
        }
        result
    }

    Closure pureFilePrepareOpen = {}
    Closure pureFileAfterReturn = { result ->
        this.app.log.debug "afterReturn closure called with result = ${result}"
        if (JFileChooser.APPROVE_OPTION == result) {
            this.app.log.debug "fileChooser.selectedFile is '${fileChooser.selectedFile}'"
            this.selectedFile = fileChooser.selectedFile
            this.filename = this.selectedFile.name
            this.dirname = this.selectedFile.canonicalFile.parent
        }
        result
    }

    Closure nativeDirectoryPrepareOpen = {
        if (GriffonApplicationUtils.isMacOSX()) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true")
        }
        nativeFilePrepareOpen()
    }

    Closure nativeDirectoryAfterReturn = { result ->
        result = nativeFileAfterReturn(result)
        if (GriffonApplicationUtils.isMacOSX()) {
            System.setProperty("apple.awt.fileDialogForDirectories", "false")
        }
        result
    }

    Closure pureDirectoryPrepareOpen = {
        this.previousFileSelectionMode = this.fileChooser.getFileSelectionMode()
        this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
        pureFilePrepareOpen()
    }

    Closure pureDirectoryAfterReturn = { result ->
        def innerResult = pureFileAfterReturn(result)
        this.fileChooser.setFileSelectionMode(this.previousFileSelectionMode)
        innerResult
    }

    void configureClosures() {
        if (useNativeDialog) {
            if (this.chooseDirOnly) {
                this.prepareOpen = nativeDirectoryPrepareOpen
                this.afterReturn = nativeDirectoryAfterReturn
            } else {
                this.prepareOpen = nativeFilePrepareOpen
                this.afterReturn = nativeFileAfterReturn
            }
        } else {
            if (this.chooseDirOnly) {
                this.prepareOpen = pureDirectoryPrepareOpen
                this.afterReturn = pureDirectoryAfterReturn
            } else {
                this.prepareOpen = pureFilePrepareOpen
                this.afterReturn = pureFileAfterReturn
            }
        }
    }

    def createFileChooser = { args ->
        if (args.contains("useNativeDialog")) {
            this.useNativeDialog = args.useNativeDialog
        }
        if (this.useNativeDialog) {
            /*this.app.log.debug*/ println "createFileChooser using native (java.awt) dialog."
        } else {
            /*this.app.log.debug*/ println "createFileChooser using pure-java (javax.swing) file chooser."
        }

        if (args.contains("dirOnly")) {
            this.chooseDirOnly = args.dirOnly
        }
        if (this.useNativeDialog) {
            if (!this.frame) {
                if (!this.app) {
                    this.app = ApplicationHolder.application
                }
                this.frame = this.app.windowManager.startingWindow

            }
            // defaults to open file dialog
            this.fileChooser = new FileDialog(frame, this.openTitle)
            if (args?.currentDirectory) {
                this.fileChooser.directory = args.currentDirectory.canonicalFile
            } else if (args?.currentDirectoryPath) {
                this.fileChooser.directory = args.currentDirectoryPath
            }
        } else {
            if (args && args.currentDirectory && args.fileSystemView) {
                this.fileChooser = new JFileChooser(args.currentDirectory, args.fileSystemView)
            } else if (args && args.currentDirectory) {
                this.fileChooser = new JFileChooser(args.currentDirectory)
            } else if (args && args.currentDirectoryPath && args.fileSystemView) {
                this.fileChooser = new JFileChooser(args.currentDirectoryPath, args.fileSystemView)
            } else if (args && args.currentDirectoryPath) {
                this.fileChooser = new JFileChooser(args.currentDirectoryPath)
            } else {
                this.fileChooser = new JFileChooser()
            }
        }
        configureClosures()
        this.fileChooser
    }

    //
    // Convenience methods for simplified API of open and save file dialogs
    //

    public int chooseFileToOpen(startDir = null) {
        if (startDir) {
            setCurrentDirectory(startDir)
        }
        configureClosures()
        this.prepareOpen()
        def result
        if (this.useNativeDialog) {
            this.fileChooser.setMode(FileDialog.LOAD)
            this.fileChooser.setTitle(this.openTitle)
            this.fileChooser.setVisible(true)
        } else {
            // TODO : title ?
            result = this.fileChooser.showOpenDialog(this.frame)
        }
        this.afterReturn(result)
    }

    public int chooseFileToSave(startDir = null) {
        if (startDir) {
            setCurrentDirectory(startDir)
        }
        configureClosures()
        this.prepareOpen()
        def result
        if (this.useNativeDialog) {
            this.fileChooser.setMode(FileDialog.SAVE)
            this.fileChooser.setTitle(this.saveTitle)
            this.fileChooser.setVisible(true)
        } else {
            // TODO : title ?
            result = this.fileChooser.showSaveDialog(this.frame)
        }
        this.afterReturn(result)
    }

    public int chooseDir(startDir = null) {
        if (startDir) {
            setCurrentDirectory(startDir)
        }
        configureClosures()
        this.prepareOpen()
        def result
        if (this.useNativeDialog) {
            this.fileChooser.setMode(FileDialog.LOAD)
            this.fileChooser.setTitle(this.openDirectoryTitle)
            this.fileChooser.setVisible(true)
        } else {
            // TODO: find how to set the title for swing dialog
            result = this.fileChooser.showOpenDialog(this.frame)

        }
        this.afterReturn(result)

    }
}
