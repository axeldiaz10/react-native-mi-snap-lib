import { NativeModules } from 'react-native';
const { CalendarModule } = NativeModules;
interface CalendarInterface {
  createCalendarEvent(
    name: string,
    location: string
    // callbackSuccess: Function,
    // callbackError: Function
  ): void;
}
export default CalendarModule as CalendarInterface;
