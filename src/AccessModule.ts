/**
* This exposes the native CalendarModule module as a JS module. This has a
* function 'createCalendarEvent' which takes the following parameters:

* 1. String name: A string representing the name of the event
* 2. String location: A string representing the location of the event
*/
import {NativeModules} from 'react-native';
const {AccessModule} = NativeModules;

interface readableMap {
  username: string;
  password: string;
  androidUri: string;
}
interface AccessModule {
  sendData: (readableMap: readableMap[]) => boolean;
  getData: () => Promise<string>;
}

export default AccessModule as AccessModule;
