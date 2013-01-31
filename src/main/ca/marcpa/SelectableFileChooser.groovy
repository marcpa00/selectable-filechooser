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
        app = ApplicationHolder.application
        useNativeDialog = app.config.selectableFileChooser.useNative.file
        fileChooser = createFileChooser()
    }

    public SelectableFileChooser(File currentDirectory) {
        app = ApplicationHolder.application
        useNativeDialog = app.config.selectableFileChooser.useNative.file
        fileChooser = createFileChooser(currentDirectory: currentDirectory)
    }

    public SelectableFileChooser(File currentDirectory, FileSystemView fsv) {
        app = ApplicationHolder.application
        tiveDialog = app.config.selectableFileChooser.useNative.file
        fileChooser = createFileChooser(currentDirectory: currentDirectory, fileSystemView: fsv)
    }

    public SelectableFileChooser(String currentDirectoryPath) {
        app = ApplicationHolder.application
        useNativeDialog = app.config.selectableFileChooser.useNative.file
        fileChooser = createFileChooser(currentDirectoryPath: currentDirectoryPath)
    }

    public SelectableFileChooser(String currentDirectoryPath, FileSystemView fsv) {
        app = ApplicationHolder.application
        useNativeDialog = app.config.selectableFileChooser.useNative.file
        fileChooser = createFileChooser(currentDirectoryPath: currentDirectoryPath, fileSystemView: fsv)
    }

    //
    // Non-facade constructors
    //

    public SelectableFileChooser(Map args) {
        if (! args) { args = [:] }
        app = args.app ?: ApplicationHolder.application
        fileChooser = createFileChooser(args)

    }

    public SelectableFileChooser(JFrame frame) {
        app = ApplicationHolder.application
        frame = frame
        useNativeDialog = app.config.selectableFileChooser.useNative.file
        fileChooser = createFileChooser(useNativeDialog)
    }


    public SelectableFileChooser(JPanel panel) {
        app = ApplicationHolder.application
        frame = SwingUtilities.getAncestorOfClass(Frame.class, panel)
        useNativeDialog = app.config.selectableFileChooser.useNative.file
        fileChooser = createFileChooser(useNativeDialog)
    }

    /**
     * Create a SelectableFileChooser, native of pure (Swing) according to plateform.  If dirOnly is true, the file chooser is configured
     * to select directories only.
     *
     * @limitation Depending on the underlying implementation of the actual component, directory selection may not be supported.
     *
     * @param dirOnly
     */
    public SelectableFileChooser(Boolean dirOnly) {
        app = ApplicationHolder.application
        useNativeDialog = dirOnly ? app.config.selectableFileChooser.useNative.directory : app.config.selectableFileChooser.useNative.file
        fileChooser = createFileChooser(dirOnly: dirOnly)
    }

    //
    // Façade to JFileChooser
    //

    public void setCurrentDirectory(String dir) {
        if (useNativeDialog) {
            fileChooser.setDirectory(dir)
        } else {
            fileChooser.setCurrentDirectory(new File(dir))
        }
    }

    public void setCurrentDirectory(File dir) {
        if (useNativeDialog) {
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
        if (fileChooser.file != null) {
            result = JFileChooser.APPROVE_OPTION
            dirname = fileChooser.directory
            filename = fileChooser.file
            selectedFile = new File("${dirname}/${filename}")
        } else {
            result = JFileChooser.CANCEL_OPTION
        }
        result
    }

    Closure pureFilePrepareOpen = {}
    Closure pureFileAfterReturn = { result ->
        app.log.debug "afterReturn closure called with result = ${result}"
        if (JFileChooser.APPROVE_OPTION == result) {
            app.log.debug "fileChooser.selectedFile is '${fileChooser.selectedFile}'"
            selectedFile = fileChooser.selectedFile
            filename = selectedFile.name
            dirname = selectedFile.canonicalFile.parent
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
        previousFileSelectionMode = fileChooser.getFileSelectionMode()
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
        pureFilePrepareOpen()
    }

    Closure pureDirectoryAfterReturn = { result ->
        def innerResult = pureFileAfterReturn(result)
        fileChooser.setFileSelectionMode(previousFileSelectionMode)
        innerResult
    }

    void configureClosures() {
        if (useNativeDialog) {
            if (chooseDirOnly) {
                prepareOpen = nativeDirectoryPrepareOpen
                afterReturn = nativeDirectoryAfterReturn
            } else {
                prepareOpen = nativeFilePrepareOpen
                afterReturn = nativeFileAfterReturn
            }
        } else {
            if (chooseDirOnly) {
                prepareOpen = pureDirectoryPrepareOpen
                afterReturn = pureDirectoryAfterReturn
            } else {
                prepareOpen = pureFilePrepareOpen
                afterReturn = pureFileAfterReturn
            }
        }
    }

    def createFileChooser = { args = [:] ->
        useNativeDialog = args.useNativeDialog ?: (useNativeDialog ?: false)
        if (useNativeDialog) {
            app.log.debug "createFileChooser using native (java.awt) dialog."
        } else {
            app.log.debug "createFileChooser using pure-java (javax.swing) file chooser."
        }

        chooseDirOnly = args.dirOnly ?: (chooseDirOnly ?: false)
        if (useNativeDialog) {

            if (!frame) {
                if (!app) {
                    app = ApplicationHolder.application
                }
                frame = app.windowManager.startingWindow
            }
            // defaults to open file dialog
            fileChooser = new FileDialog(frame, openTitle)
            fileChooser.directory = args.currentDirectory?.canonicalFile ?: args.currentDirectoryPath
        } else {
            if (args.currentDirectory && args.fileSystemView) {
                fileChooser = new JFileChooser(args.currentDirectory, args.fileSystemView)
            } else if (args.currentDirectory) {
                fileChooser = new JFileChooser(args.currentDirectory)
            } else if (args.currentDirectoryPath && args.fileSystemView) {
                fileChooser = new JFileChooser(args.currentDirectoryPath, args.fileSystemView)
            } else if (args.currentDirectoryPath) {
                fileChooser = new JFileChooser(args.currentDirectoryPath)
            } else {
                fileChooser = new JFileChooser()
            }
        }
        configureClosures()
        fileChooser
    }

    //
    // Convenience methods for simplified API of open and save file dialogs
    //

    public int chooseFileToOpen(startDir = null) {
        if (startDir) {
            setCurrentDirectory(startDir)
        }
        configureClosures()
        prepareOpen()
        def result
        if (useNativeDialog) {
            fileChooser.setMode(FileDialog.LOAD)
            fileChooser.setTitle(openTitle)
            fileChooser.setVisible(true)
        } else {
            // TODO : title ?
            result = fileChooser.showOpenDialog(frame)
        }
        afterReturn(result)
    }

    public int chooseFileToSave(startDir = null) {
        if (startDir) {
            setCurrentDirectory(startDir)
        }
        configureClosures()
        prepareOpen()
        def result
        if (useNativeDialog) {
            fileChooser.setMode(FileDialog.SAVE)
            fileChooser.setTitle(saveTitle)
            fileChooser.setVisible(true)
        } else {
            // TODO : title ?
            result = fileChooser.showSaveDialog(frame)
        }
        afterReturn(result)
    }

    public int chooseDir(startDir = null) {
        if (startDir) {
            setCurrentDirectory(startDir)
        }
        configureClosures()
        prepareOpen()
        def result
        if (useNativeDialog) {
            fileChooser.setMode(FileDialog.LOAD)
            fileChooser.setTitle(openDirectoryTitle)
            fileChooser.setVisible(true)
        } else {
            // TODO: find how to set the title for swing dialog
            result = fileChooser.showOpenDialog(frame)

        }
        afterReturn(result)

    }
}
