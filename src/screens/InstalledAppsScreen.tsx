import React, { useEffect, useState, useCallback } from 'react';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  Platform,
  ActivityIndicator,
  Alert,
} from 'react-native';
import InstalledApps from 'react-native-installed-apps';
import { PermissionsAndroid } from 'react-native';

interface AppInfo {
  appName: string;
  packageName: string;
  versionName?: string;
}

const InstalledAppsScreen: React.FC = () => {
  const [apps, setApps] = useState<AppInfo[]>([]);
  const [loading, setLoading] = useState(true);

  const requestQueryAllPackagesPermission = async (): Promise<boolean> => {
    if (Platform.OS === 'android' && Platform.Version >= 30) {
      try {
        const granted = await PermissionsAndroid.request(
          'android.permission.QUERY_ALL_PACKAGES' as any,
        );
        return granted === PermissionsAndroid.RESULTS.GRANTED;
      } catch (error) {
        console.error('Error al solicitar permiso:', error);
        return false;
      }
    }
    return true;
  };

  const loadInstalledApps = useCallback(async () => {
    try {
      if (Platform.OS === 'android') {
        const hasPermission = await requestQueryAllPackagesPermission();
        if (!hasPermission) {
          Alert.alert(
            'Permiso requerido',
            'Se necesita permiso para ver las aplicaciones instaladas. Por favor, habilita el permiso en la configuración.',
          );
          setLoading(false);
          return;
        }
        const installedApps = await InstalledApps.getApps();
        setApps(installedApps);
      } else {
        // Para iOS, por ahora mostraremos un mensaje
        setApps([]);
      }
    } catch (error) {
      console.error('Error al cargar las aplicaciones:', error);
      Alert.alert(
        'Error',
        'No se pudieron cargar las aplicaciones instaladas. Por favor, intenta de nuevo.',
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadInstalledApps();
  }, [loadInstalledApps]);

  const renderAppItem = ({ item }: { item: AppInfo }) => (
    <TouchableOpacity style={styles.appItem}>
      <View style={styles.appIconContainer}>
        <Text style={styles.appIconText}>{item.appName[0]}</Text>
      </View>
      <View style={styles.appInfo}>
        <Text style={styles.appName}>{item.appName}</Text>
        <Text style={styles.packageName}>{item.packageName}</Text>
        {item.versionName && (
          <Text style={styles.versionName}>Versión: {item.versionName}</Text>
        )}
      </View>
    </TouchableOpacity>
  );

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#007AFF" />
        <Text style={styles.loadingText}>Cargando aplicaciones...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={apps}
        renderItem={renderAppItem}
        keyExtractor={(item) => item.packageName}
        contentContainerStyle={styles.listContainer}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>
              {Platform.OS === 'ios'
                ? 'La funcionalidad de listar aplicaciones instaladas no está disponible en iOS'
                : 'No se encontraron aplicaciones instaladas'}
            </Text>
          </View>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666666',
  },
  listContainer: {
    padding: 16,
  },
  appItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
      },
      android: {
        elevation: 3,
      },
    }),
  },
  appIconContainer: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: '#007AFF',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 16,
  },
  appIconText: {
    color: '#ffffff',
    fontSize: 24,
    fontWeight: 'bold',
  },
  appInfo: {
    flex: 1,
  },
  appName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333333',
    marginBottom: 4,
  },
  packageName: {
    fontSize: 14,
    color: '#666666',
    marginBottom: 2,
  },
  versionName: {
    fontSize: 12,
    color: '#999999',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  emptyText: {
    textAlign: 'center',
    fontSize: 16,
    color: '#666666',
  },
});

export default InstalledAppsScreen; 