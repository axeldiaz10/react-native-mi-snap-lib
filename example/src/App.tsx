import * as React from 'react';

import { StyleSheet, View, Text, TouchableOpacity } from 'react-native';
import { openCamera } from 'react-native-mi-snap-lib';

export default function App() {
  const [result, setResult] = React.useState<[string] | undefined>();

  React.useEffect(() => {
    openCamera("front","").then(setResult);
  }, []);

  const handleSuccess = (eventId: any) => {
    console.log(`event id ${eventId} returned`);
  };

  const handleError = (error: any) => {
    console.error('Error!', error);
  };

  const handlePress = async () => {
    console.log('do somethig');
    try {
     openCamera("front", "LICENCE_KEY_HERE").then(setResult)
    } catch (e) {
      handleError(e);
    }
  };

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <TouchableOpacity onPress={handlePress}>
        <Text>Calendar test</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
