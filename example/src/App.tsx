import * as React from 'react';

import { StyleSheet, View, Text, TouchableOpacity, Button, TextInput } from 'react-native';
import { openCamera } from 'react-native-mi-snap-lib';

export default function App() {
  const [result, setResult] = React.useState<[string] | undefined>();
  const [text, onChangeText] = React.useState('EN');

  React.useEffect(() => {
   // openCamera("front","").then(setResult);
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
     openCamera("front","",
      text).then(setResult)
    } catch (e) {
      handleError(e);
    }
  };

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
        <Button onPress={handlePress} title="Calendar Test"></Button>
        <TextInput
        style={styles.input}
        onChangeText={onChangeText}
        value={text}
      />
         </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  input: {
    height: 40,
    margin: 12,
    backgroundColor: "white",
    borderWidth: 1,
    padding: 10,
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
