SelectableFilechooser : Choose to use the native (AWT-based) or Swing open file widget according to a config variable that can be set according to platform.
When native is choosen, a java.awt.FileDialog is used, otherwise, javax.swing.FileChooser.

The API of this plugin is roughly aligned with Swing's JFileChooser : not all of JFileChooser API is implemented but the way to use a SelectableFilechooser is
similar to JFileChooser (and thus *not* similar to a java.awt.FileDialog).

Usage
----
import com.marcpa.SelectableFileChooser

// to override default configuration
app.config.selectableFileChooser.useNative.file = true
app.config.selectableFileChooser.useNative.directory = false

...

// In a view script, with a panel child of application into variable mainPanel:
myFileChooser = new SelectableFileChooser(app, mainPanel)
myDirChooser = new SelectableFileChooser(app, mainPanel, true)

...

// in a controller
def openFile = { evt = null ->
    view.myFileChooser.chooseFileToOpen()

   if (view.myFileChooser.selectedFile != null) {
        String fileText = view.myFileChooser.selectedFile.text

        ...
   }
}

def openDir = { evt = null ->
    view.myDirChooser.chooseFileToOpen(model.dirToOpen)
    if (view.dirChooserWindow.selectedFile != null) {
        String dirPath = view.dirChooserWindow.selectedFile.canonicalPath
    }
    ...
}

Configuration
-------------

Default configuration of pluging:

switch (griffon.util.GriffonApplicationUtils.platform) {
	case 'linux' :
	case 'solaris' :
		selectableFileChooser  {
                useNative {
                        file = false
                		directory = false
                }
		}
		break;
	case 'macosx' :
		selectableFileChooser  {
                useNative {
                        file = true
                        directory = true
                }
		}
		break;
	case 'windows' :
		selectableFileChooser  {
                useNative {
                        file = true
                        directory = false
                }
		}
		break;
	default: break;
}

