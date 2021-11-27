import {NativeEventEmitter, NativeModules} from 'react-native';
import AccessModule from './src/AccessModule';

module.exports = async taskData => {
  let data = [
    {
      username: 'Ahoj',
      password: 'strejdo',
      androidUri: 'www.skolaonline.cz',
      usernameHint: 'Uživatelské jméno',
      passwordHint: 'Heslo',
    },
  ];

  const eventEmitter = new NativeEventEmitter(NativeModules.AccessModule);
  const eventListener = eventEmitter.addListener('onConnected', event => {
    console.log(event);
    AccessModule.sendData(data);
  });
};
