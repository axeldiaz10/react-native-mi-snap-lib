import * as React from 'react';

import { StyleSheet, View, Text, Button, TextInput } from 'react-native';
import { openCamera } from 'react-native-mi-snap-lib';

export default function App() {
  const [result, setResult] = React.useState<[string] | undefined>();
  const [text, onChangeText] = React.useState('ES');

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
     openCamera("front",
      "eyJzaWduYXR1cmUiOiJvbVJXbFJyWXZJK2NVZHRnNFJnekk4K0pyYnlvc1kzeUJBRFkraUxjWTRsL21qZE03dEV3UXVIVHhyQzlLcDNDVE14Q3BrQy9qTUlRZWJ5Vk9SVzYyR0NocG1yOWg4aG1KVVJzdVh2YUFpK0JqUmk5VnJNaEMvamRtcnlYWWdvYThhdk1YdDdib2hnc1lHZVZtTWc0QU1jbkgramR5aGtEdDMvc3NIVVFjOURINW1BNlpDamxteE1Yd01Jb0RmM3cwa3AyeXl3RWFyaDVORUxFVGcwVFZqOVRmNy85QzAvRmFVRkN1SEYxQVh6N0hCUUhZUXZUcnpFVEVoS0JwNUZUQTdpN3R2Y0d5Nnc2d05VZXJlWTk0Zk9YcHJ5dEVtY2NEYkt2UVlzMjVJeWtsUzYyT3ZNM1FzRFBEcGVPK3lSbTQrSWxkU1c4a2FSZnkzT21MNUVEYVE9PSIsInNpZ25lZCI6eyJleHBpcnkiOiIyMDI1LTEyLTIyIiwiZmVhdHVyZXMiOlsiZGVwb3NpdCJdLCJhcHBsaWNhdGlvbklkZW50aWZpZXIiOnsiYW5kcm9pZCI6WyJuZXQudmlhY2FzaC5teXZpYSJdLCJpb3MiOlsibmV0LnZpYWNhc2gubXl2aWEiXX0sImdwbyI6IjQ0NyIsIm9yZ2FuaXphdGlvbiI6Ik1pdGVrIFN5c3RlbXMgSW5jLiIsInZlcnNpb24iOiIxLjAifX0=",
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
    color: "black",
    borderWidth: 1,
    padding: 10,
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
