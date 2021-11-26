/**
 * @format
 */

import {AppRegistry} from 'react-native';
import App from './App';
import {name as appName} from './app.json';
import SaveFillData from './SaveFillData';

if (__DEV__) {
  import('./utils/ignoreWarning');
}

AppRegistry.registerComponent(appName, () => App);
AppRegistry.registerHeadlessTask('SaveFillData', () => SaveFillData);
