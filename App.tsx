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

interface DeviceItemProps {
  item: Device;
  onConnect: (device: Device) => void;
}

const BluetoothApp: React.FC = () => {
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [connectedDevice, setConnectedDevice] = useState<Device | null>(null);
  const [devices, setDevices] = useState<Device[]>([]);
  const [bleManager] = useState<BleManager>(new BleManager());

  // Solicitar permisos de Bluetooth
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

  async function requestNotificationPermissions(): Promise<boolean> {
    if (Platform.OS === 'android') {
      try {
        // Verificar si ya hemos mostrado la configuración de notificaciones
        const hasShownSettings = await AsyncStorage.getItem('hasShownNotificationSettings');
        
        // Verificar si ya tenemos el permiso para enviar notificaciones
        const isPostGranted = await PermissionsAndroid.check(
          PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
        );

        // Si falta el permiso de envío de notificaciones, lo solicitamos
        if (!isPostGranted) {
          const postGranted = await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
          );
          if (postGranted !== PermissionsAndroid.RESULTS.GRANTED) {
            return false;
          }
        }

        // Solo abrimos la configuración si no lo hemos hecho antes
        if (!hasShownSettings) {
          await Linking.sendIntent(
            'android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS',
          );
          // Guardamos que ya mostramos la configuración
          await AsyncStorage.setItem('hasShownNotificationSettings', 'true');
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

  // Escanear dispositivos Bluetooth
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
            // Agregar dispositivos únicos a la lista
            setDevices(prevDevices => {
              const exists = prevDevices.some(d => d.id === device.id);
              return exists ? prevDevices : [...prevDevices, device];
            });
          }
        },
      );

      // Detener escaneo después de 10 segundos
      setTimeout(() => {
        bleManager.stopDeviceScan();
      }, 10000);
    }
  };

  // Conectar a un dispositivo
  const connectToDevice = async (device: Device): Promise<void> => {
    try {
      const connectedDevice = await bleManager.connectToDevice(device.id);
      setConnectedDevice(connectedDevice);
      setIsConnected(true);
      bleManager.stopDeviceScan();
    } catch (error) {
      console.log('Error conectando:', error);
    }
  };

  // Desconectar dispositivo
  const disconnectDevice = async (): Promise<void> => {
    if (connectedDevice) {
      await bleManager.cancelDeviceConnection(connectedDevice.id);
      setConnectedDevice(null);
      setIsConnected(false);
    }
  };

  // Renderizar información de dispositivo
  const DeviceItem: React.FC<DeviceItemProps> = ({item, onConnect}) => (
    <View style={styles.deviceItem}>
      <Text style={styles.deviceName}>
        {item.name || 'Dispositivo sin nombre'}
      </Text>
      <Text>{item.id}</Text>
      <Button title="Conectar" onPress={() => onConnect(item)} />
    </View>
  );

  // Limpiar al desmontar
  useEffect(() => {
    requestNotificationPermissions();
    return () => {
      bleManager.destroy();
    };
  }, [bleManager]);

  return (
    <View style={styles.container}>
      {isConnected ? (
        <View style={styles.connectionStatus}>
          <Text style={styles.connectedText}>
            Conectado a: {connectedDevice?.name || 'Dispositivo'}
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

export default BluetoothApp;
