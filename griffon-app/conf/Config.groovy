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
