import {NativeEventEmitter, NativeModules} from 'react-native';
import AccessModule from './src/AccessModule';
import RNSInfo from 'react-native-sensitive-info';

module.exports = async taskData => {
  const eventEmitter = new NativeEventEmitter(NativeModules.AccessModule);
  const onConnected = eventEmitter.addListener('onConnected', async event => {
    console.log(event);

    const gettingFirstData = await RNSInfo.getItem('data', {
      sharedPreferencesName: 'mySharedPrefs',
      keychainService: 'myKeychain',
    });
    console.log('initial data', JSON.parse(gettingFirstData));
    if (gettingFirstData) {
      AccessModule.sendData(gettingFirstData);
    }
  });

  const pswSaved = eventEmitter.addListener('pswSaved', async event => {
    console.log(event);

    const savingFirstData = await RNSInfo.setItem('data', event.pswSaved, {
      sharedPreferencesName: 'mySharedPrefs',
      keychainService: 'myKeychain',
    });
    console.log('saving', savingFirstData);
  });
};
