/**
* This exposes the native CalendarModule module as a JS module. This has a
* function 'createCalendarEvent' which takes the following parameters:

* 1. String name: A string representing the name of the event
* 2. String location: A string representing the location of the event
*/
import {NativeModules} from 'react-native';
const {AccessModule} = NativeModules;

interface AccessModule {
  saveItem(key: string, value: string): void;
  getItem(key: string): string;
}

export default AccessModule as AccessModule;
