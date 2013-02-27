import griffon.core.GriffonApplication

/**
 * Copyright 2013 Marc Paquette
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class SelectableFilechooserGriffonAddon {
    // lifecycle methods

    // called once, after the addon is created
    void addonInit(GriffonApplication app) {
    }

    // called once, after all addons have been inited
    /**
     * This post init method will set the native or swing value for file and directory choosers according to
     * "sensible" defaults for each platform :
     *
     * - solaris and linux native choosers are minimal; go with the swing one.
     * - Mac OS X has good native support for both file and directory choosers.
     * - windows has good native support for file chooser, but directory selection is better handled by swing's chooser.
     *
     * @param app
     */
    void addonPostInit(GriffonApplication app) {
        switch (griffon.util.GriffonApplicationUtils.platform) {
            case 'linux' :
            case 'solaris' :
                app.config.selectableFileChooser.useNative.file = false
                app.config.selectableFileChooser.useNative.directory = false
                break;
            case 'macosx' :
            case 'macosx64':
                app.config.selectableFileChooser.useNative.file = true
                app.config.selectableFileChooser.useNative.directory = true
                break;
            case 'windows' :
                app.config.selectableFileChooser.useNative.file = true
                app.config.selectableFileChooser.useNative.directory = false
                break;
            default: break;
        }
    }

    // called many times, after creating a builder
    //void addonBuilderInit(GriffonApplication app, FactoryBuilderSupport builder) {
    //}

    // called many times, after creating a builder and after
    // all addons have been inited
    //void addonBuilderPostInit(GriffonApplication app, FactoryBuilderSupport builder) {
    //}


    // to add MVC Groups use create-mvc


    // builder fields, these are added to all builders.
    // closures can either be literal { it -> println it}
    // or they can be method closures: this.&method

    // adds methods to all builders
    //Map methods = [
    //    methodName: { /*Closure*/ }
    //]

    // adds properties to all builders
    //Map props = [
    //    propertyName: [
    //        get: { /* optional getter closure */ },
    //        set: {val-> /* optional setter closure */ },
    //  ]
    //]

    // adds new factories to all builders
    //Map factories = [
    //    factory : /*instance that extends Factory*/
    //]

    // adds application event handlers
    //Map events = [
    //    "StartupStart": {app -> /* event hadler code */ }
    //]

    // handle synthetic node properties or
    // intercept existing ones
    //List attributeDelegates = [
    //    {builder, node, attributes -> /*handler code*/ }
    //]

    // called before a node is instantiated
    //List preInstantiateDelegates = [
    //    {builder, attributes, value -> /*handler code*/ }
    //]

    // called after the node was instantiated
    //List postInstantiateDelegates = [
    //    {builder, attributes, node -> /*handler code*/ }
    //]

    // called after the node has been fully
    // processed, including child content
    //List postNodeCompletionDelegates = [
    //    {builder, parent, node -> /*handler code*/ }
    //]
}
