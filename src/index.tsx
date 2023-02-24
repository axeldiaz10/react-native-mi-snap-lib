import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-mi-snap-lib' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const MiSnapLib = NativeModules.MiSnapLib
  ? NativeModules.MiSnapLib
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function setLanguage(language: string) {
  return MiSnapLib.setLanguage(language)
}

export function openCamera(type: string, license: string, language: string): Promise<string> {
  return MiSnapLib.openCamera(type, license, language);
}
