class SelectableFilechooserGriffonPlugin {
    // the plugin version
    String version = '0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.1.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = '<UNKNOWN>'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, qt
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = ''

    List authors = [
        [
            name: 'Marc Paquette',
            email: 'marcpa@mac.com'
        ]
    ]
    String title = 'Plugin summary/headline'
    // accepts Markdown syntax. See http://daringfireball.net/projects/markdown/ for details
    String description = '''
SelectableFilechooser : Choose to use the native (AWT-based) or Swing open file widget according to platform.
When native is choosen, a java.awt.FileDialog is used, otherwise, javax.swing.FileChooser.

Usage
----
import com.marcpa.SelectableFileChooser

// to override default configuration
app.config.selectableFileChooser.useNative.file = true
app.config.selectableFileChooser.useNative.directory = false

...

// In a view script, with a panel child of application into variable mainPanel:
myFileChooser = new SelectableFileChooser(app, mainPanel)
myFileChooser.chooseFile("/Users")
myDirChooser = new SelectableFileChooser(app, mainPanel, true)

...

// in a controller 
def openFile = { evt = null ->
    view.myFileChooser.chooseFileToOpen()

   if (view.myFileChooser.file != null) {
        String fileText = view.myFileChooser.file.text

        ...
   }
}

def openDir = { evt = null ->
    view.myDirChooser.chooseFileToOpen(model.dirToOpen)
    if (view.dirChooserWindow.file != null) {
        String dirPath = view.dirChooserWindow.file.canonicalPath
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

'''
}
