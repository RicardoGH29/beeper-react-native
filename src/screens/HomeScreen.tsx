import React, {useState, useEffect} from 'react';
import {
  View,
  Text,
  FlatList,
  Button,
  StyleSheet,
  PermissionsAndroid,
  Platform,
  Linking,
} from 'react-native';
import {BleManager, Device} from 'react-native-ble-plx';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {NativeModules, NativeEventEmitter} from 'react-native';
const {BluetoothConnectionModule} = NativeModules;

interface DeviceItemProps {
  item: Device;
  onConnect: (device: Device) => void;
}

const HomeScreen: React.FC = () => {
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [devices, setDevices] = useState<Device[]>([]);
  const [bleManager] = useState<BleManager>(new BleManager());

  // Request Bluetooth permissions
  const requestBluetoothPermissions = async (): Promise<boolean> => {
    if (Platform.OS === 'android' && Platform.Version >= 23) {
      try {
        const granted = await PermissionsAndroid.requestMultiple([
          PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
          PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        ]);
        return Object.values(granted).every(
          permission => permission === PermissionsAndroid.RESULTS.GRANTED,
        );
      } catch (err) {
        console.warn(err);
        return false;
      }
    }
    return true;
  };

  // Request notification permissions
  async function requestNotificationPermissions(): Promise<boolean> {
    if (Platform.OS === 'android') {
      try {
        const hasShownSettings = await AsyncStorage.getItem(
          'hasShownNotificationSettings',
        );
        const isPostGranted = await PermissionsAndroid.check(
          PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
        );

        if (!isPostGranted) {
          const postGranted = await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
          );
          if (postGranted !== PermissionsAndroid.RESULTS.GRANTED) {
            return false;
          }
        }

        if (!hasShownSettings) {
          await AsyncStorage.setItem('hasShownNotificationSettings', 'true');
          await Linking.sendIntent(
            'android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS',
          );
        }

        return true;
      } catch (err) {
        console.warn(
          'Error al verificar/solicitar permisos de notificaciones:',
          err,
        );
        return false;
      }
    }
    return true;
  }

  // Scan for Bluetooth devices
  const scanDevices = async (): Promise<void> => {
    const permissionsGranted = await requestBluetoothPermissions();

    if (permissionsGranted) {
      setDevices([]);
      bleManager.startDeviceScan(
        null,
        null,
        (error: Error | null, device: Device | null) => {
          if (error) {
            console.log(error);
            return;
          }

          if (device) {
            setDevices(prevDevices => {
              const exists = prevDevices.some(d => d.id === device.id);
              return exists ? prevDevices : [...prevDevices, device];
            });
          }
        },
      );

      setTimeout(() => {
        bleManager.stopDeviceScan();
      }, 10000);
    }
  };

  // Connect to a device
  const connectToDevice = async (device: Device): Promise<void> => {
    try {
      bleManager.stopDeviceScan();

      BluetoothConnectionModule.saveConnectedDevice(
        device.id,
        device.name || 'Unknown Device',
      );
      await BluetoothConnectionModule.connectToDevice(device.id);
    } catch (error) {
      console.log('Error conectando:', error);
    }
  };

  // Disconnect device
  const disconnectDevice = async (): Promise<void> => {
    console.log('Disconnecting device');
    if (BluetoothConnectionModule.isConnected()) {
      console.log('Connected device');
      setIsConnected(false);
      BluetoothConnectionModule.clearConnectedDevice();
    }
  };

  // Render device information
  const DeviceItem: React.FC<DeviceItemProps> = ({item, onConnect}) => (
    <View style={styles.deviceItem}>
      <Text style={styles.deviceName}>
        {item.name || 'Dispositivo sin nombre'}
      </Text>
      <Text>{item.id}</Text>
      <Button title="Conectar" onPress={() => onConnect(item)} />
    </View>
  );

  // Cleanup on unmount
  useEffect(() => {
    requestNotificationPermissions().then();

    return () => {
      bleManager.destroy();
    };
  }, [bleManager]);

  useEffect(() => {
    // Create event emitter for the BluetoothConnectionModule
    const eventEmitter = new NativeEventEmitter(BluetoothConnectionModule);
    // Subscribe to Bluetooth connection events
    const connectionListener = eventEmitter.addListener(
      'onBluetoothConnectionChanged',
      connected => {
        console.log('Bluetooth connection changed:', connected);
        setIsConnected(connected);

        if (connected) {
          const now = new Date();
          const messageSendHour = `time|${now.getHours()}|${now.getMinutes()}|${now.getSeconds()}|${now.getDate()}|${
            now.getMonth() + 1
          }|${now.getFullYear()}`;
          setTimeout(() => { BluetoothConnectionModule.sendDataToDevice(messageSendHour); }, 1000);
        }
      },
    );

    // Cleanup listener when component unmounts
    return () => {
      connectionListener.remove();
    };
  }, []);


  return (
    <View style={styles.container}>
      {isConnected ? (
        <View style={styles.connectionStatus}>
          <Text style={styles.connectedText}>
            Conectado a:{' '}
            {BluetoothConnectionModule.getConnectedDevice() || 'Dispositivo'}
          </Text>
          <Button title="Desconectar" onPress={disconnectDevice} color="red" />
        </View>
      ) : (
        <View style={styles.connectionStatus}>
          <Text style={styles.disconnectedText}>No conectado</Text>
          <Button title="Escanear Dispositivos" onPress={scanDevices} />
        </View>
      )}

      <FlatList<Device>
        data={devices}
        renderItem={({item}) => (
          <DeviceItem item={item} onConnect={connectToDevice} />
        )}
        keyExtractor={item => item.id}
        ListEmptyComponent={
          <Text style={styles.emptyList}>No se encontraron dispositivos</Text>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  connectionStatus: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
    padding: 15,
    backgroundColor: 'white',
    borderRadius: 10,
  },
  connectedText: {
    color: 'green',
    fontWeight: 'bold',
  },
  disconnectedText: {
    color: 'red',
    fontWeight: 'bold',
  },
  deviceItem: {
    backgroundColor: 'white',
    padding: 15,
    marginVertical: 8,
    borderRadius: 10,
  },
  deviceName: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  emptyList: {
    textAlign: 'center',
    marginTop: 50,
    color: 'gray',
  },
});

export default HomeScreen;
